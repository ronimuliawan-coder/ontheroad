# OnTheRoad Project Status

This is the current health and next-gate record for the Direct / Offline Booking & Fare Estimator
feature delivery. Durable evidence and approvals are recorded in
[`EVIDENCE-LOG.md`](EVIDENCE-LOG.md); repository authority and invariants remain in
[`GOVERNANCE.md`](GOVERNANCE.md).

- Last verified: `2026-09-23` (Asia/Jakarta; repository-state reconciliation only)
- Exact revision/environment: Local `dev` working tree with uncommitted feature changes; remote `dev`/`main` at `19ee2e271c9e38b51d09cf9f0aa4246b1acc0b2e`
- Current phase/gate: Phase 1 baseline + Phase 3 architecture plan `COMPLETE`; Unit 1 implementation `IN PROGRESS`
- Overall health: `IMPLEMENTATION IN PROGRESS / CI VALIDATION PENDING`
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
| Correctness | `PENDING` | Earlier local observations are informational only; current acceptance is deferred to CI on the exact revision | 100% green tests & clean builds on the exact accepted revision | `developer-project-owner` / authorize CI delivery gate |
| Security/privacy | `PENDING` | Historical offline/no-secret claim exists; current diff still requires exact-tree review | Zero secret-bearing files, URLs, or network leaks | `developer-project-owner` / verify changed surfaces |
| Reliability/recovery | `PENDING` | Historical rollback claim exists; current integration path is not yet accepted | Reversible changes without state corruption | `developer-project-owner` / verify data and service boundaries |
| Performance | `PENDING` | Historical calculation timing exists; no current exact-tree measurement | Cold start <800ms; sub-15s driver quoting workflow | `developer-project-owner` / measure only if phase requires |
| Delivery | `BLOCKED` | Feature is local-only; remote `dev` and `main` remain at the pre-feature commit | Approved branch, CI, review, and merge flow | `developer-project-owner` / separate delivery approval |
| Developer experience | `PENDING` | Local feature files and governance docs are dirty; no accepted commit exists | CI provides the authoritative build and governance result | `developer-project-owner` / authorize CI delivery gate |

## Active blockers and risks

| Risk/blocker | Impact | Evidence | Owner | Resolution trigger |
|---|---|---|---|---|
| Canonical Linear project and issue are unavailable in the connected workspace | Progress is not mirrored to the declared external tracker | Local fallback and exception record in `EVIDENCE-LOG.md` | `developer-project-owner` | Revisit when the project is available |
| Feature implementation is uncommitted and not present on remote `dev` | No exact revision is currently eligible for CI/review or release | Current Git status and reconciliation entry in `EVIDENCE-LOG.md` | `developer-project-owner` | Complete Unit 1 validation, then separately authorize delivery |
| CI validation has not been triggered | No branch, commit, or PR is authorized yet | GitHub CI is the sole acceptance authority | `developer-project-owner` | Authorize external delivery after reviewing the diff |
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

- Work completed: PRD approval, Phase 1 baseline/reconciliation, and Phase 3 architecture/implementation plan in [11-direct-booking-and-fare-estimator.plan.md](../../.claude/PRPs/plans/11-direct-booking-and-fare-estimator.plan.md).
- Work explicitly not accepted: Unit 1 application changes are implemented locally but have not
  passed CI on an exact remote revision.
- Approval needed from: `developer-project-owner` for the branch/commit/push/PR gates that
  trigger CI; no local test/build run will be performed.
- Required decision/evidence: GitHub CI must provide the Gradle tests, lint, build, and repository
  governance results before declaring Unit 1 complete. Branch, commit, push, PR, merge, and
  release remain separate approvals.
