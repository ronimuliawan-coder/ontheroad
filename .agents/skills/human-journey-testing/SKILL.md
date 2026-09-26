---
name: human-journey-testing
description: "Test a project's human-facing features through its visible interface as customers, staff, and a QA tester, using a reconciled feature inventory and evidence ledger to prevent omissions. Use for comprehensive manual-style acceptance testing, role journeys, or UI-only exploratory QA."
---

# Human Journey Testing

Test human outcomes by operating the interface, one observed action at a time. This skill is
language-, framework-, domain-, and tool-agnostic. Browser and native applications work when
the available tools can operate their interfaces. A headless library or API-only service has
no human UI to exercise: report that limitation instead of silently changing methods.

The deliverable is an auditable account of discovered features, scenarios, observations,
defects, and gaps. Never promise exhaustive coverage of unknown features or all combinations.

## Run ownership and integration

Read [run policy](references/RUN-POLICY.md) before dispatch. Resolve and persist QA trigger,
scope, roles, target, permitted actions, and model settings; preserve prompt overrides across
sessions. Default tester model is **gpt-5.6-luna with xhigh reasoning**, unless the prompt
overrides it. The trigger defaults to AUTO; PROMOTION_ONLY and MANUAL constrain execution.
Evaluate the trigger and current authority before spawning a UI tester.

For direct skill invocation and the workflow alike, default to **DEDICATED_QA**: the primary
agent coordinates one fresh tester subagent and reviews its evidence before reporting. Read
[orchestration](references/ORCHESTRATION.md) before dispatch. Use SAME_AGENT when requested,
when delegation is unavailable/disallowed, or when UI access cannot be handed to the tester;
record the reason. Do not create a separate user-owned task or enable paid/remote services.

An agent assigned `execution role: TESTER` executes the campaign itself and must not spawn
another tester. In SAME_AGENT mode the current agent performs both testing and reconciliation,
with that independence limitation and any difference from the requested tester model disclosed.

When this campaign is part of high-assurance-engineering, also read
[engineering integration](references/ENGINEERING-INTEGRATION.md). That engagement's current
approved phase, scope, and release authority remain in force. Standalone testing does not
activate high-assurance-engineering or add its gates.

## Method boundary

- Use interactive computer/browser tools to see, click, tap, type, scroll, drag, navigate,
  upload/download through dialogs, and use keyboard shortcuts like a person. Tool wrappers
  that call a documented click/type/screenshot operation are allowed; generated test programs
  are not. Observe the result before choosing the next meaningful action.
- Screenshots and accessibility snapshots of the rendered UI may guide interaction. Check
  visual appearance in screenshots; an accessibility tree alone cannot prove layout quality.
- Do not write or run test scripts, test runners, crawlers, automated assertions, API calls,
  direct database queries, request interception, browser evaluation/injected JavaScript,
  internal-state inspection, or console commands to exercise or verify product behavior.
  Do not use CI, unit tests, or source code as evidence that a scenario passed.
- Read-only documentation/source discovery is allowed by default **only to find candidate
  features and expected behavior**. Honor a request for strict UI-only discovery. Source
  availability never substitutes for observing the deployed UI.
- Local file operations may maintain the report and copy templates. They must not create
  product state or bypass UI setup. Use existing safe fixtures or create records through UI;
  ask the owner for missing fixtures when the UI cannot create them.
- If the harness lacks a required UI capability, mark affected scenarios BLOCKED. Do not
  substitute programmatic tests. Report browser emulation as emulation, not real-device proof.

## 1. Establish the run

Reuse known answers and existing authorization. Resolve only missing information that affects
the next action; continue independent discovery while waiting.

Record the target URL/app and environment, observed build/version if available, timestamp,
scope, discovery mode, actual product roles, account aliases, data/workspaces, device/input
contexts, allowed side effects, and artifact directory. Unknown fields stay unknown. A local
commit is not proof of the version running at a remote URL.

For authenticated/multi-role testing, read [accounts and sessions](references/ACCOUNTS-AND-SESSIONS.md).
Map intended roles to account aliases, application workspaces, and actual browser/session
handles; verify the observed application role before role-specific execution. Identity-provider
sign-in alone does not establish application access. Preserve existing signed-in sessions.

For “whole project,” discover all human-facing surfaces and actual roles; do not arbitrarily
choose the three most obvious pages or collapse all staff into an administrator. Customer,
staff, and QA are testing perspectives; QA is not an invented privileged product account.
For role-only requests, keep that role's full scope and record untested dependencies on others.

Prefer a test environment and dedicated test accounts. Normal reversible UI operations within
the requested test scope need no repeated permission. Identify payments, real messages,
physical fulfillment, irreversible deletion, and third-party effects before committing them.
Use existing explicit authorization; otherwise stop just that action, record the concrete
blocker, and continue safe work. A staging URL alone does not prove its integrations are safe.
Do not silently switch accounts, environments, feature flags, or plans to obtain a pass.

Create a run directory using [the ledger template](assets/templates/RUN-LEDGER.md), adapting
its format to existing project records without dropping the required relationships. Keep
credentials and tokens out of artifacts; use aliases and redact sensitive UI evidence.

## 2. Discover before executing the full campaign

Read [coverage discovery](references/COVERAGE-DISCOVERY.md). Build two linked inventories:

1. Surfaces and capabilities: every discovered entry point, feature, action, role restriction,
   and meaningful state branch, with stable IDs and discovery sources.
2. Scenarios: specific role/context, preconditions, human steps, expected observable outcome,
   and evidence requirements for each applicable capability and variant.

First reconcile the live interface against independently available requirements/help/docs or
source candidates. Under UI-only discovery, use separate navigation-led and task-led sweeps.
Record inaccessible or contradictory candidates instead of removing them. Future/retired
features are not current requirements merely because a document mentions them.

In the default discovery mode, systematically inspect available code/docs relevant to every
in-scope product area, not just files for the first visible journey. Record inspected areas,
references, role/configuration/plan conditions, and unavailable or uninspected areas in the
discovery register. Uncertainty is a gap to resolve or disclose, not permission to skip work.

Assign every discovered control or capability to a scenario or an explained applicability
decision. Split independently failing actions: “manage records” is not one test covering
create, view, edit, archive, restore, search, and export. Identical repeated instances can share
coverage only when the equivalence and sampled data are recorded; exceptions need scenarios.

Start with this baseline, then expand it whenever exploration reveals another feature or
state. Never freeze the inventory to keep a progress percentage high. Risk controls execution
order; it does not silently remove low-risk features from a whole-project request.

## 3. Execute through the UI

Read [persona lenses](references/PERSONA-LENSES.md). Exercise natural end-to-end tasks for
each actual role, then use focused UI scenarios to cover actions and branches those journeys
missed. Include handoffs between roles and later return visits, not just isolated screens.

For every scenario:

1. Confirm the current role, environment, viewport/input context, record identity, and
   prerequisites from the visible interface. Use separate sessions for incompatible roles.
2. Record an expected observable result **before** the decisive action. Cite requirements,
   UI promises, or an explicitly labeled assumption. If the expected behavior is unresolved,
   keep the scenario INCONCLUSIVE rather than accepting whatever happens.
3. Perform the actual human steps. Check intermediate feedback and the final outcome. A
   toast or closed dialog alone does not establish success: reopen/refresh the record or
   inspect the receiving role's UI where persistence or delivery is part of the promise.
   Record consequential action intent/outcome as described in RUN-POLICY.md. If interrupted
   before confirmation, reconcile the same record through UI before considering a retry.
4. Record actual observations, context, timestamp, evidence references, status, and defects
   immediately. Capture evidence around decisive transitions, failures, and ambiguous states;
   do not rely on memory or a single campaign-ending screenshot.
5. Add newly discovered controls/state branches to the inventory and queue. Preserve IDs
   and earlier attempts; a later pass must not erase the original failure.

Each scenario starts NOT_RUN and ends in one of:

| Status | Meaning |
| --- | --- |
| PASS | Expected outcome directly observed; required checks and evidence present. |
| FAIL | Observed behavior contradicts a supported expectation; link a defect. |
| BLOCKED | A prerequisite, authorization, account, environment, or UI tool is missing. |
| INCONCLUSIVE | Attempted but observation or expected behavior cannot settle the result. |
| NOT_APPLICABLE | Demonstrably irrelevant to this role/version/scope; retain the reason and source. |

NOT_RUN remains visible for work not attempted, including a time limit. A failing prerequisite
does not fail all its dependents: mark them BLOCKED and link the prerequisite. Missing access
is never NOT_APPLICABLE. An absent feature promised for this build may be a FAIL; confirm the
promise and visibility conditions first. Each defect needs reproducible UI steps, expected vs
actual, affected role/context, impact, evidence, and affected scenario IDs. Keep speculative
root causes separate from observed behavior.

Avoid repeatedly causing the same harmful effect. Reproduce safely when useful, then continue
independent coverage. Do not fix code or settings under a testing-only request. When the
effective trigger is due and applicable phase authority permits, retest an externally supplied
fix through the same UI scenario and affected adjacent journeys; bind the result to the new
build/context and retain the previous attempt. Otherwise keep the finding unresolved with its
deferred retest prerequisite; receiving a fix does not itself override QA timing.

## 4. Reconcile omissions before finishing

Conduct a fresh UI sweep in a different order from the execution plan: inspect each role's
navigation, secondary menus, settings, detail screens, state-dependent controls, and alternate
entry points. Compare each item to the ledger rather than merely rereading PASS rows.
Revisit the discovery sources and account for items never reached through normal navigation.

Check both directions: every candidate feature maps to scenarios/dispositions, and every
required scenario maps to an observation or explicit gap. Check the coverage families and
handoffs from the references. Record applicability decisions for each family; do not assume
that a catalog item applies to every product or omit it without a decision.

Any newly found item reopens inventory/scenario coverage. Continue discovery, execution, and
reconciliation until a complete reconciliation pass finds no new items or an actual blocker
or user limit stops work. A no-new-item pass is evidence of closure, not proof no feature exists
outside the discoverable scope. Save the last sweep's roles, surfaces, and sources checked.

## 5. Report and preserve continuation

Use [the report template](assets/templates/FINAL-REPORT.md). Separate scope reconciliation,
execution completeness, and observed quality.

For optional artifact-only checking, read [ledger validation](references/LEDGER-VALIDATION.md).
The local checker validates record structure, references, and arithmetic; it never tests the
product or establishes that a screenshot proves its claimed outcome.

Let:

- `T` = all retained scenario rows, including justified NOT_APPLICABLE rows.
- `A` = applicable scenarios = T minus justified NOT_APPLICABLE.
- `E` = exercised to a conclusive outcome = PASS + FAIL.

Report all six status counts and `E/A` execution coverage; `PASS/A` is passing coverage.
When A is zero, report “no applicable scenarios established,” not 100%. Do not report either
as a percentage of all possible product behavior. Summarize by actual role and feature, too:
a global percentage must not hide an untouched staff role. A feature is exercised only when
all its applicable scenarios are PASS or FAIL; it passes only when all are PASS.

“Complete within the inventoried scope” requires a reconciled inventory with no unresolved
candidate dispositions, a no-new-item sweep, and zero NOT_RUN, BLOCKED, or INCONCLUSIVE
applicable scenarios. FAIL can coexist with complete execution, never with “all passed.”
NOT_APPLICABLE exclusions remain visible and cannot shrink scope without justification.

State the completion boundary explicitly: inspected source areas, environment/build, verified
roles/workspaces, configuration/plan conditions, device/input contexts, scenario matrix,
sampling/exclusions, and outstanding discovery gaps. Label output as UI_OBSERVED, SIMULATED,
or PHYSICAL_OBSERVED where relevant. Print preview or simulated delivery cannot pass a required
paper-output or real-delivery scenario. Keep those separate and BLOCKED if observation is
unavailable. Attribute any human-assisted physical observation to its actual observer.

The coordinator must complete the evidence review in ORCHESTRATION.md before adopting a
tester's completion claim. Required evidence missing from a PASS row reopens that scenario;
a tester's final message alone does not establish coverage.

At interruption, save the current ledger, inventory revision, last observed UI state, created
record aliases, cleanup status, open blockers, and the exact next scenario/action. On resume,
reconfirm environment/build, accounts, fixtures, and visible state; retain historical evidence
and reopen affected results if anything changed. For a new target build, checkpoint the old
context and attempts, keep scenario IDs, and reset affected current-index rows to NOT_RUN
(or BLOCKED for a known current prerequisite). When change impact cannot be established,
reopen all in-scope rows. Mark any retained unaffected historical evidence with its original
build and explicit reuse basis; never claim it was rerun on the new target. Required new-build
checks must be rerun. Corrections for inadequate prior evidence remain in that attempt's history
as INCONCLUSIVE; a new unattempted build starts NOT_RUN. Never mark remaining items passed to finish.
Report fixtures cleaned up through UI and any residue requiring action. This skill supplies
acceptance evidence, not a release approval or proof of code/security/accessibility completeness.
