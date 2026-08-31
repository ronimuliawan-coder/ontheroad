#!/usr/bin/env node

/**
 * Projects trusted GitHub refs into a GitLab review-only repository.
 *
 * Pull-request commits are fetched as opaque Git objects and pushed to a
 * deterministic synthetic branch. This script never checks out, imports,
 * builds, sources, tests, traverses, or otherwise executes pull-request content.
 * All Git commands use argument arrays rather than a shell.
 */

import { spawnSync } from "node:child_process";
import { mkdtemp, mkdir, readFile, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import { pathToFileURL } from "node:url";

const ALLOWED_BRANCHES = new Set(["dev", "main"]);
const ACTIVE_PULL_REQUEST_ACTIONS = new Set([
    "opened",
    "synchronize",
    "reopened",
    "ready_for_review",
    "converted_to_draft",
]);
const SHA_PATTERN = /^[0-9a-f]{40}$/i;
const REPOSITORY_SEGMENT = "(?!(?:\\.{1,2})(?:/|$))[A-Za-z0-9_.-]+";
const REPOSITORY_PATTERN = new RegExp(`^${REPOSITORY_SEGMENT}/${REPOSITORY_SEGMENT}$`);
const GITLAB_SSH_PATTERN = /^git@gitlab\.com:[A-Za-z0-9_.-]+\/[A-Za-z0-9_.-]+\.git$/;
const GITLAB_API_URL = "https://gitlab.com/api/v4";
const GITLAB_PROJECT = "ronimuliawan/ontheroad";
const GITLAB_API_REQUEST_TIMEOUT_MS = 10_000;
const GITHUB_API_URL = "https://api.github.com";
const GITHUB_GRAPHQL_API_URL = `${GITHUB_API_URL}/graphql`;
const GITHUB_API_VERSION = "2026-03-10";
const GITHUB_API_REQUEST_TIMEOUT_MS = 10_000;
const SAFE_TAG_PATTERN = /^(?:v\d+\.\d+\.\d+(?:[-+][0-9A-Za-z.-]+)?|release-[0-9A-Za-z._-]+)$/;
const REVIEW_BRANCH_PREFIX = "review/github-pr-";
const REVIEW_NUMBER_PATTERN_SOURCE = "([1-9]\\d*)";
const REVIEW_BRANCH_PATTERN = new RegExp(`^${REVIEW_BRANCH_PREFIX}${REVIEW_NUMBER_PATTERN_SOURCE}$`);
const REVIEW_REF_PATTERN = new RegExp(`^refs/heads/${REVIEW_BRANCH_PREFIX}${REVIEW_NUMBER_PATTERN_SOURCE}$`);
const MAX_RECONCILED_REVIEW_REFS = 100;
const MAX_GITLAB_MERGE_REQUEST_PAGES = 10;
const RECONCILE_MUTATION_CONCURRENCY = 4;
const GIT_COMMAND_MAX_BUFFER_BYTES = 1024 * 1024;

function fail(message) {
    throw new Error(`gitlab-review-replica: ${message}`);
}

export function validateSha(value, label = "SHA") {
    if (typeof value !== "string" || !SHA_PATTERN.test(value)) fail(`${label} must be a full 40-hex commit SHA`);
    return value.toLowerCase();
}

export function validateRepository(value) {
    if (typeof value !== "string" || !REPOSITORY_PATTERN.test(value)) {
        fail("GitHub repository must have the form owner/name");
    }
    return value;
}

export function validateGitLabUrl(value) {
    if (typeof value !== "string" || !GITLAB_SSH_PATTERN.test(value)) {
        fail("GitLab replica URL must be a credential-free git@gitlab.com:owner/name.git URL");
    }
    return value;
}

export function validateGitLabApiUrl(value) {
    if (value !== GITLAB_API_URL) {
        fail(`GitLab API URL must be exactly ${GITLAB_API_URL}`);
    }
    return value;
}

export function validateGitLabProject(value) {
    if (value !== GITLAB_PROJECT) {
        fail(`GitLab project must be exactly ${GITLAB_PROJECT}`);
    }
    return value;
}

function validateApiToken(value) {
    if (typeof value !== "string" || value.length === 0 || value.length > 512 || /[\r\n]/.test(value)) {
        fail("GITLAB_REPLICA_API_TOKEN is not configured correctly");
    }
    return value;
}

function validateGitHubToken(value) {
    if (typeof value !== "string" || value.length === 0 || value.length > 512 || /[\r\n]/.test(value)) {
        fail("GITHUB_TOKEN is not configured correctly");
    }
    return value;
}

function validateApiRequestTimeout(value) {
    if (!Number.isSafeInteger(value) || value <= 0 || value > GITLAB_API_REQUEST_TIMEOUT_MS) {
        fail(`GitLab API request timeout must be between 1 and ${GITLAB_API_REQUEST_TIMEOUT_MS} milliseconds`);
    }
    return value;
}

function validateGitHubApiRequestTimeout(value) {
    if (!Number.isSafeInteger(value) || value <= 0 || value > GITHUB_API_REQUEST_TIMEOUT_MS) {
        fail(`GitHub API request timeout must be between 1 and ${GITHUB_API_REQUEST_TIMEOUT_MS} milliseconds`);
    }
    return value;
}

export function validatePullRequestNumber(value) {
    if (!Number.isSafeInteger(value) || value <= 0) fail("pull request number must be a positive integer");
    return value;
}

export function validateBranch(value) {
    if (typeof value !== "string" || !ALLOWED_BRANCHES.has(value)) {
        fail("target branch must be dev or main");
    }
    return value;
}

export function reviewRef(number) {
    return `${REVIEW_BRANCH_PREFIX}${validatePullRequestNumber(number)}`;
}

export function parseGitLabReviewRefs(output, repository) {
    const canonicalRepository = validateRepository(repository);
    if (typeof output !== "string") fail("GitLab review-ref listing must be text");
    if (Buffer.byteLength(output, "utf8") > GIT_COMMAND_MAX_BUFFER_BYTES) {
        fail(`GitLab review-ref listing exceeded ${GIT_COMMAND_MAX_BUFFER_BYTES} bytes`);
    }
    if (output === "") return [];

    const plans = [];
    let refCount = 0;
    let lineStart = 0;
    while (lineStart < output.length) {
        const newline = output.indexOf("\n", lineStart);
        const lineEnd = newline === -1 ? output.length : newline;
        const rawLine = output.slice(lineStart, lineEnd);
        const line = rawLine.endsWith("\r") ? rawLine.slice(0, -1) : rawLine;
        if (line === "") fail("unexpected GitLab review-ref listing response");
        const [sha, fullRef, ...extra] = line.split(/\s+/);
        if (extra.length > 0 || !sha || !fullRef) fail("unexpected GitLab review-ref listing response");
        validateSha(sha, `${fullRef} SHA`);
        const match = REVIEW_REF_PATTERN.exec(fullRef);
        if (!match) fail(`unexpected GitLab review ref ${fullRef}`);
        const number = validatePullRequestNumber(Number(match[1]));
        refCount += 1;

        let low = 0;
        let high = plans.length;
        while (low < high) {
            const middle = Math.floor((low + high) / 2);
            if (plans[middle].number < number) low = middle + 1;
            else high = middle;
        }
        if (plans[low]?.number === number) {
            fail(`GitLab returned duplicate review ref for pull request ${number}`);
        }
        if (low < MAX_RECONCILED_REVIEW_REFS) {
            plans.splice(low, 0, {
                kind: "delete-review-ref",
                repository: canonicalRepository,
                sourceRef: reviewRef(number),
                number,
                reason: "trusted reconciliation found a merged GitHub pull request",
            });
            if (plans.length > MAX_RECONCILED_REVIEW_REFS) plans.pop();
        }
        lineStart = newline === -1 ? output.length : newline + 1;
    }
    if (refCount > MAX_RECONCILED_REVIEW_REFS) {
        console.log(
            `gitlab-review-replica: processing the first ${MAX_RECONCILED_REVIEW_REFS} of ${refCount} validated review refs`,
        );
    }
    return plans;
}

export function githubRepositoryUrl(repository) {
    return `https://github.com/${validateRepository(repository)}.git`;
}

export function githubPullRequestApiUrl(repository, number) {
    return `${GITHUB_API_URL}/repos/${validateRepository(repository)}/pulls/${validatePullRequestNumber(number)}`;
}

export function gitLabReviewBranchApiUrl(apiUrl, project, number) {
    const base = validateGitLabApiUrl(apiUrl);
    const projectPath = encodeURIComponent(validateGitLabProject(project));
    const branch = encodeURIComponent(reviewRef(number));
    return `${base}/projects/${projectPath}/repository/branches/${branch}`;
}

export function gitLabOpenReviewMergeRequestsApiUrl(apiUrl, project, number) {
    const base = validateGitLabApiUrl(apiUrl);
    const projectPath = encodeURIComponent(validateGitLabProject(project));
    const sourceRef = encodeURIComponent(reviewRef(number));
    return `${base}/projects/${projectPath}/merge_requests?state=opened&source_branch=${sourceRef}`;
}

export function gitLabOpenMergeRequestsPageApiUrl(apiUrl, project, page) {
    const base = validateGitLabApiUrl(apiUrl);
    const projectPath = encodeURIComponent(validateGitLabProject(project));
    if (!Number.isSafeInteger(page) || page <= 0 || page > MAX_GITLAB_MERGE_REQUEST_PAGES) {
        fail(`GitLab merge-request page must be between 1 and ${MAX_GITLAB_MERGE_REQUEST_PAGES}`);
    }
    return `${base}/projects/${projectPath}/merge_requests?state=opened&per_page=100&page=${page}`;
}

/** Keeps a GitLab-provided next link inside the token's exact API boundary. */
function validateGitLabMergeRequestsPageLink(value, apiUrl, project, page) {
    let actual;
    try {
        actual = new URL(value);
    } catch {
        fail("GitLab open merge-request listing returned invalid pagination");
    }
    const expected = new URL(gitLabOpenMergeRequestsPageApiUrl(apiUrl, project, page));
    const sortedParams = (url) => [...url.searchParams.entries()]
        .sort(([leftKey, leftValue], [rightKey, rightValue]) => (
            leftKey.localeCompare(rightKey) || leftValue.localeCompare(rightValue)
        ));
    if (
        actual.username !== ""
        || actual.password !== ""
        || actual.hash !== ""
        || actual.origin !== expected.origin
        || actual.pathname !== expected.pathname
        || JSON.stringify(sortedParams(actual)) !== JSON.stringify(sortedParams(expected))
    ) {
        fail("GitLab open merge-request listing returned invalid pagination");
    }
    return actual.href;
}

/** Resolves a canonical next page when either GitLab pagination header advertises one. */
function gitLabNextMergeRequestsPageUrl(response, apiUrl, project, page) {
    const nextPageHeader = response.headers.get("x-next-page") ?? "";
    let headerHasNextPage = false;
    if (nextPageHeader !== "") {
        const nextPage = Number(nextPageHeader);
        if (!Number.isSafeInteger(nextPage) || nextPage !== page + 1) {
            fail("GitLab open merge-request listing returned invalid pagination");
        }
        headerHasNextPage = true;
    }

    const linkHeader = response.headers.get("link") ?? "";
    const nextLinks = [];
    if (linkHeader !== "") {
        for (const segment of linkHeader.split(/,\s*(?=<)/)) {
            const link = /^\s*<([^<>]+)>(.*)$/.exec(segment);
            if (!link) fail("GitLab open merge-request listing returned invalid pagination");
            const relation = /(?:^|;)\s*rel\s*=\s*(?:"([^"]+)"|([^;,\s]+))/i.exec(link[2]);
            const relations = (relation?.[1] ?? relation?.[2] ?? "")
                .toLowerCase()
                .split(/\s+/);
            if (relations.includes("next")) nextLinks.push(link[1]);
        }
    }
    if (nextLinks.length > 1) fail("GitLab open merge-request listing returned invalid pagination");
    if (!headerHasNextPage && nextLinks.length === 0) return null;
    if (page >= MAX_GITLAB_MERGE_REQUEST_PAGES) {
        fail(`GitLab open merge-request listing exceeded ${MAX_GITLAB_MERGE_REQUEST_PAGES} pages`);
    }
    if (nextLinks.length === 1) {
        return validateGitLabMergeRequestsPageLink(nextLinks[0], apiUrl, project, page + 1);
    }
    return gitLabOpenMergeRequestsPageApiUrl(apiUrl, project, page + 1);
}

export function gitLabMergeRequestApiUrl(apiUrl, project, iid) {
    const base = validateGitLabApiUrl(apiUrl);
    const projectPath = encodeURIComponent(validateGitLabProject(project));
    if (!Number.isSafeInteger(iid) || iid <= 0) fail("GitLab merge request IID must be a positive integer");
    return `${base}/projects/${projectPath}/merge_requests/${iid}`;
}

function pullRequestPayload(event) {
    const pullRequest = event?.pull_request;
    if (!pullRequest || typeof pullRequest !== "object") fail("pull_request payload is required");
    return pullRequest;
}

export function planEvent({ eventName, event, repository }) {
    const canonicalRepository = validateRepository(repository);

    if (eventName === "pull_request_target") {
        const pullRequest = pullRequestPayload(event);
        const number = validatePullRequestNumber(event.number ?? pullRequest.number);
        const sourceRef = reviewRef(number);
        const headRepository = pullRequest.head?.repo?.full_name;

        // Enforce the same-repository boundary in the planner as well as the
        // workflow job condition. This keeps future callers from turning a
        // forged close event into deletion of a same-number synthetic ref.
        if (headRepository !== canonicalRepository) {
            fail("fork pull requests are not eligible for the secret-bearing review projection");
        }

        if (event.action === "closed") {
            return {
                kind: "delete-review-ref",
                repository: canonicalRepository,
                sourceRef,
                number,
                reason: "GitHub pull request closed",
            };
        }

        if (!ACTIVE_PULL_REQUEST_ACTIONS.has(event.action)) {
            return {
                kind: "noop",
                repository: canonicalRepository,
                reason: `pull request action ${String(event.action)} does not change the review ref`,
            };
        }

        const targetBranch = validateBranch(pullRequest.base?.ref);
        const headSha = validateSha(pullRequest.head?.sha, "pull request head SHA");
        const baseSha = validateSha(pullRequest.base?.sha, "pull request base SHA");

        return {
            kind: "project-pull-request",
            repository: canonicalRepository,
            number,
            sourceRef,
            sourceFetchRef: `refs/pull/${number}/head`,
            targetBranch,
            targetFetchRef: `refs/heads/${targetBranch}`,
            headSha,
            baseSha,
            draft: pullRequest.draft === true,
        };
    }

    if (eventName === "push") {
        const match = /^refs\/heads\/(dev|main)$/.exec(event?.ref ?? "");
        if (!match) return { kind: "noop", repository: canonicalRepository, reason: "push ref is not dev or main" };
        const branch = validateBranch(match[1]);
        const sha = validateSha(event.after, "push SHA");
        return {
            kind: "project-canonical-ref",
            repository: canonicalRepository,
            branch,
            fetchRef: `refs/heads/${branch}`,
            sha,
        };
    }

    if (eventName === "schedule" || eventName === "workflow_dispatch") {
        return {
            kind: "reconcile",
            repository: canonicalRepository,
            branches: [...ALLOWED_BRANCHES].sort(),
            tagPattern: SAFE_TAG_PATTERN.source,
        };
    }

    return { kind: "noop", repository: canonicalRepository, reason: `unsupported event ${String(eventName)}` };
}

async function fetchCurrentPullRequest({
    repository,
    number,
    githubToken,
    githubApiRequestTimeoutMs = GITHUB_API_REQUEST_TIMEOUT_MS,
    fetchImpl = globalThis.fetch,
}) {
    if (typeof fetchImpl !== "function") fail("Fetch API is unavailable");
    const canonicalRepository = validateRepository(repository);
    const pullRequestNumber = validatePullRequestNumber(number);
    const token = validateGitHubToken(githubToken);
    const requestTimeoutMs = validateGitHubApiRequestTimeout(githubApiRequestTimeoutMs);
    const url = githubPullRequestApiUrl(canonicalRepository, pullRequestNumber);
    let response;
    try {
        response = await fetchImpl(url, {
            method: "GET",
            headers: {
                Accept: "application/vnd.github+json",
                Authorization: `Bearer ${token}`,
                "X-GitHub-Api-Version": GITHUB_API_VERSION,
            },
            redirect: "error",
            signal: AbortSignal.timeout(requestTimeoutMs),
        });
    } catch (error) {
        if (error?.name === "AbortError" || error?.name === "TimeoutError") {
            fail("GitHub pull-request state lookup timed out");
        }
        fail("GitHub pull-request state lookup failed");
    }
    if (response.status !== 200) fail(`GitHub pull-request state lookup returned HTTP ${response.status}`);

    let currentPullRequest;
    try {
        currentPullRequest = await response.json();
    } catch (error) {
        if (error?.name === "AbortError" || error?.name === "TimeoutError") {
            fail("GitHub pull-request state response timed out");
        }
        fail("GitHub pull-request state lookup returned invalid JSON");
    }
    if (!currentPullRequest || typeof currentPullRequest !== "object") {
        fail("GitHub pull-request state lookup returned an invalid response");
    }
    if (validatePullRequestNumber(currentPullRequest.number) !== pullRequestNumber) {
        fail("GitHub pull-request state lookup returned the wrong pull request");
    }
    if (currentPullRequest.state !== "open" && currentPullRequest.state !== "closed") {
        fail("GitHub pull-request state lookup returned an invalid state");
    }
    return currentPullRequest;
}

/**
 * Resolves every reconciled PR in one shallow GraphQL query. All nodes are
 * validated before cleanup begins, so a partial or inconsistent response
 * cannot cause a subset of refs to be deleted on unverified state.
 */
async function fetchReconciliationPullRequests(plans, {
    githubToken,
    githubApiRequestTimeoutMs = GITHUB_API_REQUEST_TIMEOUT_MS,
    fetchImpl = globalThis.fetch,
}) {
    if (typeof fetchImpl !== "function") fail("Fetch API is unavailable");
    if (plans.length === 0) return new Map();
    const repository = validateRepository(plans[0].repository);
    const [owner, name] = repository.split("/");
    const token = validateGitHubToken(githubToken);
    const requestTimeoutMs = validateGitHubApiRequestTimeout(githubApiRequestTimeoutMs);
    const selections = plans.map((plan, index) => {
        const number = validatePullRequestNumber(plan.number);
        if (validateRepository(plan.repository) !== repository) {
            fail("all reconciled review refs must belong to the same GitHub repository");
        }
        return `pr${index}: pullRequest(number: ${number}) { number state merged mergedAt headRepository { nameWithOwner } }`;
    });
    const query = `query ReviewRefReconciliation { repository(owner: ${JSON.stringify(owner)}, name: ${JSON.stringify(name)}) { ${selections.join(" ")} } }`;

    let response;
    try {
        response = await fetchImpl(GITHUB_GRAPHQL_API_URL, {
            method: "POST",
            headers: {
                Accept: "application/vnd.github+json",
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json",
                "X-GitHub-Api-Version": GITHUB_API_VERSION,
            },
            body: JSON.stringify({ query }),
            redirect: "error",
            signal: AbortSignal.timeout(requestTimeoutMs),
        });
    } catch (error) {
        if (error?.name === "AbortError" || error?.name === "TimeoutError") {
            fail("GitHub pull-request state lookup timed out");
        }
        fail("GitHub pull-request state lookup failed");
    }
    if (response.status !== 200) fail(`GitHub pull-request state lookup returned HTTP ${response.status}`);

    let payload;
    try {
        payload = await response.json();
    } catch (error) {
        if (error?.name === "AbortError" || error?.name === "TimeoutError") {
            fail("GitHub pull-request state response timed out");
        }
        fail("GitHub pull-request state lookup returned invalid JSON");
    }
    if (!payload || typeof payload !== "object" || payload.errors !== undefined) {
        fail("GitHub pull-request state lookup returned GraphQL errors");
    }
    const nodes = payload.data?.repository;
    if (!nodes || typeof nodes !== "object") fail("GitHub pull-request state lookup returned an invalid response");

    const states = new Map();
    for (const [index, plan] of plans.entries()) {
        const node = nodes[`pr${index}`];
        const number = validatePullRequestNumber(plan.number);
        if (!node || typeof node !== "object" || validatePullRequestNumber(node.number) !== number) {
            fail(`GitHub pull-request state lookup returned an invalid node for pull request ${number}`);
        }
        if (node.headRepository?.nameWithOwner !== repository) {
            fail(`GitHub pull request ${number} is no longer owned by the canonical repository`);
        }
        const merged = node.state === "MERGED" && node.merged === true;
        const reopenable = (node.state === "OPEN" || node.state === "CLOSED")
            && node.merged === false
            && node.mergedAt === null;
        if (!merged && !reopenable) fail(`GitHub pull request ${number} returned an inconsistent merge state`);
        if (merged && (typeof node.mergedAt !== "string" || Number.isNaN(Date.parse(node.mergedAt)))) {
            fail(`GitHub pull request ${number} returned an invalid mergedAt value`);
        }
        states.set(number, { merged, state: node.state });
    }
    return states;
}

export async function gatePullRequestPlan(plan, {
    githubToken,
    githubApiRequestTimeoutMs = GITHUB_API_REQUEST_TIMEOUT_MS,
    fetchImpl = globalThis.fetch,
}) {
    if (plan.kind !== "project-pull-request" && plan.kind !== "delete-review-ref") return plan;
    const repository = validateRepository(plan.repository);
    const number = validatePullRequestNumber(plan.number);
    const currentPullRequest = await fetchCurrentPullRequest({
        repository,
        number,
        githubToken,
        githubApiRequestTimeoutMs,
        fetchImpl,
    });

    const expectedState = plan.kind === "project-pull-request" ? "open" : "closed";
    if (currentPullRequest.state !== expectedState) {
        return {
            kind: "noop",
            repository,
            reason: `current GitHub pull request state is ${currentPullRequest.state}; skipping stale ${plan.kind} event`,
        };
    }

    // Re-plan from the authoritative response so an older queued synchronize
    // converges on the current head, target, and draft state. Per-PR workflow
    // serialization then ensures a state change after this read queues its own
    // correcting projection or cleanup behind the current run.
    return planEvent({
        eventName: "pull_request_target",
        event: {
            action: currentPullRequest.state === "closed" ? "closed" : "synchronize",
            number,
            pull_request: currentPullRequest,
        },
        repository,
    });
}

function commandDisplay(args) {
    return ["git", ...args].map((argument) => (/^[A-Za-z0-9_./:=+@-]+$/.test(argument) ? argument : JSON.stringify(argument))).join(" ");
}

function git(args, { cwd, hooksPath, allowFailure = false }) {
    const safeArgs = [
        "-c",
        "protocol.file.allow=never",
        "-c",
        `core.hooksPath=${hooksPath}`,
        "-c",
        "submodule.recurse=false",
        ...args,
    ];
    console.log(`gitlab-review-replica: ${commandDisplay(args)}`);
    const gitEnvironment = { ...process.env };
    // Reconciliation needs the cleanup token in this Node process, never in
    // Git or its helpers. Keep the broader credential out of every child.
    delete gitEnvironment.GITLAB_REPLICA_API_TOKEN;
    const result = spawnSync("git", safeArgs, {
        cwd,
        encoding: "utf8",
        maxBuffer: GIT_COMMAND_MAX_BUFFER_BYTES,
        env: {
            ...gitEnvironment,
            // A clean hosted runner is expected, but ignoring ambient Git
            // configuration also prevents URL rewrites, credential helpers,
            // filters, or system attributes from widening this boundary.
            GIT_CONFIG_GLOBAL: "/dev/null",
            GIT_CONFIG_NOSYSTEM: "1",
            GIT_ATTR_NOSYSTEM: "1",
            GIT_TERMINAL_PROMPT: "0",
            GIT_LFS_SKIP_SMUDGE: "1",
        },
        stdio: ["ignore", "pipe", "pipe"],
    });
    if (result.error?.code === "ENOBUFS") {
        fail(`Git command output exceeded ${GIT_COMMAND_MAX_BUFFER_BYTES} bytes`);
    }
    if (result.error) fail(`could not start Git: ${result.error.message}`);
    if (result.status !== 0 && !allowFailure) {
        const stderr = result.stderr.trim();
        fail(`Git command failed (${commandDisplay(args)}): ${stderr || `exit ${result.status}`}`);
    }
    return result;
}

function remoteSha(remote, fullRef, context) {
    const result = git(["ls-remote", "--refs", remote, fullRef], context);
    const line = result.stdout.trim();
    if (line === "") return null;
    const [sha, returnedRef, ...extra] = line.split(/\s+/);
    if (extra.length > 0 || returnedRef !== fullRef) fail(`unexpected ls-remote response for ${fullRef}`);
    return validateSha(sha, `${remote} ${fullRef} SHA`);
}

function fetchAndVerify(fetchRef, expectedSha, context, { tags = false } = {}) {
    const args = ["fetch", "--no-recurse-submodules"];
    if (!tags) args.push("--no-tags");
    args.push("origin", fetchRef);
    git(args, context);
    const fetched = git(["rev-parse", "FETCH_HEAD"], context).stdout.trim().toLowerCase();
    if (fetched !== expectedSha) fail(`fetched ${fetchRef} at ${fetched}, expected ${expectedSha}`);
}

function assertFastForward(remoteOldSha, newSha, fullRef, context) {
    if (!remoteOldSha || remoteOldSha === newSha) return;
    const result = git(["merge-base", "--is-ancestor", remoteOldSha, newSha], { ...context, allowFailure: true });
    if (result.status !== 0) fail(`${fullRef} would diverge on GitLab; refusing to force the canonical branch`);
}

function syncCanonicalBranch(branch, expectedSha, fetchRef, context) {
    const fullRef = `refs/heads/${validateBranch(branch)}`;
    const sha = validateSha(expectedSha, `${branch} SHA`);
    fetchAndVerify(fetchRef, sha, context);
    const remoteOld = remoteSha("gitlab", fullRef, context);
    if (remoteOld === sha) {
        console.log(`gitlab-review-replica: ${fullRef} already matches ${sha}`);
        return;
    }
    assertFastForward(remoteOld, sha, fullRef, context);
    git(["push", "-o", "ci.skip", "gitlab", `FETCH_HEAD:${fullRef}`], context);
    const projected = remoteSha("gitlab", fullRef, context);
    if (projected !== sha) fail(`${fullRef} parity check failed after push`);
}

function projectPullRequest(plan, context) {
    // The base SHA in a pull_request event is useful validation evidence, but
    // another trusted push can advance the canonical branch before this job
    // starts. Resolve the branch again at execution time so an older PR event
    // can never rewind the GitLab projection or fail solely because of ordering.
    const currentTargetSha = remoteSha("origin", plan.targetFetchRef, context);
    if (currentTargetSha === null) fail(`GitHub ${plan.targetFetchRef} does not exist`);
    syncCanonicalBranch(plan.targetBranch, currentTargetSha, plan.targetFetchRef, context);
    fetchAndVerify(plan.sourceFetchRef, plan.headSha, context);

    const fullRef = `refs/heads/${plan.sourceRef}`;
    const remoteOld = remoteSha("gitlab", fullRef, context);
    if (remoteOld === plan.headSha) {
        console.log(`gitlab-review-replica: ${fullRef} already matches ${plan.headSha}`);
        return;
    }

    const args = [
        "push",
        "-o",
        "ci.skip",
        `--force-with-lease=${fullRef}:${remoteOld ?? ""}`,
    ];
    if (remoteOld === null) {
        args.push(
            "-o",
            "merge_request.create",
            "-o",
            `merge_request.target=${plan.targetBranch}`,
            "-o",
            `merge_request.title=GitHub PR #${plan.number} review replica`,
            "-o",
            `merge_request.description=Review-only projection of https://github.com/${plan.repository}/pull/${plan.number}. Do not merge on GitLab.`,
        );
    }
    args.push("gitlab", `FETCH_HEAD:${fullRef}`);
    git(args, context);

    const projected = remoteSha("gitlab", fullRef, context);
    if (projected !== plan.headSha) fail(`${fullRef} parity check failed after push`);
}

/**
 * Loads open review MRs through bounded pagination before any mutation. This
 * replaces one lookup per ref and detects duplicate MRs before cleanup starts.
 */
async function listOpenGitLabReviewMergeRequests({
    gitlabApiUrl,
    gitlabProject,
    gitlabApiToken,
    deletionNumbers,
    gitlabApiRequestTimeoutMs = GITLAB_API_REQUEST_TIMEOUT_MS,
    fetchImpl = globalThis.fetch,
}) {
    if (typeof fetchImpl !== "function") fail("Fetch API is unavailable");
    if (!(deletionNumbers instanceof Set)) fail("GitLab merge-request deletion targets must be a Set");
    for (const number of deletionNumbers) validatePullRequestNumber(number);
    const token = validateApiToken(gitlabApiToken);
    const headers = { "PRIVATE-TOKEN": token };
    const requestTimeoutMs = validateApiRequestTimeout(gitlabApiRequestTimeoutMs);
    const mergeRequests = new Map();
    let url = gitLabOpenMergeRequestsPageApiUrl(gitlabApiUrl, gitlabProject, 1);

    for (let page = 1; page <= MAX_GITLAB_MERGE_REQUEST_PAGES; page += 1) {
        let response;
        try {
            response = await fetchImpl(url, {
                method: "GET",
                headers,
                redirect: "error",
                signal: AbortSignal.timeout(requestTimeoutMs),
            });
        } catch (error) {
            if (error?.name === "AbortError" || error?.name === "TimeoutError") {
                fail("GitLab open merge-request listing timed out");
            }
            fail("GitLab open merge-request listing failed");
        }
        if (response.status !== 200) fail(`GitLab open merge-request listing returned HTTP ${response.status}`);

        let entries;
        try {
            entries = await response.json();
        } catch (error) {
            if (error?.name === "AbortError" || error?.name === "TimeoutError") {
                fail("GitLab open merge-request listing response timeout");
            }
            fail("GitLab open merge-request listing returned invalid JSON");
        }
        if (!Array.isArray(entries)) fail("GitLab open merge-request listing did not return an array");
        for (const entry of entries) {
            if (!entry || typeof entry !== "object" || typeof entry.source_branch !== "string") {
                fail("GitLab open merge-request listing returned an invalid entry");
            }
            if (!entry.source_branch.startsWith(REVIEW_BRANCH_PREFIX)) continue;
            const match = REVIEW_BRANCH_PATTERN.exec(entry.source_branch);
            if (!match) fail(`unexpected GitLab review merge-request source ${entry.source_branch}`);
            const number = validatePullRequestNumber(Number(match[1]));
            if (!deletionNumbers.has(number)) continue;
            if (entry.state !== "opened") fail(`GitLab merge request for ${entry.source_branch} is not open`);
            validateBranch(entry.target_branch);
            if (!Number.isSafeInteger(entry.iid) || entry.iid <= 0) fail("GitLab merge request IID must be a positive integer");
            if (mergeRequests.has(number)) fail(`GitLab returned multiple open merge requests for ${entry.source_branch}`);
            mergeRequests.set(number, entry);
        }

        const nextUrl = gitLabNextMergeRequestsPageUrl(response, gitlabApiUrl, gitlabProject, page);
        if (nextUrl === null) return mergeRequests;
        url = nextUrl;
    }
    fail(`GitLab open merge-request listing exceeded ${MAX_GITLAB_MERGE_REQUEST_PAGES} pages`);
}

async function mapWithConcurrency(items, concurrency, worker) {
    if (!Number.isSafeInteger(concurrency) || concurrency <= 0) fail("mutation concurrency must be a positive integer");
    const results = new Array(items.length);
    let nextIndex = 0;
    let firstError;
    const runWorker = async () => {
        while (firstError === undefined) {
            const index = nextIndex;
            nextIndex += 1;
            if (index >= items.length) return;
            try {
                results[index] = await worker(items[index], index);
            } catch (error) {
                firstError = error;
            }
        }
    };
    await Promise.all(Array.from(
        { length: Math.min(concurrency, items.length) },
        () => runWorker(),
    ));
    if (firstError !== undefined) throw firstError;
    return results;
}

export async function deleteReviewRefViaApi(plan, {
    gitlabApiUrl,
    gitlabProject,
    gitlabApiToken,
    mergeRequests: preloadedMergeRequests = undefined,
    gitlabApiRequestTimeoutMs = GITLAB_API_REQUEST_TIMEOUT_MS,
    fetchImpl = globalThis.fetch,
}) {
    if (plan.kind !== "delete-review-ref") fail("only a delete-review-ref plan may use the GitLab API");
    const expectedRef = reviewRef(plan.number);
    if (plan.sourceRef !== expectedRef) fail("review branch does not match the pull request number");
    if (typeof fetchImpl !== "function") fail("Fetch API is unavailable");
    const requestTimeoutMs = validateApiRequestTimeout(gitlabApiRequestTimeoutMs);

    const token = validateApiToken(gitlabApiToken);
    const headers = { "PRIVATE-TOKEN": token };
    let mergeRequests;
    if (preloadedMergeRequests === undefined) {
        const listUrl = gitLabOpenReviewMergeRequestsApiUrl(gitlabApiUrl, gitlabProject, plan.number);
        const listResponse = await fetchImpl(listUrl, {
            method: "GET",
            headers,
            redirect: "error",
            signal: AbortSignal.timeout(requestTimeoutMs),
        });
        if (listResponse.status !== 200) {
            fail(`GitLab review merge-request lookup returned HTTP ${listResponse.status}`);
        }
        try {
            mergeRequests = await listResponse.json();
        } catch (error) {
            if (error?.name === "AbortError" || error?.name === "TimeoutError") {
                fail("GitLab review merge-request lookup response timeout");
            }
            fail("GitLab review merge-request lookup returned invalid JSON");
        }
    } else {
        mergeRequests = preloadedMergeRequests;
    }
    if (!Array.isArray(mergeRequests)) fail("GitLab review merge-request lookup did not return an array");
    if (mergeRequests.length > 1) fail(`GitLab returned multiple open merge requests for ${expectedRef}`);

    if (mergeRequests.length === 1) {
        const mergeRequest = mergeRequests[0];
        if (!mergeRequest || typeof mergeRequest !== "object") {
            fail("GitLab review merge-request lookup returned an invalid entry");
        }
        if (mergeRequest.source_branch !== expectedRef) {
            fail("GitLab review merge-request source branch does not match the deterministic review ref");
        }
        // GitLab fixes the MR target when the synthetic ref is first created,
        // while GitHub may later retarget the PR. Cleanup identity is the exact
        // PR-number ref, but the stored target must still be governed.
        validateBranch(mergeRequest.target_branch);
        if (mergeRequest.state !== "opened") fail("GitLab review merge request is not open");

        const closeUrl = gitLabMergeRequestApiUrl(gitlabApiUrl, gitlabProject, mergeRequest.iid);
        const closeResponse = await fetchImpl(closeUrl, {
            method: "PUT",
            headers: { ...headers, "Content-Type": "application/json" },
            body: JSON.stringify({ state_event: "close" }),
            redirect: "error",
            signal: AbortSignal.timeout(requestTimeoutMs),
        });
        if (closeResponse.status !== 200) {
            fail(`GitLab review merge-request close returned HTTP ${closeResponse.status}`);
        }
        console.log(`gitlab-review-replica: closed GitLab MR !${mergeRequest.iid} for ${expectedRef}`);
    }

    const branchUrl = gitLabReviewBranchApiUrl(gitlabApiUrl, gitlabProject, plan.number);
    const response = await fetchImpl(branchUrl, {
        method: "DELETE",
        headers,
        redirect: "error",
        signal: AbortSignal.timeout(requestTimeoutMs),
    });

    if (response.status === 204) {
        console.log(`gitlab-review-replica: deleted protected ${expectedRef} through the GitLab API`);
        return "deleted";
    }
    if (response.status === 404) {
        console.log(`gitlab-review-replica: protected ${expectedRef} is already absent`);
        return "absent";
    }
    fail(`GitLab protected review-branch deletion returned HTTP ${response.status}`);
}

export async function reconcileReviewRefs(plans, {
    githubToken,
    gitlabApiUrl,
    gitlabProject,
    gitlabApiToken,
    fetchImpl = globalThis.fetch,
}) {
    if (!Array.isArray(plans)) fail("review-ref reconciliation plans must be an array");
    if (plans.length > MAX_RECONCILED_REVIEW_REFS) {
        fail(`review-ref reconciliation is limited to ${MAX_RECONCILED_REVIEW_REFS} refs`);
    }
    const numbers = new Set();
    for (const plan of plans) {
        if (plan?.kind !== "delete-review-ref") fail("reconciliation accepts only delete-review-ref plans");
        validateRepository(plan.repository);
        const number = validatePullRequestNumber(plan.number);
        if (plan.sourceRef !== reviewRef(number)) fail("review branch does not match the pull request number");
        if (numbers.has(number)) fail(`reconciliation received duplicate plan for pull request ${number}`);
        numbers.add(number);
    }

    const states = await fetchReconciliationPullRequests(plans, { githubToken, fetchImpl });
    const results = new Array(plans.length);
    const deletions = [];
    for (const [index, plan] of plans.entries()) {
        const state = states.get(plan.number);
        if (!state) fail(`GitHub state is missing for pull request ${plan.number}`);
        // A merged PR cannot reopen, so deletion cannot race a concurrent
        // reopen projection. Closed-unmerged refs remain recoverable by their
        // normal per-PR close/reopen event stream and are preserved here.
        if (!state.merged) {
            const reason = state.state === "OPEN" ? "open" : "closed but unmerged";
            console.log(`gitlab-review-replica: preserved ${reviewRef(plan.number)} because its GitHub pull request is ${reason}`);
            results[index] = { number: plan.number, result: "preserved" };
            continue;
        }
        deletions.push({ index, plan });
    }
    if (deletions.length === 0) return results;

    const openMergeRequests = await listOpenGitLabReviewMergeRequests({
        gitlabApiUrl,
        gitlabProject,
        gitlabApiToken,
        deletionNumbers: new Set(deletions.map(({ plan }) => plan.number)),
        fetchImpl,
    });
    await mapWithConcurrency(deletions, RECONCILE_MUTATION_CONCURRENCY, async ({ index, plan }) => {
        const mergeRequest = openMergeRequests.get(plan.number);
        const result = await deleteReviewRefViaApi(plan, {
            gitlabApiUrl,
            gitlabProject,
            gitlabApiToken,
            fetchImpl,
            mergeRequests: mergeRequest ? [mergeRequest] : [],
        });
        results[index] = { number: plan.number, result };
    });
    return results;
}

function listGitLabReviewRefPlans(repository, context) {
    const result = git(["ls-remote", "--refs", "gitlab", "refs/heads/review/github-pr-*"], context);
    return parseGitLabReviewRefs(result.stdout, repository);
}

function listOriginTags(context) {
    const result = git(["ls-remote", "--tags", "--refs", "origin", "refs/tags/*"], context);
    const tags = [];
    for (const line of result.stdout.trim().split(/\r?\n/)) {
        if (!line) continue;
        const [sha, fullRef, ...extra] = line.split(/\s+/);
        if (extra.length > 0 || !fullRef?.startsWith("refs/tags/")) fail("unexpected tag ls-remote response");
        const tag = fullRef.slice("refs/tags/".length);
        if (!SAFE_TAG_PATTERN.test(tag)) continue;
        tags.push({ tag, fullRef, sha: validateSha(sha, `${tag} SHA`) });
    }
    return tags.sort((left, right) => (left.tag < right.tag ? -1 : left.tag > right.tag ? 1 : 0));
}

function syncTag(tag, fullRef, sha, context) {
    const remoteOld = remoteSha("gitlab", fullRef, context);
    if (remoteOld === sha) return;
    if (remoteOld !== null) fail(`${fullRef} differs on GitLab; refusing to rewrite a tag`);
    fetchAndVerify(fullRef, sha, context, { tags: true });
    git(["push", "-o", "ci.skip", "gitlab", `FETCH_HEAD:${fullRef}`], context);
    if (remoteSha("gitlab", fullRef, context) !== sha) fail(`${fullRef} parity check failed after push`);
    console.log(`gitlab-review-replica: projected tag ${tag}`);
}

async function executePlan(plan, {
    githubToken,
    gitlabUrl,
    gitlabApiUrl,
    gitlabProject,
    gitlabApiToken,
}) {
    plan = await gatePullRequestPlan(plan, { githubToken });
    if (plan.kind === "noop") {
        console.log(`gitlab-review-replica: no-op: ${plan.reason}`);
        return;
    }

    // Git refuses deletion of a protected branch even when the deploy key may
    // update it. Keep the broader API credential out of every other operation.
    if (plan.kind === "delete-review-ref") {
        await deleteReviewRefViaApi(plan, { gitlabApiUrl, gitlabProject, gitlabApiToken });
        return;
    }

    const replicaUrl = validateGitLabUrl(gitlabUrl);
    const root = await mkdtemp(path.join(tmpdir(), "ontheroad-gitlab-replica-"));
    const repositoryPath = path.join(root, "repository");
    const hooksPath = path.join(root, "disabled-hooks");
    await mkdir(hooksPath, { recursive: true });
    const context = { cwd: repositoryPath, hooksPath };

    try {
        git(["init", repositoryPath], { cwd: root, hooksPath });
        git(["remote", "add", "origin", githubRepositoryUrl(plan.repository)], context);
        git(["remote", "add", "gitlab", replicaUrl], context);

        if (plan.kind === "project-canonical-ref") {
            syncCanonicalBranch(plan.branch, plan.sha, plan.fetchRef, context);
        } else if (plan.kind === "project-pull-request") {
            projectPullRequest(plan, context);
        } else if (plan.kind === "reconcile") {
            for (const branch of plan.branches) {
                const fullRef = `refs/heads/${validateBranch(branch)}`;
                const sha = remoteSha("origin", fullRef, context);
                if (sha === null) fail(`GitHub ${fullRef} does not exist`);
                syncCanonicalBranch(branch, sha, fullRef, context);
            }
            for (const tag of listOriginTags(context)) syncTag(tag.tag, tag.fullRef, tag.sha, context);
            await reconcileReviewRefs(listGitLabReviewRefPlans(plan.repository, context), {
                githubToken,
                gitlabApiUrl,
                gitlabProject,
                gitlabApiToken,
            });
        } else {
            fail(`unsupported operation ${String(plan.kind)}`);
        }
    } finally {
        await rm(root, { recursive: true, force: true });
    }
}

function parseArguments(argv) {
    const options = { dryRun: false };
    for (let index = 0; index < argv.length; index += 1) {
        const argument = argv[index];
        if (argument === "--dry-run") options.dryRun = true;
        else if (argument === "--event-file") options.eventFile = argv[++index];
        else if (argument === "--event-name") options.eventName = argv[++index];
        else if (argument === "--repository") options.repository = argv[++index];
        else if (argument === "--gitlab-url") options.gitlabUrl = argv[++index];
        else if (argument === "--gitlab-api-url") options.gitlabApiUrl = argv[++index];
        else if (argument === "--gitlab-project") options.gitlabProject = argv[++index];
        else fail(`unknown argument ${argument}`);
    }
    for (const name of ["eventFile", "eventName", "repository"]) {
        if (!options[name]) fail(`--${name.replace(/[A-Z]/g, (letter) => `-${letter.toLowerCase()}`)} is required`);
    }
    if (!options.dryRun) {
        if (!options.gitlabUrl) fail("--gitlab-url is required unless --dry-run is used");
        if (!options.gitlabApiUrl) fail("--gitlab-api-url is required unless --dry-run is used");
        if (!options.gitlabProject) fail("--gitlab-project is required unless --dry-run is used");
    }
    return options;
}

async function main() {
    try {
        const options = parseArguments(process.argv.slice(2));
        const event = JSON.parse(await readFile(options.eventFile, "utf8"));
        const plan = planEvent({ eventName: options.eventName, event, repository: options.repository });
        if (options.dryRun) {
            process.stdout.write(`${JSON.stringify(plan, null, 2)}\n`);
        } else {
            await executePlan(plan, {
                githubToken: process.env.GITHUB_TOKEN,
                gitlabUrl: options.gitlabUrl,
                gitlabApiUrl: options.gitlabApiUrl,
                gitlabProject: options.gitlabProject,
                gitlabApiToken: process.env.GITLAB_REPLICA_API_TOKEN,
            });
        }
    } catch (error) {
        const message = error instanceof Error ? error.message : String(error);
        console.error(message.startsWith("gitlab-review-replica:") ? message : `gitlab-review-replica: ${message}`);
        process.exitCode = 1;
    }
}

const isMain = process.argv[1] && pathToFileURL(path.resolve(process.argv[1])).href === import.meta.url;
if (isMain) void main();
