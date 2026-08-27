# OnTheRoad Baseline — Pre-Development Foundation

This is the frozen pre-development baseline evidence for **OnTheRoad**.
It records the verified system environment, toolchain versions, and Git state observed
before product code was introduced.

---

## Capture Identity

| Field | Observed Value |
|---|---|
| Captured at | `2026-08-27T21:17:00+07:00` |
| Time zone | Asia/Jakarta (`UTC+07:00`) |
| Host | Linux `7.1.9-arch1-2`, x86_64 |
| Initial branch | `main` |
| Target project | `ontheroad` |
| Linear project | `OnTheRoad` (Linear workspace: `rons-space`) |
| Approved Phase 1 Plan | `.system_generated/implementation_plan.md` |

---

## Host System & Toolchain Evidence

| Tool / Component | Observed Path / Version |
|---|---|
| OpenJDK 21 | `21.0.11` (`/home/ron/.jdks/jbr-21.0.11/bin/java`) |
| Android SDK Root | `/home/ron/Android/Sdk` |
| Android SDK Platforms | `android-35`, `android-36`, `android-36.1` |
| Android Build Tools | `35.0.0`, `36.0.0`, `36.1.0`, `37.0.0` |
| Android ADB | `1.0.41` (Version `37.0.1-15733141`) |
| Node.js | `v26.7.0` (`/usr/bin/node`) |
| Bun | `1.4.0` (`/usr/bin/bun`) |
| Git | `2.55.0` (`/usr/bin/git`) |
| ECC Version | `2.2.0` (commit `63040e29b52f51709d0255a461a7a762edced8bb`) |

Reproduce toolchain evidence:

```bash
/home/ron/.jdks/jbr-21.0.11/bin/java -version
ls -la /home/ron/Android/Sdk/platforms
ls -la /home/ron/Android/Sdk/build-tools
/home/ron/Android/Sdk/platform-tools/adb version
node --version
bun --version
git --version
```

---

## Repository State at Capture

- Initialized Git repository on `main`.
- Installed curated ECC `.agents/` profile:
  - 10 common engineering rules + 5 Kotlin rules + README
  - 73 workflow command definitions (including `prp-prd.md`)
  - 30 agent role definitions
  - 66 curated skill directories
- Automated zero-dependency verifier: `scripts/verify-governance.mjs`.
- Dual-workflow CI: `.github/workflows/governance.yml` and `sync-dev-to-main.yml`.
- Product code (`app/`, `core/`, `gradle/`): **None** (deferred to Phase 3 following PRD approval).
