# GitLab Review Replica Runbook

This runbook defines the GitHub-owned projection of OnTheRoad into the public
GitLab project [`ronimuliawan/ontheroad`](https://gitlab.com/ronimuliawan/ontheroad).
GitHub remains the only source-control, merge, release, and deployment authority.
GitLab merge requests exist only for CodeRabbit and other advisory review.

## Authority and ref contract

| Ref or action | Authority | GitLab behavior |
|---|---|---|
| `dev` | GitHub | Explicit, non-forced projection |
| `main` | GitHub | Explicit, non-forced projection |
| `v<semver>` and `release-*` tags | GitHub | Add-only scheduled/manual projection |
| GitHub PR `N` | GitHub source branch | Synthetic `review/github-pr-N` branch and GitLab MR |
| GitLab MR | Review providers | Never merge; findings return to the GitHub PR |
| Product CI and releases | GitHub | Never run from GitLab |

The workflow never uses `git push --mirror`. Canonical branches and accepted
release tags cannot be rewritten by the projection. A synthetic PR ref may be
updated with `--force-with-lease` because a GitHub PR head can be rewritten.
Fork PRs are excluded from the secret-bearing projection until a separate threat
review approves a safe design.

## Repository and workflow

The trusted default-branch workflow is
`.github/workflows/gitlab-review-replica.yml`. It handles:

- `push` events for `dev` and `main`;
- same-repository `pull_request_target` open, update, reopen, draft, and close events;
- scheduled and manually dispatched reconciliation.

The workflow checks out only trusted workflow code. It fetches PR commits as
opaque Git objects and never checks out, builds, sources, tests, traverses, or
executes PR content. It uses a separate concurrency group for each PR, keeps
pending lifecycle events queued, and re-reads current GitHub PR state before
projection or cleanup.

GitLab receives `ci.skip` push options, and `.gitlab-ci.yml` is intentionally
inert. GitLab must not become a second build, deployment, branch-reconciliation,
or merge path.

## Required GitHub secrets

Create these repository secrets without placing values in Git, URLs, logs,
issues, PRs, or this runbook:

| Secret | Purpose | Rotation trigger |
|---|---|---|
| `GITLAB_REPLICA_SSH_KEY` | Dedicated private deploy key with write access to this GitLab project | Exposure, expiry, owner change, or replica retirement |
| `GITLAB_REPLICA_KNOWN_HOSTS` | Verified GitLab.com host-key line(s) | GitLab-published host-key rotation |
| `GITLAB_REPLICA_API_TOKEN` | Dedicated project API credential for closing review MRs and deleting protected synthetic refs | Exposure, expiry, owner change, scope change, or replica retirement |

The SSH key is used only through an ephemeral runner file and pinned
`known_hosts`. The API token is exposed only to the trusted close and
maintenance paths, sent in a `PRIVATE-TOKEN` header, and never passed as a CLI
argument or embedded in a URL. The minimum currently available API scope for
protected-ref cleanup is broad `api`; use a dedicated project-scoped credential,
not a personal token.

## GitLab project setup

1. Add one dedicated deploy key under **Settings → Repository → Deploy keys**.
   Enable write permission, record only its public fingerprint and expiry, and
   do not reuse it in another project.
2. Protect `*`, `dev`, and `main`.
   - force push: disabled;
   - merge access: no one;
   - push access: only the dedicated replica deploy key;
   - retain owner access to change protection settings.
3. Install CodeRabbit only for this project with the minimum review access it
   needs. Do not grant it branch-push, merge, release, or deployment authority.
4. Add the three GitHub secrets through standard input or the GitHub UI. Verify
   secret names only; values must remain undisclosed.
5. After the workflow is present on GitHub's default branch, run the disposable
   same-repository proof below before relying on the replica for active review.

## Proof and parity

Use a disposable draft PR from a same-repository branch into `dev`. Do not use a
fork and do not merge the proof PR.

Record only safe identifiers and timestamps:

- GitHub workflow run URL and exact head SHA;
- GitHub PR number and GitLab MR IID;
- synthetic branch name;
- GitHub/GitLab `dev` and `main` SHAs;
- CodeRabbit review result;
- cleanup and rollback result.

Parity checks:

```bash
git fetch origin
git rev-parse origin/main origin/dev
git ls-remote https://gitlab.com/ronimuliawan/ontheroad.git \
  refs/heads/main refs/heads/dev
```

The GitLab synthetic branch must equal the current GitHub PR head SHA. Update the
proof PR, confirm the same GitLab MR/ref converges to the new SHA, then close it
and confirm the MR closes before the synthetic ref is deleted. Confirm GitLab
has no active product pipeline and that no human or review provider can merge or
push protected refs.

## Normal operation

Review findings discovered in GitLab are applied to the GitHub PR branch. Never
commit directly to GitLab and never merge a GitLab MR. If a GitHub PR is
retargeted between `dev` and `main`, the next trusted event updates the MR
target; a closed PR's deterministic synthetic ref remains eligible for cleanup.

If the projection is delayed, use the scheduled/manual reconciliation from the
default branch. Do not push `dev`, `main`, or tags by hand to GitLab. If the
workflow fails, preserve the GitHub PR and inspect the workflow summary before
retrying.

## Rollback and incident response

To disable the projection:

1. Disable `.github/workflows/gitlab-review-replica.yml` on GitHub.
2. Revoke the three GitHub secrets and the matching GitLab deploy/API
   credentials.
3. Delete only synthetic branches matching `review/github-pr-<positive integer>`
   after verifying their exact names.
4. Restore the prior workflow/documentation revision through the normal GitHub
   PR process.

Rollback does not modify GitHub `dev`, `main`, tags, application data, release
artifacts, or deployment state. If a credential may have leaked, revoke it
immediately, preserve safe run identifiers, and do not substitute a personal
credential.
