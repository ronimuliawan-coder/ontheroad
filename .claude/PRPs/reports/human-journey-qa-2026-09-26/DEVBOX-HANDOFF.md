# Handoff brief — OnTheRoad human-journey QA execution (devbox session)

Paste this file's contents (or point the devbox session at this file) to start execution.
Prep campaign: `otr-qa-2026-09-26`. Ledger: `RUN-LEDGER.md` + `ledger-index.json` in this
directory. Transport branch `qa-ledger` must never be merged.

You are the DEDICATED_QA tester. Prep is done; you execute. Read this whole brief first,
then work top to bottom.

## 0. Get the ledger (do not switch branches)

```bash
git fetch origin
git checkout origin/qa-ledger -- .claude/PRPs/reports/human-journey-qa-2026-09-26/
```

All 49 scenarios are `NOT_RUN`. Never merge `qa-ledger`.

Also read `.agents/skills/human-journey-testing/SKILL.md` and its `references/` files
for the full method (persona lenses, run policy, orchestration, coverage discovery).
This brief is the execution order; the skill is the authority on disputed points.

## 1. One-time environment setup (M Linux devbox, repo root)

```bash
ls -l /dev/kvm                                  # must exist; abort to owner if absent
java -version 2>&1 | head -1                    # need 21; if missing: sudo apt-get update && sudo apt-get install -y temurin-21-jdk
export ANDROID_HOME=$HOME/Android/Sdk ANDROID_SDK_ROOT=$HOME/Android/Sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH
# install cmdline-tools from https://developer.android.com/studio#command-line-tools-only, then:
yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0" "system-images;android-35;google_apis;x86_64"
echo "no" | avdmanager create avd -n qa-pixel -k "system-images;android-35;google_apis;x86_64" -d "pixel_7_pro"
emulator -avd qa-pixel -no-window -no-audio -memory 4096 &
adb wait-for-device
until [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 10; done
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Record the APK's source SHA (`git rev-parse HEAD`) in the ledger's Run context before
executing. If it differs from `da73770`, note it — no results exist yet, so nothing reopens.

## 2. Method contract (non-negotiable)

- Human pace only: screenshot (`adb exec-out screencap -p > shot.png`) → view it → one
  `adb shell input tap x y` / `text` / `keyevent` → observe. No scripts, no assertions,
  no API/DB access, no CI-as-evidence.
- Per attempt record: expected (from ledger/PRD) **before** the decisive tap, observed
  after, screenshot refs, status. PASS needs observed outcome + evidence; unresolved
  expectation = INCONCLUSIVE, never assumed.
- Movement: `adb emu geo fix <lon> <lat>` (SIMULATED label). Share sheet: cancel at
  chooser, never send (J-03). Failing paths: screenshot the exact message.
- Cleanup at end: restore theme/profiles/rates you changed (record originals first).

## 3. Owner must confirm in-session before you touch the app

Install APK, grant location, create local trips (uninstall wipes them), foreground-service
run, share-sheet invocation (cancel-only). No production, no real sends, no account
actions (none exist).

## 4. Execution order

Fresh install → S-023, S-024 (zero states) → S-039 (nav) → S-001 journey →
Direct chain S-006→S-019 → J-02 → Settings S-030→S-038 (with J-01) →
History/Share S-025→S-029 → system S-040→S-042 → recovery S-020, S-045, S-048, S-049 →
CONFIRM items S-043, S-044, S-047 → omission sweep REC-02 (different order) →
counts + report.

## 5. Report back

Status counts (must sum to T), FAIL/INCONCLUSIVE rows with defect records, BLOCKED rows
with missing prerequisite, the updated ledger files, and confirmation that settings were
restored. Do not claim release-readiness.
