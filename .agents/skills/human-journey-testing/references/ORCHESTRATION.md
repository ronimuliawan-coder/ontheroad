# Coordinator and dedicated tester

Read before starting or resuming a campaign. Mode defaults to DEDICATED_QA for both the skill
and workflow. Honor an explicit SAME_AGENT request and applicable harness restrictions.
Resolve RUN-POLICY.md first and dispatch only when its trigger and applicable authority allow
execution. Pass gpt-5.6-luna and xhigh explicitly by default; honor recorded prompt overrides
and disclose unsupported settings. Use a role that does not pin a conflicting model.
Use the harness's in-task subagent mechanism, not a new sidebar task, remote agent service,
or a particular vendor API. This is an instruction contract, not an installed agent daemon.
Standalone execution uses the user's campaign request as its approval basis; engineering
phase gates apply only when integrated with an active engineering engagement.

## Establish ownership

The coordinator resolves the requested scope, existing authorization, output directory,
available tools, and known accounts. It may prepare a preliminary source/feature map while
the tester independently discovers the application. Neither map is an exhaustive answer key.

Before dispatch, establish whether the tester can actually use the required UI tools and
sessions. A fresh agent does not imply a fresh browser, and two tabs do not imply isolated
authentication. If capability is unknown, have the tester confirm it before product actions.
If the tester cannot access the UI but the coordinator can, switch to SAME_AGENT, record the
reason, and continue through the UI. If neither can, preserve discovery and mark execution
BLOCKED. Do not repeatedly spawn replacements or replace UI testing with scripts.

Create one fresh tester with minimal task context where supported. Give raw requirements and
constraints, known risks as hypotheses, and source locations; do not tell it the implementation
is correct or give it a predetermined pass verdict. It must perform its own code/docs and UI
discovery within the selected discovery mode. Preserve applicable instructions and authority.

Use this dispatch contract, filling known fields and retaining unknowns explicitly:

```text
Execution role: TESTER. Use human-journey-testing at [resolved SKILL.md path].
Effective policy: [record path/revision, resolved model/effort, trigger and satisfaction evidence].
Execute the campaign yourself; do not delegate or invoke a coordinator workflow recursively.
Target/environment and observed build: [details or unknown].
Scope and acceptance sources: [roles/features, requirements/docs/source paths].
Discovery mode: [docs/source plus UI | UI-only].
Accounts, workspaces, and browser/session handles: [aliases/handles, never credentials].
UI capability/access: [known capabilities or confirm before testing].
Approved operations and limits: [test data, external effects, current phase if integrated].
Artifact ownership: [run directory, ledger/report paths]; preserve prior attempt history.
Browser ownership: [exclusive session(s), handoff protocol if shared].
You are not alone in the workspace. Do not revert others' edits. Do not edit application code,
deploy, change account permissions, or write outside your assigned artifacts.
Independently discover features, execute UI journeys, persist observations, and perform the
final omission sweep. Surface authentication/tool blockers promptly and continue safe work.
Return the ledger/report paths, coverage counts and gaps, discovery limits, defects,
cleanup/resume state, and evidence needed for coordinator review.
```

The coordinator owns the overall task and its acceptance/evidence links. The tester owns the
campaign's ledger, scenario attempts, and draft report. Do not edit the same artifacts
concurrently. On handoff, checkpoint and explicitly transfer ownership before another writer
updates them. The tester has no authority to fix the product or advance an engineering phase.

## Browser and data coordination

Give the tester exclusive control of its assigned sessions during execution. The coordinator
can inspect docs, map requirements, or review completed artifact snapshots in parallel, but
must not navigate those sessions, switch their accounts, or mutate their fixtures. UI review
by the coordinator occurs after an explicit browser handoff. If only a shared session exists,
serialize its use; do not discard working logins just to manufacture a separate browser.

Do not change the tested deployment/build underneath an active campaign. If it changes,
checkpoint the old evidence, establish the new context, and reopen affected scenarios. Other
agents may prepare code independently when it does not change the running target or fixtures.

Additional testers are not the default. Use them only within the user's/harness's delegation
authority, with explicit scenario/artifact ownership, isolated sessions, and disjoint mutable
fixtures. Name one owner for each cross-role handoff and merge results without double-counting.

## Monitor and resume

Wait for the assigned tester's progress using the harness's supported coordination tools.
Do not call dispatch itself completion. Forward only concrete missing user input; do not ask
again for known account mappings or approved test operations. A login blocker in one role
does not stop independent tests in another.

If interrupted, save the tester identity, artifact/browser ownership, last checkpoint, open
authentication requests, and queue. Resume the same tester if available and context remains
usable; otherwise transfer the persisted run to one replacement tester. An assigned TESTER
never spawns another TESTER. SAME_AGENT execution follows the same coverage/evidence rules.
Save action intents and OUTCOME_UNKNOWN outcomes; the replacement must reconcile those through
UI before retrying. Restore the policy, not just the scenario queue. Do not relaunch QA during
resume if its configured trigger is still pending.

## Coordinator acceptance of the report

After the tester checkpoints and hands off its artifacts, the coordinator must:

1. Reconcile the tester's feature/source inventory with the requested scope and independently
   available requirements. Check every candidate disposition and every role, not just totals.
2. Inspect scenario definitions, current statuses, and evidence references for all required
   rows. Check that references exist, support the claimed observation/context, and include
   persistence/recipient checks where required. Directly inspect evidence for failures,
   handoffs, material success claims, and any doubtful row; do not blindly trust PASS labels.
3. Verify the final UI omission sweep is documented. A coordinator reading a report is not
   a substitute for the tester revisiting the interface. Return newly found candidates to
   discovery/testing; preserve a queue and clear ownership for follow-up.
4. Reconcile all status counts and applicable denominators, role/session verification,
   build identity, configuration limits, sampling, and physical/simulated evidence boundaries.
   Missing required evidence changes PASS to INCONCLUSIVE pending follow-up; unattempted
   scenarios remain NOT_RUN. Do not infer observations from code or CI.
5. Record reviewed artifacts/context, gaps found, follow-up outcomes, and verdict as VERIFIED
   or FOLLOW_UP_REQUIRED. VERIFIED means the report accurately represents evidence and gaps;
   it can accompany incomplete execution or observed failures. It is not release approval.

Use a bounded UI spot-check when evidence is ambiguous and access is available; otherwise
record the gap. Keep review method separate from outcome: COORDINATOR_REVIEW for a coordinator
reviewing the tester, SELF_REVIEW for SAME_AGENT. Either method returns VERIFIED or
FOLLOW_UP_REQUIRED; self-review does not imply that gaps are settled or independence exists.
Present the final report with execution mode, review method/outcome, reviewer alias, and limits.
