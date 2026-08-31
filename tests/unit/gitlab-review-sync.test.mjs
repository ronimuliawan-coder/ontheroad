import test from "node:test";
import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";

import {
    gatePullRequestPlan,
    parseGitLabReviewRefs,
    planEvent,
    reviewRef,
    validateBranch,
    validateGitLabApiUrl,
    validateGitLabProject,
    validateGitLabUrl,
    validateRepository,
    validateSha,
} from "../../scripts/sync-gitlab-review.mjs";

const REPOSITORY = "ronimuliawan/ontheroad";
const GITLAB_PROJECT = "ronimuliawan/ontheroad";
const GITLAB_URL = "git@gitlab.com:ronimuliawan/ontheroad.git";
const GITLAB_API_URL = "https://gitlab.com/api/v4";
const HEAD_SHA = "a".repeat(40);
const BASE_SHA = "b".repeat(40);

function pullRequestEvent(overrides = {}) {
    return {
        action: "opened",
        number: 42,
        pull_request: {
            number: 42,
            draft: false,
            title: "Reviewable change",
            head: {
                ref: "feature/safe-change",
                sha: HEAD_SHA,
                repo: { full_name: REPOSITORY },
            },
            base: { ref: "dev", sha: BASE_SHA },
        },
        ...overrides,
    };
}

test("review projection constants are pinned to the OnTheRoad boundary", async () => {
    assert.equal(validateRepository(REPOSITORY), REPOSITORY);
    assert.equal(validateGitLabProject(GITLAB_PROJECT), GITLAB_PROJECT);
    assert.equal(validateGitLabUrl(GITLAB_URL), GITLAB_URL);
    assert.equal(validateGitLabApiUrl(GITLAB_API_URL), GITLAB_API_URL);
    assert.equal(reviewRef(42), "review/github-pr-42");

    const workflow = await readFile(".github/workflows/gitlab-review-replica.yml", "utf8");
    assert.match(workflow, /branches: \[ dev, main \]/);
    assert.match(workflow, /--gitlab-project ronimuliawan\/ontheroad/);
    assert.match(workflow, /GITLAB_REPLICA_SSH_KEY/);
    assert.match(workflow, /GITLAB_REPLICA_KNOWN_HOSTS/);
    assert.match(workflow, /GITLAB_REPLICA_API_TOKEN/);
    assert.match(workflow, /cancel-in-progress: false/);
    assert.match(workflow, /queue: max/);
    assert.doesNotMatch(workflow, /git push --mirror/);
});

test("active same-repository PR events map deterministically to a synthetic review ref", () => {
    for (const action of ["opened", "synchronize", "reopened", "ready_for_review", "converted_to_draft"]) {
        const event = pullRequestEvent({ action });
        const first = planEvent({ eventName: "pull_request_target", event, repository: REPOSITORY });
        const second = planEvent({ eventName: "pull_request_target", event, repository: REPOSITORY });

        assert.deepEqual(first, second);
        assert.deepEqual(first, {
            kind: "project-pull-request",
            repository: REPOSITORY,
            number: 42,
            sourceRef: "review/github-pr-42",
            sourceFetchRef: "refs/pull/42/head",
            targetBranch: "dev",
            targetFetchRef: "refs/heads/dev",
            headSha: HEAD_SHA,
            baseSha: BASE_SHA,
            draft: false,
        });
    }
});

test("closed PRs map to cleanup while unsupported actions are no-ops", () => {
    assert.deepEqual(
        planEvent({
            eventName: "pull_request_target",
            event: pullRequestEvent({ action: "closed" }),
            repository: REPOSITORY,
        }),
        {
            kind: "delete-review-ref",
            repository: REPOSITORY,
            sourceRef: "review/github-pr-42",
            number: 42,
            reason: "GitHub pull request closed",
        },
    );

    assert.deepEqual(
        planEvent({
            eventName: "pull_request_target",
            event: pullRequestEvent({ action: "labeled" }),
            repository: REPOSITORY,
        }),
        {
            kind: "noop",
            repository: REPOSITORY,
            reason: "pull request action labeled does not change the review ref",
        },
    );
});

test("only dev and main are canonical projections", () => {
    for (const branch of ["dev", "main"]) {
        assert.deepEqual(
            planEvent({
                eventName: "push",
                event: { ref: `refs/heads/${branch}`, after: HEAD_SHA },
                repository: REPOSITORY,
            }),
            {
                kind: "project-canonical-ref",
                repository: REPOSITORY,
                branch,
                fetchRef: `refs/heads/${branch}`,
                sha: HEAD_SHA,
            },
        );
    }

    assert.equal(
        planEvent({
            eventName: "push",
            event: { ref: "refs/heads/feature/unsafe", after: HEAD_SHA },
            repository: REPOSITORY,
        }).kind,
        "noop",
    );

    assert.deepEqual(
        planEvent({ eventName: "schedule", event: {}, repository: REPOSITORY }),
        {
            kind: "reconcile",
            repository: REPOSITORY,
            branches: ["dev", "main"],
            tagPattern: "^(?:v\\d+\\.\\d+\\.\\d+(?:[-+][0-9A-Za-z.-]+)?|release-[0-9A-Za-z._-]+)$",
        },
    );
});

test("fork PRs and unsafe metadata cannot reach the secret-bearing projection", () => {
    const fork = pullRequestEvent();
    fork.pull_request.head.repo.full_name = "attacker/fork";
    assert.throws(
        () => planEvent({ eventName: "pull_request_target", event: fork, repository: REPOSITORY }),
        /fork pull requests/,
    );

    const hostile = pullRequestEvent();
    hostile.pull_request.title = "$(touch exploited)\n--upload-pack=evil";
    hostile.pull_request.head.ref = "../../dev; echo stolen";
    assert.deepEqual(
        planEvent({ eventName: "pull_request_target", event: hostile, repository: REPOSITORY }),
        planEvent({ eventName: "pull_request_target", event: pullRequestEvent(), repository: REPOSITORY }),
    );
});

test("stale lifecycle events converge on current GitHub PR state", async () => {
    const synchronizePlan = planEvent({
        eventName: "pull_request_target",
        event: pullRequestEvent({ action: "synchronize" }),
        repository: REPOSITORY,
    });
    const closePlan = planEvent({
        eventName: "pull_request_target",
        event: pullRequestEvent({ action: "closed" }),
        repository: REPOSITORY,
    });
    const token = "github-job-token";
    const requests = [];
    let current = {
        ...pullRequestEvent().pull_request,
        state: "open",
        head: { ...pullRequestEvent().pull_request.head, sha: "c".repeat(40) },
    };
    const fetchImpl = async (input, init) => {
        requests.push({ url: String(input), init });
        return Response.json(current);
    };

    await assert.doesNotReject(
        gatePullRequestPlan(synchronizePlan, { githubToken: token, fetchImpl }),
    );
    const currentPlan = await gatePullRequestPlan(synchronizePlan, { githubToken: token, fetchImpl });
    assert.equal(currentPlan.kind, "project-pull-request");
    assert.equal(currentPlan.headSha, "c".repeat(40));

    current = { ...current, state: "open" };
    const staleClose = await gatePullRequestPlan(closePlan, { githubToken: token, fetchImpl });
    assert.equal(staleClose.kind, "noop");
    assert.match(staleClose.reason, /open/);

    assert.equal(requests.length, 3);
    for (const request of requests) {
        assert.equal(request.url, "https://api.github.com/repos/ronimuliawan/ontheroad/pulls/42");
        assert.equal(request.init.redirect, "error");
        assert.equal(request.init.headers.Authorization, `Bearer ${token}`);
        assert.doesNotMatch(request.url, new RegExp(token));
    }
});

test("reconciliation accepts only exact synthetic review refs", () => {
    const firstSha = "1".repeat(40);
    const secondSha = "2".repeat(40);
    assert.deepEqual(
        parseGitLabReviewRefs(
            `${secondSha}\trefs/heads/review/github-pr-99\n${firstSha}\trefs/heads/review/github-pr-7`,
            REPOSITORY,
        ),
        [
            {
                kind: "delete-review-ref",
                repository: REPOSITORY,
                sourceRef: "review/github-pr-7",
                number: 7,
                reason: "trusted reconciliation found a merged GitHub pull request",
            },
            {
                kind: "delete-review-ref",
                repository: REPOSITORY,
                sourceRef: "review/github-pr-99",
                number: 99,
                reason: "trusted reconciliation found a merged GitHub pull request",
            },
        ],
    );
    assert.throws(
        () => parseGitLabReviewRefs(`${firstSha}\trefs/heads/dev`, REPOSITORY),
        /unexpected GitLab review ref/,
    );
});

test("input validators reject malformed boundaries", () => {
    assert.throws(() => validateSha("abc"), /full 40-hex commit SHA/);
    assert.throws(() => validateBranch("master"), /dev or main/);
    assert.throws(() => validateRepository("../owner/repository"), /owner\/name/);
    assert.throws(() => validateGitLabUrl("https://gitlab.com/ronimuliawan/ontheroad.git"), /credential-free/);
    assert.throws(() => validateGitLabApiUrl("https://example.test/api/v4"), /https:\/\/gitlab.com\/api\/v4/);
    assert.throws(() => validateGitLabProject("someone/else"), /ronimuliawan\/ontheroad/);
});
