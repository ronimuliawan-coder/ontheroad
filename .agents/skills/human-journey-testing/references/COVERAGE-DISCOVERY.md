# Coverage discovery and omission audit

Use this during initial inventory and final reconciliation. The unit of coverage is a human
capability with a role and meaningful state, not a route, button count, or internal function.
One page may contain many capabilities; one capability may span several pages and roles.

## Independent discovery passes

**Surface-led pass:** Start from every known app/portal entry point. As each actual role,
inspect global navigation, footer, account menu, tabs, overflow/context menus, toolbars,
drawers, dialogs, row actions, detail pages, empty states, help, and responsive navigation.
Scroll enough to inspect the complete screen. Open nested menus and alternate views. Record
controls only shown after selecting a row, changing state, obtaining permission, or entering
data. Record disabled actions and investigate their visible prerequisites.

**Intent-led pass:** Independently ask what each role comes to accomplish, how they recover
from mistakes, what happens before/after another role intervenes, and what they revisit later.
Walk those tasks from entry through final receipt/history/status. This catches tasks omitted
from navigation, including invitation links, onboarding, restore, and follow-up notifications.

**Candidate-source pass (default discovery mode):** Compare available requirements, product
help, role/permission matrices, release notes, and focused read-only source navigation/action
definitions with the UI inventory. No specific language, directory layout, CLI, code graph,
tracker, or framework is required. Reuse available project navigation guidance. Avoid reading
secrets. Code indicates a candidate, not that a feature exists in this deployment or works.
Do not run source, call endpoints, or query storage. If sources are missing, record this and
continue the two UI passes with a narrower discovery-confidence statement.

Create a product-area map from the available project structure/docs, then inspect relevant
entry points, action/permission definitions, and configuration/plan conditions across every
in-scope area. Record specific inspected references and areas still uninspected or inaccessible.
Reconcile runtime contexts such as tenant, role, plan, flags, integrations, or locale using
available non-secret documentation and visible UI evidence; do not expose credentials or
toggle hidden configuration to manufacture coverage. Reading only the first journey's files
does not establish whole-project discovery. Remaining inspection work prevents closure.

In strict UI-only mode, skip source/docs inspection and disclose that features unreachable
from the available UI/accounts may remain undiscovered. User-supplied scope and expectations
still apply. A documented-only or flag-dependent candidate needs one of: a current scenario,
an evidenced future/retired/out-of-scope disposition, or an unresolved discovery gap. Never
quietly drop it because its entry point is hard to find.

## Coverage families

Make one applicability decision for each family below, with linked features/scenarios or a
reason it does not apply. These are discovery prompts, not a universal feature checklist.
For each applicable feature, consider success, invalid/empty/boundary input, cancellation,
persistence, permission, and recovery; record relevant variants or an explicit rationale for
omitting a dimension. Distinguish “not relevant” from “could not test.”

| Family | Often missed UI behavior |
| --- | --- |
| Entry and navigation | Guest vs signed-in entry, onboarding, deep link through address bar, back/forward, reload, breadcrumbs, footer/help links, unknown/not-found page. |
| Identity and access | Sign-in/out, invited/new account, reset/recovery, session expiry, workspace switch, distinct staff permissions, permitted and denied access using a known UI URL. |
| Discovery and collections | Search/no results/clear, combined filters/reset, sorting, pagination/load more, alternate views, selected rows and bulk actions. |
| Record lifecycle | Create/read/edit, draft/save/submit, duplicate, archive/delete, restore/undo; detail vs list consistency, status history, returning after refresh. |
| Inputs and validation | Required/optional, empty/whitespace, invalid formats, visible length/value boundaries, date/time/locale, selection controls, validation timing, preserving input after errors. |
| Workflow states | Empty/loading/populated, pending/approved/rejected, unavailable/completed/cancelled, state-dependent actions and blocked transitions; product-specific states discovered in UI. |
| Cross-role handoffs | Initiator submits → recipient sees correct record → recipient acts → initiator sees updated outcome; all involved roles and permission boundaries. |
| Money and entitlements | Totals/discounts/tax/currency, free vs paid capabilities, confirmation, decline/cancel/refund where supported and authorized; receipt and history. |
| Files and output | Upload/remove/replace, format/size rejection, previews, download/export content opened in a viewer, print preview; physical output needs actual evidence or a gap. |
| Communication | In-app notifications/read state/preferences, links, recipient delivery in an authorized mailbox UI, feedback/contact/help, recovery from undelivered or delayed results. |
| Settings and administration | Profile, preferences, workspace settings, membership/roles, configuration changes visible to affected roles; restore original settings afterward when appropriate. |
| Interaction and presentation | Keyboard/focus, dialogs Escape/cancel, browser zoom, overflow, touch/drag if supported, readable errors, responsive menus, real-device-only behavior. |
| Recovery and continuity | Back/reload/reopen, unsaved changes, supported undo, repeated submit, two tabs, stale record, interrupted connection through available user-facing controls. |

Exercise each distinct allowed staff role rather than assuming the administrator represents
all of them. Where a feature has role restrictions, verify the permitted path and relevant
denied path through UI using safe accounts; do not perform security exploitation.

## Bounded combinations

List meaningful values for roles, record states, input classes, permissions, devices, locales,
and plan/flag contexts actually in scope. Give distinct business rules and high-impact
combinations their own scenario. Use justified representative combinations for remaining
layout/data repetition and disclose what was sampled. One desktop success cannot stand in for
a mobile-only menu, different permission rule, or a different workflow transition.

Do not promise a full Cartesian product or use “pairwise” as a claim unless a concrete matrix
supports it. If the user requests all combinations, enumerate the finite requested matrix and
show outstanding rows; if infeasible, report the limit rather than silently sampling.

Use current UI data or UI-created fixtures to reach states. Missing declined-payment fixtures,
expired invitations, alternate plans, physical devices, or offline controls create specific
BLOCKED scenarios. No request stubs, clock manipulation, storage edits, or synthetic API setup.

For effects beyond the interface, split UI submission/preview from physical completion when
both matter. Record UI_OBSERVED, SIMULATED, or PHYSICAL_OBSERVED evidence and its observer.
A print-to-PDF or printer simulation can exercise its own scenario; it does not establish
paper quality, printer connectivity, or actual fulfillment. Keep requested physical checks
blocked without appropriate hardware/observation; do not exclude them merely because the
agent lacks access. Owner-supplied observations must be attributed rather than represented
as the tester's firsthand evidence.

## Closure questions

- Does each surface have an inspected role/context and a list of its discovered controls?
- Does every action, documented candidate, and meaningful state map to scenarios or a reasoned
  disposition? Are all applicability decisions recorded?
- Have hidden menus, item detail screens, responsive entry points, and “after completion”
  screens been inspected? Did changes reveal actions absent from the initial inventory?
- Did a natural task and a navigation sweep both reach each major capability?
- Are initiator and recipient observations linked to the same UI-visible record?
- Is any role, prerequisite, combination, or scenario represented only by an assumption?
- Did the final sweep actually revisit the interface, and did new discoveries reenter testing?

Maintain unknowns as first-class gaps. Stable IDs and persisted observations make omissions
detectable across sessions; the size of a checklist by itself does not establish coverage.
