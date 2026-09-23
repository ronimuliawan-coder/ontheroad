# OnTheRoad Project Status

This is the current health and next-gate record for the Direct / Offline Booking & Fare Estimator
feature delivery. Durable evidence and approvals are recorded in
[`EVIDENCE-LOG.md`](EVIDENCE-LOG.md); repository authority and invariants remain in
[`GOVERNANCE.md`](GOVERNANCE.md).

- Last verified: `2026-09-23` (Asia/Jakarta; Unit 4 merged to `dev` after exact-revision CI passed)
- Latest accepted application revision: PR #14 merge `26882bbae0fe93b9b6d303d241b8b86dc1ca3c78`; `main` baseline `19ee2e271c9e38b51d09cf9f0aa4246b1acc0b2e`
- Repository governance lifecycle: Phase 10 complete; active feature delivery is tracked with implementation units
- Current work item: Unit 4 complete (Linear `RON-385`); no next implementation unit is authorized yet
- Overall health: `UNITS 1–4 ACCEPTED / NEXT UNIT AWAITS EXPLICIT APPROVAL`
- Tracker/project: Linear Project `OnTheRoad: Direct Booking & Fare Estimator` (`RON-277`, `RON-278`, `RON-279`, `RON-280`, `RON-282`, `RON-385`)

## Current objective

Deliver the on-the-spot / offline direct booking quoting engine, configurable per-profile rates,
address estimation, live GPS odometer cockpit integration, and shareable direct receipts while
preserving verified product behavior, data, pure Kotlin domain architecture (`ARCH-001`), and zero
secrets in code (`SEC-001`). Units 1–4 are accepted; any next implementation unit requires
separate explicit approval.

## Health and thresholds

| Area | Status | Current evidence | Target/threshold | Owner / next action |
|---|---|---|---|---|
| Correctness | `UNIT 4 ACCEPTED` | PR #14 exact-revision Governance and Android CI passed; Android CI includes JVM tests, lint, and Pixel 7 Pro/API 35 UI acceptance | Next approved unit's acceptance criteria | `developer-project-owner` / approve the next unit |
| Security/privacy | `ACCEPTED` | PR #14 uses a local generated image and receipt-only `FileProvider` cache path; no network service | No coordinates, route traces, notes, earnings internals, or external upload in receipts | `developer-project-owner` / retain the receipt allowlist |
| Reliability/recovery | `ACCEPTED` | Legacy scalar preferences are preserved; additive Room migration and compatibility behavior passed CI | Forward-only database recovery | `developer-project-owner` / retain migration coverage |
| Performance | `PENDING` | No current cold-start or quote-workflow measurement; Unit 3 added no performance instrumentation | Cold start <800ms; sub-15s driver quoting workflow | `developer-project-owner` / measure only if phase requires |
| Delivery | `INTEGRATED` | PR #14 merged into `dev` at `26882bbae0fe93b9b6d303d241b8b86dc1ca3c78`; `main` is unchanged | Approved branch, CI, review, and merge flow | `developer-project-owner` / approve the next unit or a separate promotion |
| Developer experience | `CI ACCEPTANCE` | Governance run `35865267091` and Android CI run `35865267102` passed on PR #14; jobs ran on Namespace | CI provides the authoritative build and governance result | `developer-project-owner` / preserve CI-only verification |

## Active blockers and risks

| Risk/blocker | Impact | Evidence | Owner | Resolution trigger |
|---|---|---|---|---|
| Next implementation unit is not yet approved | No additional feature scope may start | Unit 4 is complete and the approval boundary is recorded in `EVIDENCE-LOG.md` | `developer-project-owner` | Explicit approval of the next unit |

## Intentional-removal delete-zone

| Removed path/concept | Why it must not return | Replacement | Evidence/decision | Revisit condition |
|---|---|---|---|---|
| None in this unit | No existing product or canonical documentation surface is being retired | Reconciled status and contract documents | Unit 2 evidence in `EVIDENCE-LOG.md` | Only with an explicitly approved retirement decision |

## Environment and release state

| Environment | Revision/artifact | Status | Last acceptance evidence | Owner |
|---|---|---|---|---|
| GitHub `main` | `19ee2e271c9e38b51d09cf9f0aa4246b1acc0b2e` | `CURRENT RELEASE BASELINE` | v0.1.0 baseline; Unit 4 remains on `dev`; promotion has not been authorized | `developer-project-owner` |
| GitHub `dev` | `26882bbae0fe93b9b6d303d241b8b86dc1ca3c78` | `CURRENT INTEGRATION TIP / UNIT 4 ACCEPTED` | PR #14 exact-revision Governance and Android CI passed before merge | `developer-project-owner` |
| Release candidate `v0.1.0` | Existing tagged product release | Historical/current release reference; no deployment requested | Release measurements and hashes in `EVIDENCE-LOG.md` | `developer-project-owner` |

## Next gate

- Work completed: PRD approval; Units 1–4 implementation and acceptance; PR #14 merge at
  `26882bbae0fe93b9b6d303d241b8b86dc1ca3c78`.
- Current work: No new implementation unit is authorized. Unit 4 is complete; Linear issue `RON-385`
  is Done. Promotion to `main` remains unauthorized.
- Acceptance: Governance run `35865267091` and Android CI run `35865267102` passed on PR #14's exact
  revision; Android CI included JVM tests, lint, and Pixel 7 Pro / API 35 UI acceptance. CodeRabbit
  skipped review for non-default base `dev`; no review comments were present. No local test/build
  runs were performed, per the owner's standing direction.
- Terminology: governance **phases** are project-wide gates; feature **units** are implementation
  slices. We do not use “stage” as a tracking term.
