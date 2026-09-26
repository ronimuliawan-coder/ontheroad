---
description: "Inventory and test human-facing features through UI role journeys, then reconcile every feature and scenario with evidence or an explicit gap"
argument-hint: "[target, scope, roles, environment, accounts, allowed side effects, and output directory]"
---

# Human Journey Testing Workflow

Use the installed `human-journey-testing` skill for `$ARGUMENTS`. Read its `SKILL.md` fully
and follow its reference routing. Resolve it through the harness's skill catalog, the local
`.agents/skills/human-journey-testing/` installation, or the explicitly provided toolkit path.
Do not assume a particular working directory or install other packages to run this workflow.

Default to DEDICATED_QA for this workflow and direct skill invocation: the primary agent
coordinates one fresh tester under the skill's orchestration contract. Use model `gpt-5.6-luna`, and reasoning `xhigh` by default; honor explicit prompt overrides
and saved policy. Resolve RUN-POLICY.md and the execution trigger before dispatch. Pass execution role
TESTER so it does not delegate recursively. Disclose SAME_AGENT fallback when requested or
when delegation/UI handoff is unavailable. The primary agent reviews the resulting coverage
and evidence before adopting the report. Do not create a separate user-owned task.

Copy-paste entry shape; fill known values and leave others `DISCOVER`:

```text
Run human-journey-testing
— target: [URL or native app]
— project context: [optional repository/docs path]
— tester mode: DEDICATED_QA
— tester model: gpt-5.6-luna
— tester reasoning: xhigh
— QA trigger: AUTO
— integration: [standalone | active high-assurance-engineering phase/plan reference]
— scope: [whole human-facing project | all features for specified role | named area]
— roles: DISCOVER actual customer/end-user and staff/operator roles; apply QA perspective
— environment/build: DISCOVER
— accounts/fixtures: [safe account references and fixture instructions; no secrets]
— sessions: [existing profile/session handles by role, or DISCOVER; preserve signed-in accounts]
— UI tools: DISCOVER
— discovery: docs/source read-only plus UI
— side effects: [existing authorization and any specific limits]
— devices/input: [supported target contexts or DISCOVER]
— output: [artifact directory]
```

Standalone testing proceeds without a separate phase-approval ceremony. When integrated with
an active engineering engagement, preserve its current approved scope/phase and authority;
this workflow does not authorize later phases, fixes, or deployment.
Only ask for missing information that affects execution, and isolate blocked actions while
continuing independent work. A request to test is not permission for unrelated real payments,
messages, production destruction, or changes to source/configuration.

Maintain an inventory and scenario ledger, then perform UI journeys and an independent final
omission sweep. Report every scenario as NOT_RUN, PASS, FAIL, BLOCKED, INCONCLUSIVE, or justified
NOT_APPLICABLE. Source inspection discovers candidates; all product test evidence comes from
visible UI interaction. Do not substitute scripts, APIs, CI, or injected browser code.

For resumption, add `— resume: [existing run ledger]`. Reconfirm the environment, build,
accounts, fixtures, and current UI state, then continue the persisted queue. Keep past evidence
and reopen affected scenarios when context changed. Do not start a new checklist and lose gaps.
Use saved settings unless the new prompt explicitly overrides them; omit the entry shape's
default fields when merely resuming. PROMOTION_ONLY waits for a verified matching promotion
candidate; MANUAL waits for an explicit campaign request. Deferred scenarios stay NOT_RUN.
Reconcile interrupted consequential actions through UI evidence before retrying. For an
engineering session handoff, use its HANDOFF/RESUME lifecycle and link the existing QA ledger.
