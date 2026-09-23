# OnTheRoad Project Status

This is the current health and next-gate record for the Direct / Offline Booking & Fare Estimator
feature delivery. Durable evidence and approvals are recorded in
[`EVIDENCE-LOG.md`](EVIDENCE-LOG.md); repository authority and invariants remain in
[`GOVERNANCE.md`](GOVERNANCE.md).

- Last verified: `2026-09-23` (Asia/Jakarta; Unit 4 approved and started from `dev`)
- Latest accepted application revision: PR #11 merge `a8ba7c87b06f62cb6e3f54525d741c891a65276d`; documentation-only PRs #12–#13 advanced `dev` to `7cbb0d3335e5e3dfaaa708a389687265086b7843`; `main` baseline `19ee2e271c9e38b51d09cf9f0aa4246b1acc0b2e`
- Repository governance lifecycle: Phase 10 complete; active feature delivery is tracked with implementation units
- Current work item: Unit 4 — configurable detour, multi-vehicle rate profiles, and shareable direct receipt (Linear `RON-385`)
- Overall health: `UNITS 1–3 ACCEPTED / UNIT 4 IN PROGRESS`
- Tracker/project: Linear Project `OnTheRoad: Direct Booking & Fare Estimator` (`RON-277`, `RON-278`, `RON-279`, `RON-280`, `RON-282`, `RON-385`)

## Current objective

Deliver the on-the-spot / offline direct booking quoting engine, configurable per-profile rates,
address estimation, live GPS odometer cockpit integration, and shareable direct receipts while
preserving verified product behavior, data, pure Kotlin domain architecture (`ARCH-001`), and zero
secrets in code (`SEC-001`). Units 1–3 are accepted; Unit 4 is approved and active.

## Health and thresholds

| Area | Status | Current evidence | Target/threshold | Owner / next action |
|---|---|---|---|---|
| Correctness | `UNIT 4 IN PROGRESS` | Unit 3 CI passed; Unit 4 acceptance is recorded in the implementation plan | Exact-revision GitHub CI for profile, persistence, receipt, and UI behavior | `developer-project-owner` / review CI and receipt behavior |
| Security/privacy | `IN SCOPE` | Unit 4 shares a local generated image through a receipt-only `FileProvider` cache path; no network service | No coordinates, route traces, notes, earnings internals, or external upload in receipts | `developer-project-owner` / review generated receipt and URI boundary |
| Reliability/recovery | `IN SCOPE` | Unit 4 preserves old scalar settings and uses additive Room migration | Safe first-run profile migration and forward-only database recovery | `developer-project-owner` / review CI migration evidence |
| Performance | `PENDING` | No current cold-start or quote-workflow measurement; Unit 3 added no performance instrumentation | Cold start <800ms; sub-15s driver quoting workflow | `developer-project-owner` / measure only if phase requires |
| Delivery | `INTEGRATION` | PRs #8–#11 app changes and docs PRs #12–#13 are on `dev`; Unit 4 branch is local; promotion to `main` is not authorized | Approved branch, CI, review, and merge flow | `developer-project-owner` / review Unit 4 CI |
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
| GitHub `dev` | `7cbb0d3335e5e3dfaaa708a389687265086b7843` | `CURRENT INTEGRATION TIP / UNIT 4 BASE` | PR #11 remains the latest accepted application code; docs-only PRs #12–#13 advanced the integration tip | `developer-project-owner` |
| Release candidate `v0.1.0` | Existing tagged product release | Historical/current release reference; no deployment requested | Release measurements and hashes in `EVIDENCE-LOG.md` | `developer-project-owner` |

## Next gate

- Work completed: PRD approval; Units 1–3 implementation and acceptance; PR #11 merge; and
  post-merge Android, governance, and GitLab trusted-ref CI on `a8ba7c87b06f62cb6e3f54525d741c891a65276d`.
- Current work: Unit 4 is approved and active on `rons/unit-4-direct-rate-profiles-receipts`, based
  on `7cbb0d3335e5e3dfaaa708a389687265086b7843`.
- Acceptance: Unit 3 JVM tests, lint, and Pixel 7 Pro / API 35 UI acceptance passed in GitHub CI.
  Unit 4 requires exact-revision CI for its documented acceptance criteria; local test/build runs
  remain disabled by the owner's standing direction. Promotion to `main` remains unauthorized.
- Terminology: governance **phases** are project-wide gates; feature **units** are implementation
  slices. We do not use “stage” as a tracking term.
