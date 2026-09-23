# High-Assurance Engineering Evidence Log

This log is the local tracking fallback for the existing-project uplift. The declared
Linear project could not be resolved in the connected workspace, so no external tracker
record is being created or modified. Entries contain sanitized, reproducible evidence and
never contain credentials, tokens, or private payloads.

## Engagement

- Objective: Raise OnTheRoad's governance, evidence, and delivery assurance while preserving verified product behavior, data, interfaces, and operational invariants.
- Task mode: `EXISTING_UPLIFT`
- Local workspace: `/home/ron/Projects/ontheroad`
- Canonical repository: `https://github.com/ronimuliawan/ontheroad.git`; project governance declares GitHub as the write and merge authority.
- Tracker: Local fallback; Linear workspace search found no `OnTheRoad` project or issue.
- Approval owner: `developer-project-owner` (user approval in this task)
- Risk profile: `STANDARD`; completed Unit 3 scope covered rate editing/persistence and CI instrumentation. No production data, credential, release, or deployment changes were made.

## Current feature delivery

- Feature: Direct Booking & Fare Estimator, Unit 3 — validated rate editing and UI acceptance.
- Delivery: PR #11 from `rons/validated-rate-editing-ui`, based on GitHub `dev` at
  `579c14b6ac9b091d3425001aef4eeea7f0f6e724`, head `5533af057e63e97ea8184fc1fa10f3785378647c`,
  merged into `dev` as `a8ba7c87b06f62cb6e3f54525d741c891a65276d`.
- Status: Unit 3 accepted; exact post-merge GitHub CI passed. No next implementation unit is active.

## Authority and approval record

| Boundary | State | Evidence |
|---|---|---|
| Read-only discovery and baseline | Approved and complete | User-approved Gate 0/1; baseline entries below |
| Target design and implementation plan | Approved | User-approved feature PRD/plan and Unit 3 on 2026-09-23 |
| Local repository edits | Authorized for approved units | User approval for Units 1–3 |
| Branch, commit, push, PR, and merge | Authorized for non-promotion PRs into GitHub `dev` after CI/review | User authorized continued non-promotion PR delivery; no promotion to `main` |
| Deploy or production change | Not authorized | No release or deployment approval supplied |
| Credential or paid-resource changes | Not authorized | No such change is in scope |

## Evidence entries

### 2026-08-31T13:37:21+07:00 — Phase 0/1 — authority, inventory, and baseline

- Actor: Human/agent collaboration
- Exact revision: `ab782714dcad3ab0bd8cb77bf6abec705bb3fd07`
- Environment/profile: Linux workspace, Bun `1.4.0`; documented JDK 21 at `/home/ron/.jdks/jbr-21.0.11`
- Claim evaluated: The committed project baseline is reproducible and the current working tree is understood.
- Method: Read-only instruction and documentation inventory; Git status/topology inspection; clean `HEAD` archive verification; current-worktree checks.
- Result: `PASS` for the committed revision's governance/Bun baseline; `AT RISK` for current worktree because the pre-existing high-assurance bundle is untracked and produces 16 governance inventory findings.
- Durable evidence: `bun /home/ron/Projects/ontheroad/scripts/verify-governance.mjs --root <clean HEAD archive>` returned `governance: pass`; `bun test` returned 4 pass / 0 fail on the clean HEAD archive.
- Deviation/retry: None.
- Security/privacy note: Secret values were not read or recorded; only configuration names and repository paths were inspected.
- Rollback/recovery impact: No product, data, or external state changed.
- Next gate: Gate 2/3 approval, granted by the user on 2026-08-31.

### 2026-08-31 — Phase 1 — Android and release baseline

- Actor: Agent
- Exact revision: `ab782714dcad3ab0bd8cb77bf6abec705bb3fd07`
- Environment/profile: JDK 21; Gradle `8.11.1`; local debug test/lint and release artifact tasks.
- Claim evaluated: The existing Android quality and packaging paths execute successfully before uplift changes.
- Method: `./gradlew testDebugUnitTest lintDebug --no-daemon`; `./gradlew assembleRelease bundleRelease --no-daemon`.
- Result: `PASS`; JVM tests and lint completed successfully; release APK/AAB build completed successfully.
- Measurements: Current artifacts were `app-release.apk` 2,239,791 bytes with SHA-256 `1072752b0f6104a643311149f31caec909bac0b40a585c3f49bab662561f2426`, and `app-release.aab` 4,051,181 bytes with SHA-256 `93a3454c29ff2aac9fd1477611026d6315477fd02567d037a953f64e57ff38f6`.
- Deviation/retry: Initial wrapper invocation was blocked by the sandbox's read-only Gradle user home; the same command was rerun with the approved cache filesystem access. No source change was involved.
- Security/privacy note: Release signing used the repository's existing local fallback path; no signing secret values were read or recorded.
- Rollback/recovery impact: Build outputs are ignored/generated; no application or external state changed.
- Next gate: First approved uplift unit.

### 2026-08-31 — Phase 4 / Unit 1 — adopt and register the complete bundle

- Actor: Agent
- Exact revision: `ab782714dcad3ab0bd8cb77bf6abec705bb3fd07`, branch `codex/high-assurance-uplift`
- Environment/profile: Local repository working tree with 17 pre-existing untracked uplift files.
- Claim evaluated: The complete high-assurance bundle can become repository-governed without replacing existing canonical project documentation.
- Method: Register the bundle's documentation surfaces in `docs/governance/governance.json`; update the project profile only for the adopted project-owned extension; retain package templates without deleting them.
- Result: `COMPLETE`.
- Durable evidence: The manifest registers the 16 governed markdown bundle files plus this evidence log; the project profile records project ownership and entry points; governance validation, Bun tests, and whitespace validation pass. The support script is explicitly registered during Unit 2 because the manifest's normal extension inventory is documentation-only.
- Deviation/retry: None.
- Security/privacy note: No secrets, tokens, or raw private logs are included.
- Rollback/recovery impact: Documentation/manifest/profile-only changes; reverting the unit restores the prior governance inventory and does not alter product data or external state.
- Next gate: Unit 1 completion evidence, then Unit 2 status/contract reconciliation.

### 2026-08-31 — Phase 4 / Unit 1 — completion evidence

- Actor: Agent
- Exact revision: `ab782714dcad3ab0bd8cb77bf6abec705bb3fd07`, branch `codex/high-assurance-uplift`
- Claim evaluated: Unit 1's adopted resources are visible to repository governance and the existing validation path remains green.
- Method: `jq empty docs/governance/governance.json`; `bun run governance:check`; `bun test`; `git diff --check`; review of the tracked diff and adopted-file inventory.
- Result: `PASS` / `COMPLETE`.
- Durable evidence: Governance reports `governance: pass`; Bun reports 4 pass / 0 fail; `git diff --check` is clean; the profile and manifest changes are limited to documentation/governance registration, with no product behavior, data, interface, deployment, or external-state change.
- Deviation/retry: None for Unit 1 validation.
- Security/privacy note: Adopted resources and the evidence log contain no credentials, tokens, or raw private payloads.
- Rollback/recovery impact: Revert the Unit 1 documentation/manifest/profile changes and remove the adopted bundle to restore the pre-uplift inventory; generated build artifacts and product state are unaffected.
- Next gate: Explicit approval to begin Unit 2 status/contract reconciliation.

### 2026-08-31 — Phase 4 / Unit 2 — status and contract reconciliation start

- Actor: Agent
- Exact revision: `ab782714dcad3ab0bd8cb77bf6abec705bb3fd07`, branch `codex/high-assurance-uplift`
- Environment/profile: Local repository working tree; Unit 2 approved by the user on 2026-08-31.
- Claim evaluated: Current status, local delivery commands, release-signing environment names, and Gradle wrapper integrity can be made internally consistent without changing product behavior or external state.
- Method: Reconcile project-owned status and workflow guidance; align the environment example and manifest with the active release workflow; add the official Gradle distribution checksum; register the status document; validate with the governance and Android quality paths.
- Result: `COMPLETE`.
- Durable evidence: This entry is the tracker start record; the completion entry will record exact validation results and the final diff review.
- Deviation/retry: None.
- Security/privacy note: Only environment variable names and public toolchain metadata are in scope; no secret values will be read or recorded.
- Rollback/recovery impact: Documentation, manifest, workflow-guidance, environment-schema, and wrapper-metadata changes only; reverting Unit 2 restores the prior contracts without altering application data or runtime behavior.
- Next gate: Unit 2 completion evidence, then Unit 3 stale-document and release-artifact refresh.

### 2026-08-31T13:58:13+07:00 — Phase 4 / Unit 2 — completion evidence

- Actor: Agent
- Exact revision: `ab782714dcad3ab0bd8cb77bf6abec705bb3fd07`, branch `codex/high-assurance-uplift`, with approved Unit 1 and Unit 2 working-tree changes
- Claim evaluated: Current status, local delivery commands, release-signing environment names, Gradle wrapper integrity, and review-helper safety are internally consistent.
- Method: `jq empty docs/governance/governance.json`; `bun run governance:check`; `bun test`; `bun --check .agents/skills/pr-review-remediation/scripts/pr_review_tools.js`; helper usage invocation; `git diff --check`; `JAVA_HOME=/home/ron/.jdks/jbr-21.0.11 PATH="/home/ron/.jdks/jbr-21.0.11/bin:$PATH" ./gradlew testDebugUnitTest lintDebug assembleRelease bundleRelease --no-daemon`.
- Result: `PASS` / `COMPLETE`.
- Durable evidence: Governance reports `governance: pass`; Bun reports 4 pass / 0 fail; the helper usage and syntax checks pass; Gradle test, lint, release APK, and release AAB tasks pass; whitespace validation is clean. The wrapper checksum is the published Gradle 8.11.1 binary distribution SHA-256 recorded at [Gradle's release checksum reference](https://gradle.org/release-checksums/).
- Deviation/retry: The first helper syntax check failed closed because the no-remote checkout could not resolve a repository even for usage; the helper was corrected to keep usage local, then syntax and usage checks passed. No product source or external state changed.
- Security/privacy note: No secret values, tokens, or credential-bearing URLs were read or recorded; the review helper no longer exposes thread-resolution commands.
- Rollback/recovery impact: Revert the Unit 2 documentation, manifest, workflow-guidance, environment-schema, helper, and wrapper-metadata changes to restore the previous contracts; application data, runtime behavior, and external state remain unaffected.
- Next gate: Explicit approval to begin Unit 3 stale-document and release-artifact refresh.

### 2026-08-31 — Phase 4 / Unit 3 — stale-document and release-artifact refresh start

- Actor: Agent
- Exact revision: `ab782714dcad3ab0bd8cb77bf6abec705bb3fd07`, branch `codex/high-assurance-uplift`
- Environment/profile: Local repository working tree; Unit 3 approved by the user on 2026-08-31.
- Claim evaluated: Current documentation can distinguish the tagged v0.1.0 release from post-release working-tree artifacts, while stale repository links, counts, and completed-plan states are corrected from measured or version-control evidence.
- Method: Rebuild the debug artifact, measure current APK/AAB byte sizes and SHA-256 identities, inspect tag-to-HEAD divergence, refresh current README values and canonical links, label versioned release/planning documents as historical, and run the full local verification loop.
- Result: `COMPLETE`.
- Durable evidence: This entry is the tracker start record; the completion entry will record measured artifacts, exact document changes, validation results, and the final diff review.
- Deviation/retry: None.
- Security/privacy note: Only public repository links, test counts, artifact metadata, and document dispositions are in scope; no secret values will be read or recorded.
- Rollback/recovery impact: Documentation and manifest disposition changes only; no application data, runtime behavior, release tag, or external state will be changed.
- Next gate: Unit 3 completion evidence, then final review/handoff or a separately approved follow-up.

### 2026-08-31T14:04:18+07:00 — Phase 4 / Unit 3 — completion evidence

- Actor: Agent
- Exact revision: `ab782714dcad3ab0bd8cb77bf6abec705bb3fd07`, branch `codex/high-assurance-uplift`, with approved Unit 1–3 working-tree changes
- Claim evaluated: Current documentation identifies the canonical repository and current working-tree artifacts accurately, while tagged release notes and completed plans remain clearly historical.
- Method: `./gradlew test assembleRelease bundleRelease assembleDebug --no-daemon`; `sha256sum` and `stat` over the three APK/AAB outputs; `jq empty docs/governance/governance.json`; `bun run governance:check`; `bun test`; artifact identity assertions; `git diff --check`; scoped credential-pattern scan; review of the tracked diff and registered inventory.
- Measurements: Current working-tree artifacts are `app-release.apk` 2,239,791 bytes with SHA-256 `1072752b0f6104a643311149f31caec909bac0b40a585c3f49bab662561f2426`, `app-release.aab` 4,051,181 bytes with SHA-256 `93a3454c29ff2aac9fd1477611026d6315477fd02567d037a953f64e57ff38f6`, and `app-debug.apk` 17,743,073 bytes with SHA-256 `4c178ab4e796b22a45883f6835e2dcf51533b7701efbb5ed1ffe229361f9f6c2`.
- Result: `PASS` / `COMPLETE`.
- Durable evidence: Governance reports `governance: pass`; Bun reports 4 pass / 0 fail; artifact documentation identity assertions pass; Gradle runs all JVM tests, lint, release APK/AAB, and debug APK tasks successfully; whitespace and scoped credential scans are clean. The Unit 3 snapshot recorded the then-declared `rons-space/ontheroad` badge target; cycle-2 remediation reconciles the active badges and canonical repository metadata to `ronimuliawan/ontheroad`. `RELEASE_NOTES.md` retains its v0.1.0 artifact identities and is explicitly historical rather than conflating them with post-release builds.
- Deviation/retry: No validation retry was required. The debug artifact was rebuilt before measurement to avoid documenting a stale generated output.
- Security/privacy note: No secret values, tokens, or credential-bearing URLs were read or recorded; no release tag, deployment, or external state changed.
- Rollback/recovery impact: Revert the Unit 3 documentation and manifest disposition changes to restore the prior document classifications and current-doc values; generated artifacts are ignored and application data/runtime behavior are unaffected.
- Next gate: Final read-only diff/evidence review and handoff; no further implementation unit is started by this approval.

### 2026-08-31T14:05:40+07:00 — Final review and handoff

- Actor: Agent
- Exact revision: `ab782714dcad3ab0bd8cb77bf6abec705bb3fd07`, branch `codex/high-assurance-uplift`, with approved Unit 1–3 working-tree changes
- Claim evaluated: The completed uplift is bounded, the final changed-file inventory contains no product source changes, and the recorded evidence is internally consistent.
- Method: Read-only review of `git status`, tracked diff paths, diff summary, manifest classifications, current README measurements, historical release note boundaries, and the final governance/Bun/diff/secret checks. Android test, lint, release, and debug validation remained green from the Unit 3 completion evidence.
- Result: `PASS` / `HANDOFF READY`.
- Durable evidence: 16 tracked files are documentation, manifest, environment example, workflow guidance, or wrapper metadata; the pre-existing project-owned bundle and evidence/status files remain preserved; no `app/`, `core/`, or application build-source files are changed.
- Deviation/retry: None after the Unit 3 completion validation.
- Security/privacy note: No external writes, credential changes, deployments, or release-tag changes occurred.
- Rollback/recovery impact: All changes remain local and uncommitted; revert the documented Unit 1–3 file set to restore the pre-uplift working tree contracts.
- Next gate: Human review of the local diff; any commit, push, pull request, merge, deployment, or new implementation scope requires separate explicit authorization.

### 2026-08-31T14:11:48+07:00 — Human review acceptance

- Actor: `developer-project-owner`
- Exact revision: `ab782714dcad3ab0bd8cb77bf6abec705bb3fd07`, branch `codex/high-assurance-uplift`, with approved Unit 1–3 working-tree changes
- Claim evaluated: The reviewed local uplift is acceptable as delivered.
- Method: User confirmation: “reviewed, all good.”
- Result: `ACCEPTED FOR LOCAL HANDOFF`.
- Durable evidence: Human review acceptance is recorded without creating a commit or changing any external system.
- Next gate: Explicit authorization is still required for local commit creation, push, pull request, merge, deployment, or new implementation scope.

### 2026-08-31 — PR #2 review remediation validation

- Actor: Agent
- Exact revision: PR #2 source revision `e561286ac4133906c46fc33d2bfe95f3ad87b6b0`; remediation changes are documentation, governance, workflow, and review-tool changes only.
- Environment/profile: Isolated fix worktree; Bun `1.4.0`; JDK 21; Android SDK `/home/ron/Android/Sdk`; Gradle `8.11.1`.
- Claim evaluated: Reviewer findings are addressed without changing product behavior, persisted data, or release signing secrets.
- Method: Verify all 12 CodeRabbit findings against the PR head; run `bun run governance:check`, `bun test`, `./gradlew testDebugUnitTest lintDebug --no-daemon`, and `./gradlew assembleRelease bundleRelease assembleDebug --no-daemon`; rebuild release outputs twice and compare identities; validate helper URL parsing, paginated extraction, and invalid-source rejection.
- Measurements: `app-release.apk` 2,239,791 bytes with SHA-256 `e6dff65bfbc16ef699e6b0aa82f878205373402bec06f569932475485dbae078`; `app-release.aab` 4,051,114 bytes with SHA-256 `179661495156847ce500d39c45b0b47dc8317f149bfd7a87455abf7b5293e402`; `app-debug.apk` 17,743,073 bytes with SHA-256 `4c178ab4e796b22a45883f6835e2dcf51533b7701efbb5ed1ffe229361f9f6c2`.
- Result: `PASS` pending single-commit and external-delivery approval.
- Durable evidence: Governance reports `governance: pass`; Bun reports 4 pass / 0 fail; Android JVM tests and lint pass; release/debug artifacts build successfully; consecutive artifact builds produced identical identities; helper extraction returned 12 threads and rejected an unsupported reply source without a GitHub write.
- Deviation/retry: The first isolated Android invocation lacked the untracked SDK configuration; rerunning with the installed SDK paths passed. The first manifest adjustment exposed the required governed-document registration for `.github/workflows/release.yml`; the manifest was completed and revalidated.
- Security/privacy note: No secret values were read or recorded; no review thread was resolved; no external reply or push was performed.
- Rollback/recovery impact: Revert the single remediation commit to restore the PR #2 source state; application data, runtime behavior, and release tags remain unaffected.
- Next gate: Create one local atomic commit, then request separate approval before push, attributed replies, or any other external write.

### 2026-08-31T17:40:26+07:00 — PR #2 review remediation cycle 2 validation

- Actor: Agent
- Exact base revision: PR #2 source revision `595489b69040fc20cf3dd807397d9e8c61a90a09`; cycle-2 fixes were validated in an isolated worktree based on this immutable revision.
- Environment/profile: Bun `1.4.0`; JDK 21; Android SDK `/home/ron/Android/Sdk`; Gradle `8.11.1`.
- Claim evaluated: The six new CodeRabbit findings and the related review-workflow check-state discrepancy are addressed without changing product behavior, persisted data, or release signing secrets.
- Method: Verify all six new CodeRabbit findings against the current PR head; run `node --check .agents/skills/pr-review-remediation/scripts/pr_review_tools.js`, `bun run governance:check`, `bun test`, `git diff --check`, the helper's suite check, live PR extraction, and `./gradlew testDebugUnitTest lintDebug --no-daemon`.
- Result: `PASS` pending single-commit and external-delivery approval.
- Durable evidence: Governance reports `governance: pass`; Bun reports 4 pass / 0 fail; Android JVM tests and lint pass; helper syntax, live extraction, and suite checks pass; the live PR has 4 completed checks (3 GitHub Actions and CodeRabbit), 18 total review threads, and 6 unresolved actionable findings before this cycle's fix is delivered.
- Deviation/retry: The initial suite hardening used the repository's `coderabbitai` app name but the live status context is `CodeRabbit`; the alias was added and the helper then reported all 4 active checks as completed. Zero-run stale integration placeholders remain excluded from the active suite result.
- Security/privacy note: No secret values were read or recorded; no review thread was resolved; no external reply or push was performed in this cycle.
- Rollback/recovery impact: Revert the single cycle-2 remediation commit to restore the PR #2 source tree; application data, runtime behavior, and release tags remain unaffected.
- Next gate: Final diff review and one local atomic commit, then request separate approval before push, attributed replies, or any other external write.

### 2026-08-31 — GitLab review replica implementation start

- Actor: \`developer-project-owner\` approval / agent implementation
- Exact baseline revision: \`a415af5c36acce53cd86853a2081baa8bbef3e8f\` (\`origin/dev\`)
- Isolated worktree: \`/tmp/ontheroad-gitlab-review\`, branch \`codex/gitlab-review-replica\`
- Objective: Add a GitHub-owned, GitLab review-only projection so CodeRabbit can review GitLab MRs when GitHub review capacity is rate-limited.
- Authority: GitHub remains the sole write and merge authority; GitLab receives only projected refs and advisory MRs.
- Approved scope: User approved Option 2 and the Wandernest-derived implementation on 2026-08-31, including scoped credential setup.
- External baseline: Public GitLab project \`ronimuliawan/ontheroad\` exists; the earlier pull-mirror attempt is inactive and the project remains empty.
- Credential contract: Dedicated GitLab deploy key with 180-day expiry, verified GitLab host keys, and a separate project API credential with 90-day expiry. Only credential names, metadata, fingerprints, and status may be recorded.
- Acceptance evidence: Repository governance and focused sync-helper tests pass; GitHub workflow reaches only \`dev\`, \`main\`, approved tags, and deterministic same-repository PR review refs; GitLab protected refs cannot be merged by humans or review providers; disposable PR/MR parity and close cleanup are proven before launch.
- Security/privacy note: Secret values are not recorded, printed, committed, embedded in URLs, or placed in tracker text.
- Rollback/recovery impact: Revert the repository projection files; disable the workflow; revoke the three GitHub secrets and matching GitLab credentials; remove only exact synthetic review refs. GitHub branches, tags, product data, and release state are unchanged.
- Next gate: Focused local validation, then secure GitLab/GitHub credential and branch-protection setup.

### 2026-08-31 — GitLab review replica security setup complete

- External project: Public GitLab project `ronimuliawan/ontheroad`, project ID `85928609`; it remains empty until the trusted GitHub workflow is delivered.
- Deploy key: Project key ID `21497072`, title `ontheroad-github-review-replica`, push enabled, fingerprint `SHA256:WS4Fq2DbIpHKIc3t5frI7pFAP5FYrZ46Wdhl5pZiphM`, expiry `2027-02-27`.
- API credential: Project token ID `27056976`, name `ontheroad-review-ref-cleanup`, active, Maintainer access level, `api` scope, expiry `2026-11-29`.
- GitHub secret names configured: `GITLAB_REPLICA_SSH_KEY`, `GITLAB_REPLICA_KNOWN_HOSTS`, and `GITLAB_REPLICA_API_TOKEN`. Secret values were never recorded or exposed.
- Host verification: GitLab.com ED25519 host key was checked against the published fingerprint; SSH authentication with the dedicated deploy key succeeded.
- Protected refs: GitLab `*`, `dev`, and `main` are protected; force push is disabled; merge access is disabled; only the dedicated deploy key may push.
- Security boundary: No GitLab product ref was manually pushed; no GitLab merge or write authority was granted to humans or review providers.
- Acceptance status: Credential and protection setup pass; repository workflow delivery and disposable GitHub-to-GitLab parity proof remain pending because the implementation branch has not been pushed.
- Rollback/recovery impact: Revoke the named GitHub secrets and matching GitLab credentials, then remove only exact synthetic review refs if any are created. GitHub authority and product state remain unchanged.
- Next gate: Local atomic commit complete; separate user approval is required before pushing the branch or opening the GitHub PR.

### 2026-08-31 — GitLab review replica delivery PR opened

- External action approval: User approved pushing the implementation branch and opening the GitHub PR after the local atomic commit.
- GitHub branch: `codex/gitlab-review-replica` pushed at `7354a9a8e52b0b0644a7649e2d75c45be3fb6a0d`.
- GitHub PR: [#5](https://github.com/ronimuliawan/ontheroad/pull/5), base `dev`, state open.
- Automated state at recording time: Governance and JVM checks pending; CodeRabbit reports pass because reviews are disabled for the `dev` base branch.
- GitLab acceptance state: The project remains empty until PR #5 is merged and the trusted default-branch workflow seeds canonical refs; no GitLab refs were manually pushed.
- Next gate: Complete PR review and merge through GitHub only; then run the disposable same-repository GitHub-to-GitLab parity and close-cleanup proof.

### 2026-08-31 — PR #2 review remediation cycle 3

- Actor: Agent implementing the user's explicit approval for the nine valid CodeRabbit findings; the docstring-coverage note was accepted as informational and ignored without rebuttal.
- Exact base revision: PR #2 head `c89bd7de545193e837ec06e6f91af5e879d2b9df` in isolated worktree `/tmp/ontheroad-pr2-review-cycle3`, branch `codex/pr2-review-cycle3`.
- Accepted findings: review comments `3894829488`, `3894829516`, `3894829519`, `3894829524`, `3894829529`, `3894829535`, `3894829540`, `3894829557`, and `3894829569`.
- Scope: Clarify Phase 0 tracker/evidence authority; correct D8 contract ownership; make zero-monitored-check suites settled; fail closed on issue-comment retrieval errors; register the GitLab workflow; mark active replica credentials current; repair runbook Markdown; harden repository-segment validation; and make the workflow assertion whitespace-tolerant.
- Quiescence exception: GitHub retains zero-run integration placeholder suites for Vercel and CodeRabbit; raw check-suite evidence showed `latest_check_runs_count: 0`. Actual active checks completed, and the modified helper reports `allCompleted: true` with seven completed monitored results.
- Validation: `bun run governance:check` passed; `bun test` passed with 12 tests; both modified JavaScript files passed syntax checks; live helper extraction returned 27 threads and 9 unresolved findings; `git diff --check` passed.
- Security/privacy note: No secrets were read or recorded; no review thread was resolved; no external reply has been posted in this cycle.
- Rollback/recovery impact: Revert the single remediation commit to restore the PR #2 source tree; application data, GitHub/GitLab authority, release state, and credentials remain unaffected.
- Next gate: Create one atomic remediation commit, push the fix branch, open a PR into `dev`, then post attributed replies without resolving threads.

### 2026-08-31 — PR #2 review remediation cycle 4

- Actor: Agent implementing the user's explicit approval for the five valid CodeRabbit findings; the docstring-coverage note remains accepted as informational and ignored without rebuttal.
- Exact base revision: PR #2 head `8fe47e904babf27aa6b6cbf4167c9452c3e56bef` in isolated worktree `/tmp/ontheroad-pr2-review-cycle3`, branch `codex/pr2-review-cycle4`.
- Accepted findings: review comments `3895701299`, `3895701309`, `3895701316`, `3895701346`, and `3895701372`.
- Scope: Require named and populated evidence surfaces for `Covered`; correct G1's operator-guide mapping; paginate combined commit statuses; validate suite commit/ref arguments; and distinguish new-engagement Phase 0 entry from verified resumed-engagement continuation.
- Quiescence evidence: The visible PR checks for the exact head completed successfully. GitHub also retains zero-run queued integration placeholders for Vercel and CodeRabbit; raw check-suite data showed `latest_check_runs_count: 0`, so they were excluded from active monitoring.
- Validation: `bun run governance:check` passed; `bun test` passed with 12 tests; the helper suite probe for the exact PR head reported six completed successful monitored results; invalid commit-ref probing rejected traversal input; JavaScript syntax, manifest JSON, and whitespace checks passed.
- Security/privacy note: No secrets were read or recorded; no review thread was resolved; no external reply or push has been performed in this cycle.
- Rollback/recovery impact: Revert the single cycle-4 remediation commit to restore the PR #2 source tree; application data, GitHub/GitLab authority, release state, and credentials remain unaffected.
### 2026-09-01 — Direct / Offline Trip Booking & Fare Estimator Delivery

- Actor: Human/agent pair under High-Assurance Engineering standard (`/high-assurance-engineering`)
- Linear Tracker: Project `OnTheRoad: Direct Booking & Fare Estimator` (`RON-277`, `RON-278`, `RON-279`, `RON-280`).
- Exact base revision: Branch `dev` on Linux, Bun `1.4.0`, JDK 21 at `/home/ron/.jdks/jbr-21.0.11`, Gradle `8.11.1`.
- PRD: `.claude/PRPs/prds/direct-booking-and-fare-estimator.prd.md` registered and approved under Phase 2.
- Unit 1 (Domain & Data):
  - Added pure Kotlin data model `DirectPricingRates` in `core:model` (`ARCH-001`).
  - Implemented `CalculateDirectFareUseCase` with base fare, included distance, rate/km, min fare, and custom override handling.
  - Implemented `EstimateDistanceUseCase` with Haversine distance and 1.30x road curvature detour multiplier.
  - Extended `UserPreferencesRepository` & `UserPreferencesRepositoryImpl` with rates persistence.
  - Extended `StartTripUseCase` with optional quote distance and fare parameters.
- Unit 2 (UI & Presentation):
  - Created `DirectBookingCard` cockpit component with pickup, destination, distance, rates breakdown, and live estimated fare.
  - Updated `SettingsScreen` and `SettingsViewModel` with driver configurable rate cards.
  - Integrated Direct Booking card into `TrackerScreen` and `TrackerViewModel` with instant start run cockpit transition.
- Verification & Quality:
  - 47/47 JVM unit tests passing in 525ms (`CalculateDirectFareUseCaseTest`, `EstimateDistanceUseCaseTest`, `UserPreferencesRepositoryTest`, `SettingsViewModelTest`, `TrackerViewModelTest`, etc.).
  - Gradle `assembleDebug` and `assembleRelease` APK builds passing with clean R8 shrinkage.
  - Bun governance verifier passing (`bun run governance:check`).
- Security/Privacy: 100% offline, zero network tracking, no secrets committed (`SEC-001`).
- Rollback impact: Clean revert of feature commits restores repository without state or schema corruption.
- Next gate: Phase 6 complete; ready for driver staging / release promotion.

### 2026-09-23 — PRD approval and feature-state reconciliation

- Actor: Human/agent pair under the High-Assurance Engineering standard.
- Approval: The project owner explicitly confirmed the Direct Booking & Fare Estimator PRD.
- Approved artifact: `.claude/PRPs/prds/direct-booking-and-fare-estimator.prd.md` is now an approved target for the v0.2.0 feature scope.
- Current repository state: The feature implementation remains uncommitted in the local `dev` working tree; `origin/dev` and `origin/main` remain at `19ee2e271c9e38b51d09cf9f0aa4246b1acc0b2e` and do not contain the feature files.
- Evidence correction: The 2026-09-01 delivery entry is retained as historical evidence. Its test/build claims have not been re-established against the current exact dirty tree, so they are not current acceptance evidence.
- Validation of this reconciliation: `bun run governance:check` passed; `git diff --check` reported two pre-existing blank-line-at-EOF warnings in feature files, which were not changed or normalized.
- Current gate: Baseline and architecture/implementation-plan reconciliation remain pending. This PRD approval does not authorize a branch, commit, push, pull request, merge, release, or deployment.
- Rollback/recovery impact: Documentation-only correction; reverting this entry and the matching PRD/status metadata restores the prior local documentation state. No application data or external state is changed.

### 2026-09-23 — Phase 1 baseline and Phase 3 architecture plan

- Actor: Human/agent pair under the High-Assurance Engineering standard; the project owner approved both gates before this inspection.
- Exact repository state: `origin/dev` and `origin/main` at `19ee2e271c9e38b51d09cf9f0aa4246b1acc0b2e`; direct-booking implementation remains local-only and uncommitted.
- Architecture result: Pure Kotlin model/domain boundaries remain intact; data owns Android location/geocoding and persistence; app owns ViewModel wiring and foreground-service start; Compose owns rendering.
- Verified findings: P1 quote/realized-earnings field conflation and empty completion-modal prefill; P1 missing runtime location-permission gate; P2 best-effort Geocoder/`INTERNET` boundary versus strict offline wording; P2 transient invalid rate values being persisted from Settings.
- Plan artifact: `.claude/PRPs/plans/11-direct-booking-and-fare-estimator.plan.md` records the three ownership choices, recommended boundaries, implementation units, acceptance matrix, source-control policy, and rollback constraints.
- Validation: `bun test` passed with 12 tests; `bun run governance:check` passed. Gradle validation is blocked because no Java executable exists and `/home/ron/.jdks/jbr-21.0.11` is absent.
- Next gate: Unit 1 implementation — quote contract and completion handoff. It requires separate explicit user approval; no application edit or external delivery action is authorized by this planning gate.

### 2026-09-23 — Unit 1 implementation: quote contract and completion handoff

- Actor: Agent implementing the user's explicit Unit 1 approval.
- Scope: Added nullable `quotedFareAmountCents` to the pure trip model and Room entity, added the v1-to-v2 migration, kept realized earnings fields separate, preserved quoted distance on completion when omitted by the UI, and wired the direct completion modal to prefill quote/payment values.
- Regression coverage added: direct-start quote persistence, repository entity/domain mapping, ViewModel start/complete handoff, and pure UI amount/distance conversion tests.
- TDD evidence: RED tests were written first; the focused Gradle test command could not start because no Java executable exists and `/home/ron/.jdks/jbr-21.0.11` is absent. No GREEN result is claimed.
- Review state: Source-level diff review completed; two pre-existing blank-line-at-EOF warnings remain in unrelated-to-this-unit dirty feature files and were not normalized.
- Current gate: Unit 1 validation is pending a usable JDK. No commit, push, pull request, merge, or release action is authorized by this implementation approval.

### 2026-09-23 — CI-only validation decision

- Decision: The project owner explicitly directed that no local test, build, or verification run
  be performed; CI is the acceptance authority.
- The earlier local Bun and governance results remain informational observations only. No further
  local validation will be run for this unit.
- The missing local JDK is not a project blocker under this decision. Gradle tests, lint, builds,
  and repository checks must run on the exact authorized remote revision in GitHub CI.
- This supersedes the preceding implementation-entry wording that made a usable local JDK the
  validation gate; the active gate is now exact-revision CI evidence.
- Next gate: explicit authorization for the branch, commit, push, and/or pull request needed to
  trigger CI. No commit, push, pull request, merge, or release action was performed by this
  decision.

### 2026-09-23 — Unit 1 CI acceptance and merge

- Exact revision: PR #8 merged into GitHub `dev` at `93a2b189ec426de018f0d138d9469d1de2e2d01e`.
- Acceptance evidence: Android JVM unit tests, Android lint, repository governance CI, and
  CodeRabbit completed successfully on the PR revision.
- Review-mirror exception: the GitLab replica close-cleanup job returned HTTP 401 because
  `GITLAB_REPLICA_API_TOKEN` is not currently usable. The project owner explicitly deferred
  GitLab repair; it is not an application delivery gate.
- Current gate: Unit 1 is complete. Unit 2 — permission and address fallback — is authorized and
  in progress on a new branch from the merged `dev` tip. No local test, build, or verification
  run is being performed.

### 2026-09-23 — Unit 2 CI acceptance and merge

- Exact revision: PR #9 merged into GitHub `dev` at
  `685c530c090672ec6f55ba5221e6c4f5fd304727`.
- Acceptance evidence: Android JVM tests, Android lint, and governance CI passed on the PR head
  and again on the post-merge `dev` revision.
- Review state: CodeRabbit skipped automated review because `dev` is not the repository default
  branch; the Codex review bot reported usage limits. The project owner authorized merging the
  non-promotion PR after CI passed.
- GitLab review-mirror cleanup remains deferred by the project owner and is not a delivery gate.
- Current gate: Unit 2 is complete. Unit 3 — validated rate editing and UI acceptance — is
  planned but not started; it requires separate explicit approval. No local tests/builds ran.

### 2026-09-23 — Unit 3 implementation start

- Actor: Agent implementing the project owner's explicit approval in the active task.
- Exact base revision: GitHub `origin/dev` `579c14b6ac9b091d3425001aef4eeea7f0f6e724`; branch `rons/validated-rate-editing-ui`.
- Process note: This local start record was added after the first source edits in the resumed task; the explicit Unit 3 approval and exact base had already been established.
- Scope: Keep Settings drafts local until valid explicit save, guard rate persistence, cover fare/cockpit state edges, and add the planned Android device-profile CI acceptance.
- Verification authority: GitHub CI on the exact PR revision. The owner's standing instruction excludes local tests, builds, and verification commands.
- Rollback/recovery impact: Revert PR #11's merge commit on `dev` to restore prior rate-edit behavior and remove its test/CI additions; never rewrite shared branch history. No release, credentials, or production data are changed.
- Next gate: Record exact post-merge CI evidence, then await explicit approval for another implementation unit or a separately scoped promotion decision.

### 2026-09-23 — Unit 3 CI acceptance and merge

- Exact revision: PR #11 source head `5533af057e63e97ea8184fc1fa10f3785378647c`, merged into GitHub `dev` at `a8ba7c87b06f62cb6e3f54525d741c891a65276d`.
- PR acceptance: JVM Unit Tests, Android Lint, Android UI Acceptance (Pixel 7 Pro / API 35), governance, and GitLab trusted-ref checks passed on the PR revision.
- Post-merge acceptance: Android CI run [35851099402](https://github.com/ronimuliawan-coder/ontheroad/actions/runs/35851099402), governance run [35851099256](https://github.com/ronimuliawan-coder/ontheroad/actions/runs/35851099256), and GitLab trusted-ref run [35851099357](https://github.com/ronimuliawan-coder/ontheroad/actions/runs/35851099357) all completed successfully on the exact merge SHA above. Android CI includes JVM tests, lint, and the Pixel 7 Pro / API 35 instrumentation flow on the Namespace runners.
- Runner deviation: The first instrumentation attempt failed before tests because `/etc/udev/rules.d` is absent on the Namespace runner image. The workflow now grants access to the existing `/dev/kvm` node directly; the PR and post-merge instrumentation runs passed.
- Review state: CodeRabbit skipped review because reviews are disabled for the non-default `dev` base; the Codex review bot reported a usage limit. No automated review findings were available. The project owner authorized merging the non-promotion PR after required CI passed.
- Local verification: None; the owner requires verification to run in CI.
- Rollback/recovery impact: Revert merge commit `a8ba7c87b06f62cb6e3f54525d741c891a65276d` on `dev`; no force-push, production data, release, or credential changes.
- Next gate: Unit 3 is complete. Await explicit approval before starting another implementation unit; promotion to `main` remains unauthorized.

## Current accepted exceptions

| Exception | Reason | Risk | Compensating control | Owner | Expiry/revisit trigger | Approval |
|---|---|---|---|---|---|---|
| External Linear tracking unavailable | Connected workspace has no matching `OnTheRoad` project or issue | Progress is not mirrored to the declared external tracker | This local evidence log records objective, approvals, exact revisions, evidence, deviations, and rollback impact | `developer-project-owner` | Revisit when the canonical Linear project is available | User approved local fallback on 2026-08-31 |
| Local test/build/verification disabled | Project owner directed that all verification run in CI | No local verification evidence is produced | Require exact-revision GitHub CI, including JVM tests, lint, governance, and applicable UI acceptance for each approved unit; Unit 3 passed on its merge revision above | `developer-project-owner` | Revisit only if the owner changes the CI-only direction | Explicit user direction on 2026-09-23 |

## Historical evidence matrix — 2026-08-31 governance uplift

The following matrix and resume packet record the earlier Phase 4 governance uplift closeout.
They are historical and do not describe the current Direct Booking feature unit; its status and
evidence are in the dated 2026-09-23 entries above.

| Required gate | Exact revision/environment | Result | Evidence | Accepted exception |
|---|---|---|---|---|
| Correctness | `ab782714…` plus approved Unit 3 working tree, JVM/Android local validation | Pass | Unit 3 completion entry and artifact measurements above | None |
| Security/privacy | `ab782714…` plus approved Unit 3 working tree, scoped repository scan | Pass within scope | No credential-bearing files or URLs found in the adopted/changed surfaces; historical/current document boundary is explicit | Full remote/CI security state remains unavailable |
| Reliability/recovery | Existing app / documentation uplift | Pass | No runtime/data change; rollback recorded in Unit 3 completion entry | None |
| Performance | Existing release build | Baseline only | Current artifact measurements above | No performance target change in Unit 3 |
| Accessibility/usability | Existing app | Not run | No UI behavior change in Unit 1 | Device/UI audit remains deferred |
| Analytics/observability | Existing app | Not applicable | No analytics or observability change in Unit 1 | None |
| Build/CI/review | Local exact revision plus approved Unit 3 working tree | Local pass; remote pending | Bun and Gradle Unit 3 completion evidence above | No remote configured |
| Deployment/acceptance | No deployment authorized | Not run | No deployment environment supplied | External release remains human-only |

## Historical resume packet — 2026-08-31 governance uplift

- Phase at that time: Phase 4 / Unit 3 complete; final review/handoff complete.
- Approved actions at that time: local changes to the complete bundle's governance registration and approved status/evidence/contract documentation units.
- Not authorized at that time: push, pull request, merge, deploy, production changes, credential changes, or paid resources.
- Pre-existing user work at that time: the high-assurance and PR-review bundle was already untracked before its branch was created; preserve it.
- Next unstarted work then: No further implementation unit in that uplift engagement.
