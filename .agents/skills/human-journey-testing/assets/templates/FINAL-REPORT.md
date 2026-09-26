# Human journey testing — results

Replace instructions below with evidence from the run ledger. Report scope limitations before
percentages. Keep this report self-contained and link the detailed ledger and actual evidence.

## Outcome

- Scope: target/environment, observed build or unknown, dates, actual roles, devices/input,
  discovery mode and inventory revision.
- Inventory reconciliation: reconciled / unresolved, with candidate and discovery gaps.
- Execution: complete within inventoried scope / incomplete, with reason.
- Observed quality: failures found / no failures observed, qualified by incomplete coverage.
- Main user-visible defects and the most consequential untested behavior.
- Actual tester mode/identity, fallback reason if any, review method/outcome and evidence link.
- Effective policy link; QA trigger and DUE/DEFERRED decision; current prompt overrides.
- Requested tester model/effort and actual or UNVERIFIED, with dispatch/fallback evidence.
- Promotion PR/head/base/artifact mapping and final freshness check, when applicable.
- Engineering phase/acceptance-plan and evidence-log links when integrated; approval remains
  with that engagement's owner.

Do not label this “whole project passed” when roles, states, or sources were inaccessible.
Even complete execution can contain FAIL results. This report does not authorize release.

## Coverage

State the completion boundary before counting scenarios: inspected product/source areas,
roles and verified application workspaces, environment/build identity, configuration/plan
conditions, devices/input, scenario matrix, and sampling/exclusions. Show uninspected or
inaccessible areas and unresolved discovery candidates explicitly. Scope limitations are not
permission to omit otherwise discoverable in-scope features.

| Scope / actual role | Features / fully exercised features | NOT_RUN | PASS | FAIL | BLOCKED | INCONCLUSIVE | NOT_APPLICABLE | Execution E/A | Passing PASS/A |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |

Include an overall row and one per actual role. For multi-role scenarios, define one owning
role for counts and link participating roles; avoid double-counting the overall total.
T is the sum of all six statuses; A = T − justified NOT_APPLICABLE; E = PASS + FAIL.
Show fractions with denominators; if A = 0, say no applicable scenarios established.
These are scenario counts within the inventory, not proof that all product behavior is known.

List every feature that is not fully exercised, linking its outstanding scenarios. Link the
coverage-family decisions, sampling/equivalence rationale, and all exclusions with their basis.

## Journeys and findings

- Customer/end-user outcomes and evidence.
- Staff/operator outcomes by actual permission level and evidence.
- Cross-role handoff outcomes, same-record evidence, and blocked dependencies.
- QA perspective: alternate paths, validation/recovery, permissions, presentation/input checks.

| Defect ID / severity | User impact | UI reproduction / context | Expected vs observed | Evidence | Retest state |
| --- | --- | --- | --- | --- | --- |

Separate observed functional failures from usability suggestions and unverified hypotheses.

## Access and real-world evidence

| Role/account alias | Verified application role/workspace and session | Sign-in/access blocker category and observed reason | Affected scenarios / required user action |
| --- | --- | --- | --- |

Do not infer a missing role from a missing provider login. Link the session map and preserve
credentials outside the artifacts. Note sequential switching versus genuinely isolated sessions.

| Outcome/scenario | UI_OBSERVED / SIMULATED / PHYSICAL_OBSERVED | Actual evidence and observer | Required real-world outcome still unverified |
| --- | --- | --- | --- |

Print dialog/preview and simulation cannot pass a paper-output scenario. A human-assisted
physical check identifies the observer and evidence; absent required observation remains a gap.

## Missing coverage and closure evidence

List BLOCKED, INCONCLUSIVE, NOT_RUN, unresolved candidates, absent accounts/devices/fixtures,
unknown build identity, and unsupported expectations. Identify what would unblock each gap.
Summarize the final independent sweep, new features it found, how they were handled, and the
last no-new-item sweep if one occurred. If time ran out, say so and retain the queue.

## Reuse and continuation

- Uncertain consequential actions, reconciliation evidence, and safe next action/owner.
- Effective policy/checkpoint links; preserve these on RESUME or model/provider changes.
- Optional ledger checker: command/result/index revision; bookkeeping only, never UI proof.

- Ledger and evidence links; final inventory revision.
- Retest targets and conditions under which prior results must be reopened.
- Exact next scenario/action if incomplete; prerequisite or owner input required.
- UI-created fixtures/settings cleaned up and remaining residue.
