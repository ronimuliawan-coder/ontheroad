# Retest brief — QA fix batch (devbox session)

Targeted retest of PR #19 fixes. Same method contract as DEVBOX-HANDOFF.md
(human pace, screenshot evidence per decisive transition, no scripts/assertions).
Transport branch `qa-ledger` must never be merged.

## 0. Fresh build (do not reuse the old APK)

```bash
git fetch origin
git checkout 85f053b -- . 2>/dev/null || git checkout -f 85f053b
```
If that checkout disturbs the worktree, stop and ask the owner — otherwise confirm
`git rev-parse HEAD` starts with `85f053b`, then:

```bash
./gradlew assembleDebug
adb uninstall com.ontheroad.debug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Fresh install matters: D-004 must be verified from zero state (no legacy trips),
and the old campaign's test trips must not pollute Shift aggregates.
Record the build SHA in the ledger before executing.

## 1. Scope (nothing else)

Re-run exactly: S-004, S-013, S-014, S-019, S-022, S-030, S-031, S-036, S-042,
J-01, J-02. For each, keep the original scenario ID, append a new attempt, retain
the old FAIL attempt history. All other rows keep their prior status.

Pass criteria (expected outcomes):
- S-004/S-042 (D-002): tracking shows a live-distance notification in the shade;
  tapping it returns to the app.
- S-013/S-031 (D-006): profile selected in Settings appears selected in the
  booking form immediately, no restart.
- S-014 (D-001): toggle Custom Price on, enter override, toggle back to auto →
  quote returns to the computed auto fare and follows later distance edits.
- S-019 (D-004 setup): complete a direct trip; S-022: Shift aggregates reflect it
  without any manual shift action. J-02: trip visible in History with matching figures.
- S-030 (D-005): each theme applies immediately, no force-stop.
- S-036/J-01 (D-007): saved rates change the live quote immediately.
- S-019 also re-confirms the modal prefill and paid-total validation still behave
  as originally passed.

## 2. Side effects (owner confirms in-session)

Fresh install (wipes old test trips), location grant, 1–2 local test trips,
foreground service, share-sheet cancel-only. Uninstall wipes residue at the end.

## 3. Report back

Per-scenario new statuses with evidence refs (screenshots around each previously
failing transition), updated RUN-LEDGER.md + ledger-index.json (checker must pass
with zero errors), settings restored to originals. Push ledger files + new evidence
to `origin/qa-ledger`. Do not claim release-readiness.
