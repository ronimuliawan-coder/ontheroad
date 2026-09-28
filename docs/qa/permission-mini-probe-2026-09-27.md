# Permission mini-probe — S-040/S-041, 2026-09-27

- Run: `otr-qa-2026-09-26` probe round, scope S-041 (deny) + S-040 (allow) per task brief
  (ledger + prior evidence on `origin/qa-ledger`; attempts appended, never rewritten).
- Method authority: `.agents/skills/human-journey-testing/SKILL.md`
  (human pace, screenshot per decisive action, no scripts/assertions; SAME_AGENT).
- Target build: `origin/dev` @ `a61e8704d94ff0d53b7fa6646e987789506b78ca`
  (same family as `85f053b` PR #19; `git diff --stat 85f053b..origin/dev` is docs-only,
  app code identical). APK rebuilt via `./gradlew assembleDebug` BUILD SUCCESSFUL in
  isolated worktree `/tmp/opencode/build-dev` (main `qa-ledger` worktree never disturbed),
  fresh `adb uninstall` (`DELETE_FAILED_INTERNAL_ERROR` = no prior install) + install
  Success (`com.ontheroad.debug` v0.1.0 (1)).
- Device: `qa-pixel` emulator, 1440x3120, en-US locale, API 35.
- Side effects were confirmed in-session before touching the app: fresh install
  (wipes trips), location + notification system dialogs, 1 local platform test trip
  with foreground-service run on the TEST emulator, final uninstall wiping residue.
  No production, no sends, no accounts.

## Per-scenario outcome

| Scenario | Outcome | Evidence (ledger `evidence/`) |
|---|---|---|
| S-041 (deny location → red inline message, no trip, manual quoting usable) | **PASS** (new probe attempt appended) | E-202 location `Don't allow`, E-203 notification `Don't allow`, E-204 red `Location permission is required for GPS tracking. You can still enter the quote manually.` + idle `Start Trip`, E-205 History `No trips logged yet.`, E-206 Direct manual fallback Rp 15,000 |
| S-040 (allow location + notification prompt → Start Trip proceeds, tracking + notification visible) | **PASS** (new probe attempt appended) | E-208 2nd location ask `While using the app`, E-209 notification `Allow`, E-210 tracking cockpit (`Complete Trip`, 00:02, GPS active), E-211 shade `OnTheRoad: Active Run · now / Recording GPS breadcrumbs...`, E-212 sustained tracking (00:37) |

Notification-prompt record: S-041 denied it (`Don't allow`); S-040 granted it (`Allow`).
The prompt re-appeared on the second `Start Trip` even after the S-041 denial.

Counts unchanged: NOT_RUN 1 (S-046) / PASS 46 / FAIL 0 / BLOCKED 0 /
INCONCLUSIVE 1 (S-043) / NOT_APPLICABLE 1 (S-045); E/A 46/48, PASS/A 46/48.
`check_ledger.py` passes with 0 errors. Still open: OBS-10, S-043, S-046,
permanent-denial path. No release-readiness claimed.

## Cleanup and transport status

- Settings verified at defaults at probe end (System theme, Default-only profile,
  rates 10000/3500/15000/0.0 — E-213); nothing was changed, nothing to restore.
- `adb uninstall com.ontheroad.debug` → Success; `pm list packages` confirms wiped
  (active Grab trip + foreground service removed with uninstall).
- Ledger commits on local `qa-ledger` (ahead of `origin/qa-ledger`):
  ledger rows + `ledger-index.json`, evidence E-201…E-213, and this report.
- **Push blocked**: no GitHub credentials in this environment (`gh` not logged in,
  no helper/SSH; askpass via stale/new VSCode IPC socket fails or hangs).
  To publish where auth exists: `git push origin qa-ledger`.
  Transport branch must never be merged (per brief).
