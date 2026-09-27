# Human-journey retest — QA fix batch (PR #19), 2026-09-27

- Run: `otr-qa-2026-09-26` retest round, targeted scope per
  `.claude/PRPs/reports/human-journey-qa-2026-09-26/RETEST-BRIEF.md` (branch `origin/qa-ledger`).
- Method authority: `.agents/skills/human-journey-testing/SKILL.md`
  (human pace, screenshot per decisive transition, no scripts/assertions; SAME_AGENT).
- Target build: `85f053b` (Merge PR #19, fix batch `dea0ecb` + `4b0675c`),
  `./gradlew assembleDebug` BUILD SUCCESSFUL, fresh `adb uninstall` + install.
- Device: `qa-pixel` emulator, 1440x3120, en-US locale (`$` rendering per OBS-04).
- Side effects (§2 of brief) were confirmed in-session before touching the app:
  fresh install, location grant, 1 local test trip, foreground-service run,
  share-sheet cancel-only permitted (not needed), final uninstall wiping residue.

## Result summary

All 11 retest items PASS. Old FAIL/BLOCKED attempt history is retained in
`RUN-LEDGER.md` rows and old evidence IDs are retained in `ledger-index.json`
(appended, never removed). `check_ledger.py` passes with 0 errors.

| Scenario | Old → New | New evidence (ledger `evidence/`) |
|---|---|---|
| S-004 (D-002) | FAIL → **PASS** | E-166, E-168, E-172, E-173, E-189. `OnTheRoad: Active Run` in shade while tracking (dumpsys: FOREGROUND_SERVICE ongoing + tap intent). Residual: body text static, no live km → OBS-09 |
| S-042 (D-002) | BLOCKED → **PASS** | E-168, E-173. Tap returns to Tracker, trip active (03:23 counting) |
| S-013 (D-006) | BLOCKED → **PASS** | E-137, E-139, E-140. NightProfile in Settings appears in booking form immediately |
| S-031 (D-006) | FAIL → **PASS** | E-137, E-139, E-140, E-162. Persists across revisit + cold start |
| S-014 (D-001) | FAIL → **PASS** | E-147, E-150, E-153, E-154, E-156. 6km auto Rp 40,000 → custom 30000 → Rp 30,000 → auto Rp 40,000 restored, follows edits |
| S-036 (D-007) | FAIL → **PASS** | E-141, E-145, E-146. Rate/Km 3500→5000 reaches quote live, no restart |
| S-030 (D-005) | FAIL → **PASS** | E-134, E-135, E-136. Dark / Light / System immediate |
| S-019 | PASS → **PASS** | E-163, E-165, E-166, E-175, E-176, E-177, E-179. Trip (6km, quoted/paid 40000) saved, modal closed, service stopped; prefill + paid-total gating re-confirmed |
| S-022 (D-004) | FAIL → **PASS** | E-131, E-180. Shift `$40000.00` after 1 trip from zero state, no manual action |
| J-01 | FAIL → **PASS** | E-141, E-145, E-146 |
| J-02 | FAIL → **PASS** | E-180, E-181. History Direct `$40000.00` matches Shift |

Counts: NOT_RUN 1 (S-046) / PASS 46 / FAIL 0 / BLOCKED 0 / INCONCLUSIVE 1 (S-043) /
NOT_APPLICABLE 1 (S-045); E/A 46/48, PASS/A 46/48.

## New observations (open)

- OBS-09: tracking notification body is static (`Recording GPS breadcrumbs…`), no live
  distance. Background-awareness + tap-to-return work; distance-at-a-glance missing.
- OBS-10: after typing `30000` in Destination and clearing it, distance showed a stale
  auto-computed `3006.0` (fare Rp 15,039,966); recovered by clearing distance. Transient,
  no data impact.

Still open from before: S-043, S-046, permanent-denial path.

## Cleanup and artifact locations

- Settings restored to originals (System, Default-only, 10000/3500/15000/0.0/1.25);
  app uninstalled (residue wiped). No production, no real sends, no account actions.
- Full ledger: `RUN-LEDGER.md` + `ledger-index.json` + 59 new screenshots (188 total)
  in `.claude/PRPs/reports/human-journey-qa-2026-09-26/` on the transport branch
  `qa-ledger`, local commit `7e4e24d` (objects intact in this repo; working files were
  rebuilt in isolated worktrees so `dev` was never disturbed). **Push to
  `origin/qa-ledger` is still pending — this environment has no GitHub write
  credentials.**
- This file is the durable human-readable record; the ledger branch holds the
  machine-checkable evidence.

No release-readiness is claimed. Acceptance evidence only, emulator (SIMULATED) context.
