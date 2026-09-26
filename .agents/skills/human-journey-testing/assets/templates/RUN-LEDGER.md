# Human journey testing — run ledger

Copy into the chosen run directory. Replace guidance with observed facts; empty tables are
intentional. Split large tables into linked files if needed, preserving IDs and relationships.
Never populate PASS from planned steps, source inspection, or previous unrelated runs.

## Run context

| Field | Value |
| --- | --- |
| Run ID / started / last updated / timezone | |
| Target URL or app / environment | |
| Observed build/version and evidence; unknown if unavailable | |
| Requested scope and explicit exclusions | |
| Discovery mode / available sources / unavailable sources | |
| Actual roles and account aliases; no credentials | |
| Workspace/data aliases and fixture provenance | |
| Browser/app, viewport/device, input and assistive capabilities | |
| Emulation vs actual hardware | |
| Authorized side effects / actions needing authorization | |
| Evidence directory and sensitive-data handling | |
| Inventory revision and last reconciliation | |

## Ownership and engineering context

- Requested/actual mode: DEDICATED_QA or SAME_AGENT; fallback reason if any:
- Coordinator/tester aliases and assigned execution roles; no recursive delegation:
- Tester UI capability confirmed; exclusive session/artifact ownership and handoff state:
- Current approved engineering phase/scope and acceptance-plan link, if integrated:
- Criteria → feature/scenario mapping; deployment/build authority and context changes:
- Canonical evidence-log/tracker link and posting authorization, if applicable:

## Roles, accounts, and sessions

Before filling session details, resolve the policy below. For integrated work, link the one
canonical engagement policy instead of maintaining an independently editable copy.

### Effective policy and trigger

- Canonical policy location / revision:
- Current trigger decision: DUE / DEFERRED; observed basis, phase prerequisite, next trigger:
- Trigger event/request, campaign ID/scope/context and pending/in-progress/finished state:
- Requested tester model/effort; actual model/effort or UNVERIFIED; dispatch evidence:
- Runtime capability/fallback reason and actual execution mode:

| Setting | Effective value | Source (current prompt / saved policy / default) | Change reason/date |
| --- | --- | --- | --- |
| tester_mode | DEDICATED_QA | | |
| tester_model / tester_reasoning | gpt-5.6-luna / xhigh | | |
| qa_trigger | AUTO / PROMOTION_ONLY / MANUAL — resolve one | | |
| discovery / scope / roles / devices | | | |
| target / permitted side effects | | | |
| promotion_rule / evidence_reuse | | | |

| Temporary override ID | Baseline setting/value | Override value and source | Campaign/scope | Expiry event / still active? | Value to restore |
| --- | --- | --- | --- | --- | --- |

Preserve temporary overrides through HANDOFF; restore the baseline at their recorded expiry.
An unqualified preference persists; a one-campaign override does not replace the baseline.

### Promotion identity, when applicable

| Checkpoint/time | PR/repository/source/target refs | Head SHA / base SHA | Preview or merge artifact and relationship to candidate | Running build/configuration evidence | Current / stale / unknown; reopened scenarios |
| --- | --- | --- | --- | --- | --- |

Check before execution and final acceptance. An unknown mapping blocks promotion acceptance;
preserve observations and old identities. See RUN-POLICY.md for change/retest handling.

### Session map

Use actual session handles where available. Different tabs are not proof of isolated auth.
Provider sign-in and intended role are inputs; observed application role/workspace is evidence.

| Intended role | Account alias | App workspace | Browser/profile/session handle | Isolation/shared control and tester | Observed app identity/role evidence and time | Access state/blocker category | Scenario IDs / pending user action |
| --- | --- | --- | --- | --- | --- | --- | --- |

Blocker categories: SIGN_IN_REQUIRED, WRONG_ACCOUNT, MISSING_PERMISSION, ACCOUNT_UNAVAILABLE,
ROLE_UNVERIFIED, SESSION_TOOL_UNAVAILABLE. Scenario statuses remain unchanged. Record the
specific observed obstacle and request only its missing remedy; keep other-role work moving.
Checkpoint before switching; record actual identity/role/workspace after switching or resuming.

## Discovery source register

Use UI screen/role references, help/requirement URLs, or focused file references. Record
current-version relevance, contradictions, and inaccessible sources. Keep expectations
separate from observations. A source cannot serve as UI pass evidence.

| Source ID | Product area and inspected code/docs/UI references | Date/version relevance | Roles/configuration/plan conditions inspected | Candidates found | Unavailable/uninspected areas or contradictions; next action |
| --- | --- | --- | --- | --- | --- |

Include all in-scope product areas, even when their source or runtime context is unavailable.
Record the discovery gap instead of treating absent inspection as coverage. Under UI-only
mode, explicitly identify source inspection as excluded by request. Record systematic source
coverage by relevant area/reference, not a claim that every line was read or executed.

## Surfaces

| Surface ID | UI entry point / navigation path | Roles and contexts inspected | Controls/capabilities → feature IDs | Uninspected states or gaps |
| --- | --- | --- | --- | --- |

## Feature inventory

Keep stable feature IDs such as F-001; retain later additions and exclusions. Each feature
means a distinct user outcome/action. Link parent/child features if a broad capability needs
splitting. Candidate dispositions: CURRENT (scenario-linked), EXCLUDED (evidenced reason), or
UNRESOLVED (discovery gap). EXCLUDED never means merely inaccessible.

| Feature ID | Capability/action | Surface/source IDs | Roles and meaningful states | Candidate disposition and basis | Scenario IDs |
| --- | --- | --- | --- | --- | --- |

## Coverage family decisions

Make a row for each family in COVERAGE-DISCOVERY.md. Record applicable features/scenarios,
irrelevance with a basis, or a discovery gap. For relevant feature dimensions (success,
invalid/empty/boundary input, cancellation, persistence, permission, recovery), link variants
or explain omissions in the next table. Do not turn a missing fixture into irrelevance.

| Family | Applicable / irrelevant / unresolved | Feature/scenario IDs or reason/source |
| --- | --- | --- |

## Feature dimensions and combination decisions

| Feature ID | Relevant dimensions and values | Scenario IDs | Omitted dimensions / equivalent instances / sampled combinations and rationale | Remaining gaps |
| --- | --- | --- | --- | --- |

## Scenario index

Use stable scenario IDs such as S-001. Status is exactly NOT_RUN, PASS, FAIL, BLOCKED,
INCONCLUSIVE, or NOT_APPLICABLE. Each row represents one defined role/context/variant; do
not overwrite a desktop scenario with a mobile attempt. The index holds the current status;
the attempt records below retain history and the evidence behind it.

For future-phase planning use NOT_RUN and record the phase prerequisite; when execution is due
but cannot proceed, use BLOCKED. On a target-build change, preserve IDs/history and reset
affected current rows to NOT_RUN (or BLOCKED for a known current prerequisite). If impact is
unknown, reopen all in-scope rows. Label retained historical evidence with its original build
and reuse basis; required new-build checks need new execution. Attempts are not extra scenarios.

| Scenario ID | Feature IDs | Actual role/account alias | Variant/context | Dependencies | Current status | Latest attempt / evidence | Defect, blocker, or applicability reason |
| --- | --- | --- | --- | --- | --- | --- | --- |

### Scenario definition and attempt record — duplicate per scenario/attempt

- Scenario ID / attempt ID / timestamp:
- Feature and source IDs:
- Actual role/account alias, environment/build, device/input context:
- Preconditions and UI-visible record alias; how fixture was prepared:
- Expected intermediate and final outcomes, with source or labeled assumption:
- Human UI steps actually taken (separate from steps not yet executed):
- Observed intermediate and final outcomes:
- Persistence / recipient / output check required and actually performed:
- Evidence references and what each establishes:
- Output basis where relevant: UI_OBSERVED / SIMULATED / PHYSICAL_OBSERVED; observer/source:
- Real-world outcome still required but unobserved; separate scenario/blocker ID:
- Status and reason; defect/blocker IDs; expected-behavior question if unresolved:
- New features/state branches discovered and queued:
- Cleanup or residue:

## Cross-role journeys

| Journey ID / human goal | Record alias | Initiator scenario | Recipient/next role scenarios | Return/receipt scenario | Chain outcome and dependencies |
| --- | --- | --- | --- | --- | --- |

## Defects and blockers

Do not duplicate one defect across every dependent scenario. A blocker gets an owner or
needed input when known. Severity follows observed impact, separate from confidence.

| ID | Kind / affected scenarios | Role/context and UI reproduction | Expected + basis vs actual | Impact/severity | Evidence | Needed action / retest attempts |
| --- | --- | --- | --- | --- | --- | --- |

## Evidence index

Evidence may be a screenshot, UI-tool observation, screen recording, or precise contemporaneous
manual transcript. Use actual file/tool references and a descriptive observation, not invented
filenames. Prefer captured UI evidence for decisive states where the tool supports it. Redact
secrets and personal data; note when capture was intentionally withheld and what can be proved.

| Evidence ID | Actual artifact or observation reference | Timestamp / build / role | Scenario attempts | What is visible and supported | Redactions / limitations |
| --- | --- | --- | --- | --- | --- |

## Reconciliation log

| Sweep ID / time | Roles, surfaces, sources rechecked and order | Unmapped items / new feature IDs | Scenario or disposition updates | No-new-item pass? / unresolved gaps |
| --- | --- | --- | --- | --- |

## Coordinator evidence review

- Reviewer alias / date / reviewed inventory revision and target context:
- Report, source inventory, scenario rows and evidence references inspected:
- Requested scope, actual roles, candidate dispositions, and status/count reconciliation:
- Directly inspected material outcomes, handoffs/failures, and any UI spot-check evidence:
- Missing evidence/new candidates returned to tester and resulting status/attempt updates:
- Artifact/browser ownership handoff for follow-up and completion:
- Review method: COORDINATOR_REVIEW / SELF_REVIEW (SAME_AGENT); reviewer alias:
- Review outcome: VERIFIED / FOLLOW_UP_REQUIRED; rationale and outstanding follow-up:
- Final report limits and engineering evidence links, if applicable:

VERIFIED means the report is supported, not that all scenarios passed or release is approved.
Missing required PASS evidence reopens the scenario as INCONCLUSIVE pending follow-up.

## Counts at checkpoint

Record NOT_RUN / PASS / FAIL / BLOCKED / INCONCLUSIVE / NOT_APPLICABLE, summing to T.
Record A = T − justified NOT_APPLICABLE, E = PASS + FAIL; show E/A and PASS/A, or “no
applicable scenarios established.” Recompute after additions; never preserve a stale denominator.

| Role | Features / fully exercised features | NOT_RUN | PASS | FAIL | BLOCKED | INCONCLUSIVE | NOT_APPLICABLE | E/A | PASS/A |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |

## Continuation and cleanup

### Consequential action reconciliation

| Intent ID / time / scenario | Actor/session and record alias | Intended action and expected visible result | CONFIRMED_SUCCEEDED / CONFIRMED_FAILED / OUTCOME_UNKNOWN | UI evidence, persistence/recipient check | Safe next action and owner |
| --- | --- | --- | --- | --- | --- |

Record intent before submission. Preserve uncertain actions across interruptions; inspect the
same record before any retry. Success of a click/toast does not prove the intended final outcome.

### Continuation packet

- Effective policy link/revision and DUE/DEFERRED trigger decision:
- Engineering session checkpoint link, when applicable:
- Optional canonical ledger-index path and artifact-check result/limitations:

- Current inventory revision and the last completed attempt/sweep:
- Current tester identity/mode, artifact/session owner, pending authentication request:
- Last observed UI location, role/session alias, and record state (no session tokens):
- Exact next scenario and next UI action; remaining queue:
- Blocking questions, missing fixtures/accounts/tools, and independent work still possible:
- Changes in build/environment/data that require results to be reopened:
- Created/modified record aliases, original settings where needed, cleanup through UI:
- Remaining residue, permitted next action, and owner if known:
