# Implementation Plan: Phase 9 — Verification & Release Candidate

> **PRP Plan**: `.claude/PRPs/plans/09-verification-and-release-candidate.plan.md`  
> **Lifecycle Phase**: Phase 9 (Verification and release candidate)  
> **Status**: Historical evidence / Executed
> **Target Platform**: Android (Kotlin, Jetpack Compose, Room, R8, 120Hz)  
> **Parent PRD**: [`.claude/PRPs/prds/ontheroad-trip-tracker.prd.md`](../prds/ontheroad-trip-tracker.prd.md)  

---

## 1. Executive Summary

Phase 9 executes end-to-end regression validation, release notes authoring, and release candidate (RC) packaging for **OnTheRoad v0.1.0**:
1. **Full Test & Lint Validation**: Executes 100% of JVM unit tests across all 5 modules and verifies Android Lint cleanliness.
2. **Release Candidate Packaging & Artifact Verification**: Assembles the release APK and AAB bundle with full R8 minification, calculating cryptographic SHA-256 checksums.
3. **Comprehensive Release Notes**: Authors `RELEASE_NOTES.md` capturing all driver capabilities, privacy invariants, and architecture specifications.

---

## 2. Invariants & Conventions

| Invariant | Requirement | Phase 9 Verification |
|---|---|---|
| `TEST-001` | JVM Unit testability | 100% of unit tests pass in <5s without headless/emulator dependencies. |
| `SEC-001` | Zero secrets in repo | Repository audit confirms zero keystores/passwords committed. |
| `BUILD-001` | Pinned versions | All toolchains pinned in `libs.versions.toml`. |
| `UI-001` | Cockpit usability | 64dp primary buttons, high-contrast dark theme, and 120Hz support validated. |
| `PERF-001` | High refresh rate | 120fps physics springs and display mode verified. |

---

## 3. Execution Steps

1. **Full Verification Suite**:
   - Run `./gradlew test assembleRelease bundleRelease`.
   - Verify zero compile warnings or test failures.
2. **Release Notes Authoring**:
   - Create `RELEASE_NOTES.md` documenting features, architecture, and driver ergonomics.
3. **Checksum & Artifact Audit**:
   - Verify SHA-256 integrity and artifact sizes.
4. **Governance Audit**:
   - Run `bun run governance:check && bun test`.
