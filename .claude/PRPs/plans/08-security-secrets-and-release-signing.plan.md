# Implementation Plan: Phase 8 — Security, Secrets & Release Signing

> **PRP Plan**: `.claude/PRPs/plans/08-security-secrets-and-release-signing.plan.md`  
> **Lifecycle Phase**: Phase 8 (Security, secrets and release signing)  
> **Status**: Approved Plan / Ready for Execution  
> **Target Platform**: Android (Gradle Signing + ProGuard/R8 + CI Secrets)  
> **Parent PRD**: [`.claude/PRPs/prds/ontheroad-trip-tracker.prd.md`](../prds/ontheroad-trip-tracker.prd.md)  

---

## 1. Executive Summary

Phase 8 establishes production-grade security, code obfuscation/optimization (R8), and automated CI release signing for **OnTheRoad**:
1. **Zero-Secret Invariant (`SEC-001`)**: Strict enforcement that no private keys (`*.jks`, `*.keystore`), passwords, or API tokens are ever checked into Git.
2. **Dynamic Signing Configuration**: `app/build.gradle.kts` dynamically inspects environment variables (`KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) or `local.properties`, falling back gracefully to debug signing for local offline builds.
3. **ProGuard / R8 Optimization**: Comprehensive obfuscation and resource shrinking rules preserving Room, Kotlin Coroutines, and Compose stability while pruning unused code.
4. **Automated CI Release Workflow**: `.github/workflows/release.yml` for building and signing release APKs and AAB bundles with SHA-256 integrity checksums.

---

## 2. Architecture & Components

```
.github/
└── workflows/
    └── release.yml                    (CI release build, signing, and artifact generation)
app/
├── build.gradle.kts                   (Dynamic release signing config & R8 minification)
└── proguard-rules.pro                 (Room, Coroutines, and Compose R8 rules)
```

---

## 3. Invariants & Conventions

| Invariant | Requirement | Phase 8 Implementation |
|---|---|---|
| `SEC-001` | Zero secrets in repo | `.gitignore` rules prevent keystores/credentials; CI passes secrets via environment. |
| `BUILD-001` | Pinned versions | Pinned ProGuard/R8 AGP toolchain. |
| `TEST-001` | JVM Unit testability | Local and CI unit tests run unconditionally without needing signing keys. |

---

## 4. Execution Steps

1. **R8 ProGuard Rules**:
   - Update `app/proguard-rules.pro` with keep rules for Room entities, Compose runtime, and Coroutines.
2. **Release Signing Configuration**:
   - Update `app/build.gradle.kts` with `signingConfigs.register("release")` reading env vars with safe fallbacks.
   - Enable `isMinifyEnabled = true` and `isShrinkResources = true` on `release` build type.
3. **CI Release Workflow**:
   - Create `.github/workflows/release.yml` with tag triggers, base64 keystore decoding, and APK/AAB artifact uploads.
4. **Verification & Checks**:
   - Verify `./gradlew assembleRelease` and `./gradlew test`.
   - Run `bun run governance:check && bun test`.
