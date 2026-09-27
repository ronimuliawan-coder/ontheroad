# OBS-09 movement probe — live notification km, 2026-09-27

- Run: `otr-qa-2026-09-26` probe round, scope S-004/S-042 per task brief
  (ledger + prior evidence on `origin/qa-ledger`; attempts appended, never rewritten).
- Method authority: `.agents/skills/human-journey-testing/SKILL.md`
  (human pace, screenshot per decisive transition, no scripts/assertions; SAME_AGENT)
  plus `RETEST-BRIEF.md` §§2–3 (side-effect confirmation, reporting rules).
- Target build: `85f053b` (full SHA `85f053b114eaf5e53cc714b9335da0e21b83046b`,
  Merge PR #19), APK rebuilt via `./gradlew assembleDebug` BUILD SUCCESSFUL in
  isolated worktree `/tmp/opencode/build-85f053b` (main worktree never disturbed),
  fresh-installed (`versionName 0.1.0`). A same-build APK was assumed present but the
  emulator had been clean-booted with no app installed, so a verified rebuild was required.
- Device: `qa-pixel` emulator, 1440x3120, en-US locale.
- Side effects were confirmed in-session before touching the app: fresh install,
  location + notification grants (via system dialogs), 1 local direct test trip with
  foreground-service run, final uninstall wiping residue.

## Setup (fixture) and observation

- Fresh zero state confirmed on launch (Tracker idle, Grab, 0.00 km — E-190); no
  pre-wipe needed since no prior test trips existed.
- Fresh direct trip through UI: Direct chip → manual distance 2 km → fare Rp 17,000
  (= 10000 + 2×3500, E-191) → Start Direct Run → granted location, then notifications
  (E-199, E-200 intermediates) → tracking with duration counting (E-192).
- Walk fixture: 41 `adb emu geo fix` positions, lon 106.8271→106.8285, lat −6.1754,
  ~3 s apart over ~2 min. Observation after the fixture was human-paced.
- Mechanism (source, read-only): `LocationTrackingService` posts
  `Tracking: X.XX km` once `actualDistanceKm > 0`, else `Recording GPS breadcrumbs…`.

## Per-scenario outcome

| Scenario | Outcome | Evidence (ledger `evidence/`) |
|---|---|---|
| S-004 (F-019, live-distance notification) | **PASS** (new probe attempt appended) | E-190, E-192, E-193, E-194, E-196. Tracker ACTUAL DISTANCE **0.16 km**; shade body **`Tracking: 0.16 km`** |
| S-042 (F-041, tap-to-return) | **PASS** (prior 85f053b PASS stands, probe note appended) | E-194. Notification present with live km; tap not re-exercised |
| OBS-09 | **RESOLVED** | Static text is only the zero-distance state; live km confirmed working. No code change needed |

Counts unchanged: NOT_RUN 1 (S-046) / PASS 46 / FAIL 0 / BLOCKED 0 /
INCONCLUSIVE 1 (S-043) / NOT_APPLICABLE 1 (S-045); E/A 46/48, PASS/A 46/48.
`check_ledger.py` passes with 0 errors. Still open: OBS-10, S-043, S-046,
permanent-denial path. No release-readiness claimed.

## Cleanup and transport status

- Settings verified at defaults at probe end (System theme, Default-only profile,
  rates 10000/3500/15000/0.0 — E-195); nothing was changed, nothing to restore.
- `adb uninstall com.ontheroad.debug` → Success; residue wiped.
- Ledger commits on local `qa-ledger` (ahead of `origin/qa-ledger`):
  ledger rows + `ledger-index.json`, evidence E-190…E-200, and this report.
- **Push blocked**: no GitHub credentials in this environment (`gh` not logged in,
  no helper/SSH; headless askpass hangs; fail-fast error `could not read Username`,
  exit 128). To publish where auth exists: `git push origin/qa-ledger`.
  Transport branch must never be merged (per brief).
