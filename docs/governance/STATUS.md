# OnTheRoad Project Status

This is the current health and next-gate record for the Direct / Offline Booking & Fare Estimator
feature delivery. Durable evidence and approvals are recorded in
[`EVIDENCE-LOG.md`](EVIDENCE-LOG.md); repository authority and invariants remain in
[`GOVERNANCE.md`](GOVERNANCE.md).

- Last verified: `2026-09-23` (Asia/Jakarta; PR #9 merge and post-merge GitHub CI)
- Accepted feature revision: PR #9 merge `685c530c090672ec6f55ba5221e6c4f5fd304727`; `main` baseline `19ee2e271c9e38b51d09cf9f0aa4246b1acc0b2e`
- Repository governance lifecycle: Phase 10 complete; active feature delivery is tracked with implementation units
- Current work item: Unit 2 complete; Unit 3 awaits explicit implementation approval
- Overall health: `UNITS 1–2 ACCEPTED / UNIT 3 NOT STARTED`
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
| Correctness | `UNITS 1–2 ACCEPTED` | PR #8 and PR #9 merge revisions passed Android JVM tests, lint, and governance CI | 100% green tests & clean builds on the exact accepted revision | `developer-project-owner` / approve Unit 3 |
| Security/privacy | `PENDING` | Historical offline/no-secret claim exists; current diff still requires exact-tree review | Zero secret-bearing files, URLs, or network leaks | `developer-project-owner` / verify changed surfaces |
| Reliability/recovery | `PENDING` | Historical rollback claim exists; current integration path is not yet accepted | Reversible changes without state corruption | `developer-project-owner` / verify data and service boundaries |
| Performance | `PENDING` | Historical calculation timing exists; no current exact-tree measurement | Cold start <800ms; sub-15s driver quoting workflow | `developer-project-owner` / measure only if phase requires |
| Delivery | `INTEGRATION` | PRs #8 and #9 are merged into `dev`; promotion to `main` is not yet authorized | Approved branch, CI, review, and merge flow | `developer-project-owner` / complete the remaining approved units, then consider promotion |
| Developer experience | `CI ACCEPTANCE` | Unit 2 passed post-merge GitHub CI on Namespace runners | CI provides the authoritative build and governance result | `developer-project-owner` / preserve CI-only verification |

## Active blockers and risks

| Risk/blocker | Impact | Evidence | Owner | Resolution trigger |
|---|---|---|---|---|
| Canonical Linear project and issue are unavailable in the connected workspace | Progress is not mirrored to the declared external tracker | Local fallback and exception record in `EVIDENCE-LOG.md` | `developer-project-owner` | Revisit when the project is available |
| Unit 3 is awaiting approval | Rate editing and UI acceptance work has not started | Approved PRD and implementation plan | `developer-project-owner` | Explicitly approve Unit 3 implementation |
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
  Unit 2 implementation, PR #9 merge, and post-merge Android/governance CI acceptance.
- Current work: Unit 3 — validated rate editing and UI acceptance — is planned but not started.
- Required decision: explicit project-owner approval to begin Unit 3. Its tests/build/UI acceptance
  will run in GitHub CI; local test/build runs remain disabled by the owner's standing direction.
- Terminology: governance **phases** are project-wide gates; feature **units** are implementation
  slices. We do not use “stage” as a tracking term.
