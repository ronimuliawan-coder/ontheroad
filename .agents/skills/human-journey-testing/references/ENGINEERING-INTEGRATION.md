# Integration with high-assurance-engineering

Use only when that engineering engagement is already active or explicitly requested. Resolve
its installed skill/master prompt through the harness catalog or supplied toolkit location;
do not assume the companion is installed at a fixed relative path. If unavailable, report the
missing companion for combined work; standalone human-journey testing still works.

The engineering coordinator owns scope, implementation, checks, tracking, approvals, and
release. The human-journey tester owns UI discovery/execution and its evidence. Its normal
mode is one dedicated subagent under ORCHESTRATION.md; absence of delegation does not exempt
required UI acceptance. Source/code inspection for discovery remains permitted; all product
test results in this campaign come from actual UI observations.

## Connect the evidence to the existing plan

Resolve RUN-POLICY.md before scheduling execution. Persist one effective policy in the owning
engagement record: QA trigger AUTO / PROMOTION_ONLY / MANUAL, scope and side effects, model,
and evidence context. The QA subagent defaults to gpt-5.6-luna with xhigh reasoning, passed
explicitly to the harness; prompt overrides win and survive RESUME/model changes. Record
requested versus actual settings and any capability fallback. The coordinator is not pinned.

The implementation/release execution guidance below applies only when that saved trigger is
due. PROMOTION_ONLY permits discovery/planning before the verified promotion candidate, but
no ordinary-feature-PR UI campaign or test-fixture mutations. MANUAL requires an explicit
campaign request; a generic continue or RESUME does not trigger it. Scheduled work stays
NOT_RUN; a missing prerequisite when execution is due is BLOCKED. Other required engineering
checks retain their gates. A trigger override changes QA timing, not release authority.

For promotion acceptance, bind PR repository/source/target refs, head/base SHAs, merge/preview
artifact, and actual deployed build/configuration. Recheck before first execution and final
acceptance. Changed identity reopens affected scenarios (all if impact is unknown); unknown
identity blocks promotion acceptance. Read-only PR/deployment metadata verifies identity,
never product behavior. Follow the full RUN-POLICY.md identity contract.

- During approved discovery/design/planning, map user acceptance criteria to features,
  role-specific scenarios, account/session needs, environment/build, and evidence. Identify
  UI-tool and physical-output limitations early. Planning does not authorize later mutations.
- During approved implementation validation, run the affected feature, relevant permissions,
  alternate/recovery paths, and connected handoffs when the target is testable. Do not turn
  each small code change into an unsolicited whole-project campaign.
- For release acceptance, use the broader agreed release scope, including applicable shared
  journeys. Record exactly which UI checks run before deployment on a preview and which run
  afterward on the approved target. Preview evidence does not prove production behavior.
- Attach the ledger/report and coordinator review to the canonical engineering evidence log
  or tracker, using existing write authorization. Link acceptance criterion → scenario → UI
  evidence → defect → fix/retest evidence. Avoid duplicating the full ledger in the tracker.

The companion master prompt owns the exact phase gates. This skill's standalone permission
to proceed does not bypass an active engineering approval boundary or authorize a deployment.
Use existing approved environments; missing deployment authority blocks only dependent work.

During planning, future-phase scenarios stay NOT_RUN with their owning phase/approval recorded
as prerequisites. Do not treat a scheduled future phase as an active execution failure. Once
execution is requested/due, missing authorization or another concrete prerequisite makes the
affected scenario BLOCKED. Neither state supplies execution evidence or permits crossing a gate.

## Remediation and acceptance

HAE START/RESUME/HANDOFF are session actions, separate from work types. HANDOFF checkpoints
the effective policy, ledger/evidence, active tester/browser ownership, pending effects and
exact next action. RESUME reconciles authoritative state and reacquires exclusive resources
before continuing the approved unit. It preserves old results and reopens stale evidence;
it neither restarts Phase 0 nor automatically dispatches a new tester. Reconcile uncertain
submissions through the same-record UI before retrying. Use the owning engineering skill's
SESSION-CHECKPOINT template; a pointer here does not create a second approval source.

The tester reports reproducible observations and gaps; the coordinator triages them through
the engineering process. In-scope authorized fixes belong to the implementation owner.
Material new scope returns to the owning approval gate. After a fix is available on the test
target and the effective trigger/phase permits execution, re-exercise the failed UI scenario
and affected adjacent journeys on the new build,
preserving previous attempts. Do not mark a UI failure resolved solely from a code change,
unit test, or green CI run. Do not deploy over an actively tested build without checkpointing.
If the trigger is still deferred, keep the unresolved finding and retest prerequisite visible.

Engineering's automated checks/CI remain required where applicable. Human-journey evidence
adds UI acceptance; its ban on programmatic product tests applies to this campaign, not to
the separate engineering checks. Neither evidence stream substitutes for the other.

An unknown deployed build can support observations about that session, but cannot satisfy
engineering's exact-revision acceptance requirement until its identity is established. Keep
that as an acceptance evidence gap; do not fabricate a commit from the local checkout.

Required blocked/unattempted/inconclusive scenarios and unresolved discovery gaps prevent a
complete-execution claim. Required FAIL results prevent all-passing acceptance. The engineering
owner can accept a documented exception under its existing gates, but the ledger retains the
actual FAIL/BLOCKED/etc. status and the report must not become “100% passed.” Coordinator
verification of an accurate report and human release approval are separate decisions.
