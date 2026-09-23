# OnTheRoad Project Status

This is the current health and next-gate record for the Direct / Offline Booking & Fare Estimator
feature delivery. Durable evidence and approvals are recorded in
[`EVIDENCE-LOG.md`](EVIDENCE-LOG.md); repository authority and invariants remain in
[`GOVERNANCE.md`](GOVERNANCE.md).

- Last verified: `2026-09-23` (Asia/Jakarta; Unit 3 start based on current `dev`)
- Accepted feature revision: PR #9 merge `685c530c090672ec6f55ba5221e6c4f5fd304727`; current `dev` baseline after PR #10 `579c14b6ac9b091d3425001aef4eeea7f0f6e724`; `main` baseline `19ee2e271c9e38b51d09cf9f0aa4246b1acc0b2e`
- Repository governance lifecycle: Phase 10 complete; active feature delivery is tracked with implementation units
- Current work item: Unit 3 — validated rate editing and UI acceptance — in progress
- Overall health: `UNITS 1–2 ACCEPTED / UNIT 3 IN PROGRESS`
- Tracker/project: Linear Project `OnTheRoad: Direct Booking & Fare Estimator` (`RON-277`, `RON-278`, `RON-279`, `RON-280`)

## Current objective

Deliver the on-the-spot / offline direct booking quoting engine, rate configurations in Settings,
address estimation, and live GPS odometer cockpit integration while preserving verified product
behavior, data, pure Kotlin domain architecture (`ARCH-001`), and zero secrets in code (`SEC-001`).
The product scope is approved; implementation acceptance is not yet established for the current
Unit 3 PR candidate.

## Health and thresholds

| Area | Status | Current evidence | Target/threshold | Owner / next action |
|---|---|---|---|---|
| Correctness | `UNITS 1–2 ACCEPTED / UNIT 3 IN PROGRESS` | PR #8 and PR #9 merge revisions passed Android JVM tests, lint, and governance CI | Unit 3 JVM, lint, and Android UI acceptance green on exact PR revision | `developer-project-owner` / complete Unit 3 CI acceptance |
| Security/privacy | `PENDING` | Historical offline/no-secret claim exists; current diff still requires exact-tree review | Zero secret-bearing files, URLs, or network leaks | `developer-project-owner` / verify changed surfaces |
| Reliability/recovery | `PENDING` | Historical rollback claim exists; current integration path is not yet accepted | Reversible changes without state corruption | `developer-project-owner` / verify data and service boundaries |
| Performance | `PENDING` | Historical calculation timing exists; no current exact-tree measurement | Cold start <800ms; sub-15s driver quoting workflow | `developer-project-owner` / measure only if phase requires |
| Delivery | `INTEGRATION` | PRs #8 and #9 plus documentation-only PR #10 are merged into `dev`; promotion to `main` is not yet authorized | Approved branch, CI, review, and merge flow | `developer-project-owner` / complete Unit 3, then consider promotion |
| Developer experience | `CI ACCEPTANCE` | Unit 2 passed post-merge GitHub CI on Namespace runners | CI provides the authoritative build and governance result | `developer-project-owner` / preserve CI-only verification |

## Active blockers and risks

| Risk/blocker | Impact | Evidence | Owner | Resolution trigger |
|---|---|---|---|---|
| Canonical Linear project and issue are unavailable in the connected workspace | Progress is not mirrored to the declared external tracker | Local fallback and exception record in `EVIDENCE-LOG.md` | `developer-project-owner` | Revisit when the project is available |
| Namespace emulator support is unconfirmed | The critical Settings save flow needs device-profile acceptance | Unit 3 adds a Pixel 7 Pro / API 35 instrumentation job on the existing Namespace runner profile | `developer-project-owner` | Require that exact CI job to pass before accepting Unit 3 |

## Intentional-removal delete-zone

| Removed path/concept | Why it must not return | Replacement | Evidence/decision | Revisit condition |
|---|---|---|---|---|
| None in this unit | No existing product or canonical documentation surface is being retired | Reconciled status and contract documents | Unit 2 evidence in `EVIDENCE-LOG.md` | Only with an explicitly approved retirement decision |

## Environment and release state

| Environment | Revision/artifact | Status | Last acceptance evidence | Owner |
|---|---|---|---|---|
| GitHub `main` | `19ee2e271c9e38b51d09cf9f0aa4246b1acc0b2e` | `CURRENT RELEASE BASELINE` | v0.1.0 baseline; Unit 3 promotion is not authorized | `developer-project-owner` |
| GitHub `dev` | `579c14b6ac9b091d3425001aef4eeea7f0f6e724` | `CURRENT INTEGRATION BASELINE` | Units 1–2 merged in PRs #8–#9; documentation-only PR #10 merged; Unit 3 CI pending | `developer-project-owner` |
| Release candidate `v0.1.0` | Existing tagged product release | Historical/current release reference; no deployment requested | Release measurements and hashes in `EVIDENCE-LOG.md` | `developer-project-owner` |

## Next gate

- Work completed: PRD approval, architecture plan, Unit 1 implementation, PR #8 merge, and
  Unit 2 implementation, PR #9 merge, and post-merge Android/governance CI acceptance.
- Current work: Unit 3 — validated rate editing and UI acceptance — is approved and in progress.
- Acceptance: JVM tests, lint, and the Pixel 7 Pro / API 35 UI acceptance job must pass in GitHub CI;
  local test/build runs remain disabled by the owner's standing direction.
- Terminology: governance **phases** are project-wide gates; feature **units** are implementation
  slices. We do not use “stage” as a tracking term.
