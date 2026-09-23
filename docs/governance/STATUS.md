# OnTheRoad Project Status

This is the current health and next-gate record for the Direct / Offline Booking & Fare Estimator
feature delivery. Durable evidence and approvals are recorded in
[`EVIDENCE-LOG.md`](EVIDENCE-LOG.md); repository authority and invariants remain in
[`GOVERNANCE.md`](GOVERNANCE.md).

- Last verified: `2026-09-23` (Asia/Jakarta; remote merge and CI status inspection)
- Exact revision/environment: `origin/dev` at `93a2b189ec426de018f0d138d9469d1de2e2d01e`; `origin/main` at `19ee2e271c9e38b51d09cf9f0aa4246b1acc0b2e`; Unit 2 branch is local-only
- Current work item: Unit 2 — permission and address fallback `IN PROGRESS`
- Overall health: `IMPLEMENTATION IN PROGRESS / UNIT 2 CI PENDING`
- Tracker/project: Linear Project `OnTheRoad: Direct Booking & Fare Estimator` (`RON-277`, `RON-278`, `RON-279`, `RON-280`)

## Current objective

Deliver the on-the-spot / offline direct booking quoting engine, rate configurations in Settings,
address estimation, and live GPS odometer cockpit integration while preserving verified product
behavior, data, pure Kotlin domain architecture (`ARCH-001`), and zero secrets in code (`SEC-001`).
The product scope is approved; implementation acceptance is not yet established for the current
dirty tree.

## Health and thresholds

| Area | Status | Current evidence | Target/threshold | Owner / next action |
|---|---|---|---|---|
| Correctness | `UNIT 1 ACCEPTED` | PR #8 Android JVM tests and lint passed; Unit 2 has not yet reached CI | 100% green tests & clean builds on the exact accepted revision | `developer-project-owner` / deliver Unit 2 to CI |
| Security/privacy | `PENDING` | Historical offline/no-secret claim exists; current diff still requires exact-tree review | Zero secret-bearing files, URLs, or network leaks | `developer-project-owner` / verify changed surfaces |
| Reliability/recovery | `PENDING` | Historical rollback claim exists; current integration path is not yet accepted | Reversible changes without state corruption | `developer-project-owner` / verify data and service boundaries |
| Performance | `PENDING` | Historical calculation timing exists; no current exact-tree measurement | Cold start <800ms; sub-15s driver quoting workflow | `developer-project-owner` / measure only if phase requires |
| Delivery | `INTEGRATION` | PR #8 is merged into `dev`; promotion to `main` is not yet authorized | Approved branch, CI, review, and merge flow | `developer-project-owner` / complete Unit 2 and later approve promotion |
| Developer experience | `PENDING` | Local feature files and governance docs are dirty; no accepted commit exists | CI provides the authoritative build and governance result | `developer-project-owner` / authorize CI delivery gate |

## Active blockers and risks

| Risk/blocker | Impact | Evidence | Owner | Resolution trigger |
|---|---|---|---|---|
| Canonical Linear project and issue are unavailable in the connected workspace | Progress is not mirrored to the declared external tracker | Local fallback and exception record in `EVIDENCE-LOG.md` | `developer-project-owner` | Revisit when the project is available |
| Unit 2 implementation is local-only | No exact Unit 2 revision is currently eligible for CI/review or release | Current branch and implementation-plan entry | `developer-project-owner` | Commit/push/open the Unit 2 PR after local implementation review |
| GitLab review-mirror cleanup is deferred | Advisory close cleanup currently returns HTTP 401; application delivery is unaffected | PR #8 post-merge workflow result | `developer-project-owner` | Revisit when GitLab review operations are resumed |
| Device/emulator UI validation is deferred | Accessibility and visual runtime behavior remain outside this documentation/build-contract unit | No `androidTest` suite was present in the baseline inventory | `developer-project-owner` | Run a separately approved UI audit |

## Intentional-removal delete-zone

| Removed path/concept | Why it must not return | Replacement | Evidence/decision | Revisit condition |
|---|---|---|---|---|
| None in this unit | No existing product or canonical documentation surface is being retired | Reconciled status and contract documents | Unit 2 evidence in `EVIDENCE-LOG.md` | Only with an explicitly approved retirement decision |

## Environment and release state

| Environment | Revision/artifact | Status | Last acceptance evidence | Owner |
|---|---|---|---|---|
| PR #2 source revision | `19ee2e271c9e38b51d09cf9f0aa4246b1acc0b2e` | `CURRENT REMOTE BASELINE` | GitHub `main`/`dev` parity and GitLab review-mirror parity were verified; direct-booking changes are not included | `developer-project-owner` |
| Release candidate `v0.1.0` | Existing tagged product release | Historical/current release reference; no deployment requested | Release measurements and hashes in `EVIDENCE-LOG.md` | `developer-project-owner` |

## Next gate

- Work completed: PRD approval, architecture plan, Unit 1 implementation, PR #8 merge, and
  exact-revision GitHub CI acceptance.
- Current work: Unit 2 permission and address fallback implementation under explicit approval.
- Required decision/evidence: GitHub CI must provide the Gradle tests, lint, build, and repository
  governance results for Unit 2 before declaring it complete. Branch, commit, push, PR, merge,
  promotion, and release remain separate approvals; no local test/build run will be performed.
