# Effective run policy and promotion evidence

Read before dispatch and on resume. Persist this policy in the existing run ledger or linked
engineering checkpoint. There is one effective policy record; other artifacts link it rather
than maintaining divergent copies. Use the run-ledger policy table as the portable template.

## Resolve settings

Apply active system/harness constraints, then explicit current user instructions, then the
saved engagement policy, then package defaults for still-unset fields. A new prompt changes
only fields it addresses. Record each effective value, its source, and the reason for a
change; do not reset other settings on resume or provider/model changes. Report contradictions
or unavailable capabilities, without silently widening scope or weakening requirements.

Keep the engagement baseline distinct from temporary overrides. Record each override's fields,
values, source, campaign/scope, expiry event, and value to restore. An instruction such as
“for this run” expires when that campaign ends; HANDOFF does not end it. On RESUME restore
the still-active override and its expiry, then revert to baseline when its condition ends.
An unqualified preference change persists for the engagement. Do not persist a one-off
“run QA now” as a permanent change to the baseline trigger or tester model.

| Setting | Default and meaning |
| --- | --- |
| tester_mode | DEDICATED_QA; SAME_AGENT only by request or disclosed capability fallback. |
| tester_model | gpt-5.6-luna (GPT-5.6 Luna). |
| tester_reasoning | xhigh. |
| qa_trigger | AUTO: scoped UI campaigns when relevant approved work is testable. |
| discovery | Read-only code/docs plus UI; honor explicit UI-only discovery. |
| scope / roles / target / devices | Discover from the request; never invent unavailable roles or a deployment identity. |
| side_effects | Existing authorized scope; record specific boundaries and missing authority. |
| promotion_rule | Required for PROMOTION_ONLY; authoritative PR repository/base/head relationship and timing, not a guessed branch name. |
| evidence_reuse | Preserve context and require new evidence for changed/unknown-impact behavior and required new-build checks. |

Pass the resolved model and reasoning as explicit spawn parameters, using the harness's
supported identifier for that model. Do not choose a named agent role that pins a different
model. Some harnesses require minimal/no history for model overrides; pass the task contract
and necessary instructions instead of discarding the requested model to inherit full history.
This default applies to the QA tester, not the coordinator or unrelated engineering agents.

Record requested and actual model/effort when the harness exposes them; otherwise record
actual as unverified and preserve the dispatch evidence. If the model/effort is unsupported,
ask the owner for an alternative while continuing independent preparation. Never silently
substitute. A SAME_AGENT fallback for unavailable delegation/UI handoff remains allowed by the
execution contract, but explicitly disclose the coordinator's actual model if known and that
the requested tester model could not be used. Do not claim a model switch happened inside the
current session. No tool availability claim may be inferred from the model name.

## QA trigger decision

| Trigger | Execution condition | Before that condition |
| --- | --- | --- |
| AUTO | Relevant approved human-facing unit is testable; broader agreed release scope at acceptance. | Prepare inventory/scenarios and prerequisites. |
| PROMOTION_ONLY | A verified promotion candidate and its matching test target are ready, at the configured pre-merge point. | Plan/discover only; do not launch a UI campaign on ordinary feature PRs. |
| MANUAL | User explicitly requests this campaign for this scope/context. | Prepare only; a generic resume/continue is not a request to run QA. |

An explicit standalone request to execute human-journey testing satisfies MANUAL for that
campaign. If a saved PROMOTION_ONLY policy conflicts with a new request clearly asking to run
now, record the scoped override; if ambiguous, clarify instead of changing the trigger silently.
Trigger satisfaction never grants deployment, merge, payment, or later-phase authority.
Deferred future scenarios remain NOT_RUN with the trigger/phase prerequisite. When execution
is due but a concrete prerequisite is missing, mark affected work BLOCKED. Do not describe a
not-yet-due campaign as failed or complete. Carry trigger decisions into handoff and final reports.

For standalone AUTO, the explicit request to test supplies the campaign's approval basis;
there is no engineering unit or phase gate to invent. Integrated AUTO uses the existing
engineering phase/unit authority. Both still require the target and authorized operations.

Bind trigger satisfaction to a campaign ID, scope and target context, including its authorizing
request/event and whether execution is pending, in progress or finished. RESUME can continue
an already-requested MANUAL campaign under that saved authority; it does not request a new one.
An unchanged, finished campaign with no reopened scenarios must not be relaunched just because
the trigger still matches. Reruns need changed evidence context, remaining work, or an explicit
rerun request. A one-campaign override leaves the long-term trigger unchanged.

Discovery before the trigger can inspect docs/source and permitted UI surfaces for inventory;
it must not execute test scenarios, create product fixtures, or spawn a UI tester under the
pretext of preparation. Explicit restrictions on even UI discovery take precedence.

## Promotion candidate identity

Read authoritative PR metadata through permitted read-only tooling; this is control-plane
verification, not an API product test. Record PR URL/ID, repository, source and target refs,
head SHA, base SHA, and any preview/merge-candidate artifact identifier. Establish the project's
promotion rule from its policy or the owner. Never infer promotion solely from title/label or
hardcode dev/master. A label can be supporting evidence, not sole identity.

Bind UI evidence to the running target's observable build/deployment identity and the intended
candidate. Deployment metadata can establish identity; only UI observations establish scenario
results. A local checkout SHA or green CI is not proof of the preview's contents. If the preview
tests a merge result, record its identity and relationship to both head and base. If the merge
method would produce different contents, the existing result is not final acceptance evidence.

Recheck head/base and deployed artifact/configuration context before the first scenario and
before final coordinator acceptance. Record non-secret configuration/plan/flag conditions and
known dependency/integration versions where material. A rebuilt mutable URL is not a stable
identity. When head/base, build, or material configuration changes, checkpoint the old result,
refresh the candidate mapping, and reopen affected scenarios. Unknown impact reopens all
in-scope rows. Preserve previous evidence with its original identity; do not reset denominators
to hide stale work. If identity cannot be established, retain useful observations but block
promotion acceptance. Do not merge as part of this testing contract.

## Uncertain actions after interruption

Before a consequential UI submission, record the intent, actor/session, fixture alias, and
expected visible result. After the action, record CONFIRMED_SUCCEEDED, CONFIRMED_FAILED, or
OUTCOME_UNKNOWN. These are action states, separate from scenario statuses. An interrupted
click/timeout does not prove success or failure and must not be retried blindly.

For OUTCOME_UNKNOWN, inspect the relevant list/detail/history and receiving-role UI for the
same record and intent before retrying. If there is affirmative evidence of failure and retry
is safe/authorized, record that evidence and retry once, then reassess. If still uncertain,
keep the scenario INCONCLUSIVE or BLOCKED by missing access and request the needed observation.
Do not use APIs, storage queries, or repeated submissions to discover whether the action worked.
No automatic retries for an action that may duplicate money movement, communication, or real
fulfillment. Apply the same reconciliation to engineering actions during HANDOFF/RESUME using
their allowed authoritative read-only sources, without repeating external writes.
