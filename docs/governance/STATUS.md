# OnTheRoad Project Status

This is the current health and next-gate record for the Direct / Offline Booking & Fare Estimator
feature delivery. Durable evidence and approvals are recorded in
[`EVIDENCE-LOG.md`](EVIDENCE-LOG.md); repository authority and invariants remain in
[`GOVERNANCE.md`](GOVERNANCE.md).

- Last verified: `2026-09-23` (Asia/Jakarta; Unit 3 accepted on merged `dev`)
- Accepted feature revision: PR #11 merge `a8ba7c87b06f62cb6e3f54525d741c891a65276d`; current `dev` baseline is the same revision; `main` baseline `19ee2e271c9e38b51d09cf9f0aa4246b1acc0b2e`
- Repository governance lifecycle: Phase 10 complete; active feature delivery is tracked with implementation units
- Current work item: None; Unit 3 — validated rate editing and UI acceptance — is complete
- Overall health: `UNITS 1–3 ACCEPTED / AWAITING NEXT UNIT APPROVAL`
- Tracker/project: Linear Project `OnTheRoad: Direct Booking & Fare Estimator` (`RON-277`, `RON-278`, `RON-279`, `RON-280`)

## Current objective

Deliver the on-the-spot / offline direct booking quoting engine, rate configurations in Settings,
address estimation, and live GPS odometer cockpit integration while preserving verified product
behavior, data, pure Kotlin domain architecture (`ARCH-001`), and zero secrets in code (`SEC-001`).
The approved Units 1–3 are implemented and accepted; any additional implementation scope requires
its own explicit approval.

## Health and thresholds

| Area | Status | Current evidence | Target/threshold | Owner / next action |
|---|---|---|---|---|
| Correctness | `UNITS 1–3 ACCEPTED` | PR #11 merge revision passed JVM tests, Android lint, Pixel 7 Pro UI acceptance, and governance CI | Preserve exact-revision GitHub CI acceptance for future approved units | `developer-project-owner` / approve any next unit |
| Security/privacy | `PENDING` | Unit 3 introduced no credential, external-network, or release surface; no standalone security review was performed | Zero secret-bearing files, URLs, or network leaks | `developer-project-owner` / review if future scope changes trust boundaries |
| Reliability/recovery | `PENDING` | Unit 3 persistence and UI regressions passed CI; no separate recovery audit was performed | Reversible changes without state corruption | `developer-project-owner` / review if future scope changes data boundaries |
| Performance | `PENDING` | No current cold-start or quote-workflow measurement; Unit 3 added no performance instrumentation | Cold start <800ms; sub-15s driver quoting workflow | `developer-project-owner` / measure only if phase requires |
| Delivery | `INTEGRATION` | PRs #8–#11 are merged into `dev`; promotion to `main` is not authorized | Approved branch, CI, review, and merge flow | `developer-project-owner` / choose the next approved scope |
| Developer experience | `CI ACCEPTANCE` | Unit 3 passed post-merge GitHub CI on Namespace runners | CI provides the authoritative build and governance result | `developer-project-owner` / preserve CI-only verification |

## Active blockers and risks

| Risk/blocker | Impact | Evidence | Owner | Resolution trigger |
|---|---|---|---|---|
| Canonical Linear project and issue are unavailable in the connected workspace | Progress is not mirrored to the declared external tracker | Local fallback and exception record in `EVIDENCE-LOG.md` | `developer-project-owner` | Revisit when the project is available |

## Intentional-removal delete-zone

| Removed path/concept | Why it must not return | Replacement | Evidence/decision | Revisit condition |
|---|---|---|---|---|
| None in this unit | No existing product or canonical documentation surface is being retired | Reconciled status and contract documents | Unit 2 evidence in `EVIDENCE-LOG.md` | Only with an explicitly approved retirement decision |

## Environment and release state

| Environment | Revision/artifact | Status | Last acceptance evidence | Owner |
|---|---|---|---|---|
| GitHub `main` | `19ee2e271c9e38b51d09cf9f0aa4246b1acc0b2e` | `CURRENT RELEASE BASELINE` | v0.1.0 baseline; Unit 3 promotion is not authorized | `developer-project-owner` |
| GitHub `dev` | `a8ba7c87b06f62cb6e3f54525d741c891a65276d` | `CURRENT INTEGRATION BASELINE` | PR #11 merge; post-merge Android CI, governance, and GitLab trusted-ref checks passed | `developer-project-owner` |
| Release candidate `v0.1.0` | Existing tagged product release | Historical/current release reference; no deployment requested | Release measurements and hashes in `EVIDENCE-LOG.md` | `developer-project-owner` |

## Next gate

- Work completed: PRD approval; Units 1–3 implementation and acceptance; PR #11 merge; and
  post-merge Android, governance, and GitLab trusted-ref CI on `a8ba7c87b06f62cb6e3f54525d741c891a65276d`.
- Current work: No implementation unit is active. The next unit requires separate explicit approval.
- Acceptance: Unit 3 JVM tests, lint, and Pixel 7 Pro / API 35 UI acceptance passed in GitHub CI;
  local test/build runs remain disabled by the owner's standing direction. Promotion to `main`
  remains unauthorized.
- Terminology: governance **phases** are project-wide gates; feature **units** are implementation
  slices. We do not use “stage” as a tracking term.
