# Implementation Plan: Phase 3 — Android Toolchain and Architecture Scaffold

> **PRP Plan**: `.claude/PRPs/plans/03-toolchain-and-scaffold.plan.md`  
> **Lifecycle Phase**: Phase 3 (Android toolchain and architecture scaffold)  
> **Status**: Approved Plan / Ready for Execution  
> **Target Platform**: Android (Kotlin + Jetpack Compose)  
> **Parent PRD**: [`.claude/PRPs/prds/ontheroad-trip-tracker.prd.md`](../prds/ontheroad-trip-tracker.prd.md)  

---

## 1. Executive Summary

Phase 3 establishes the compile-time and architectural backbone for **OnTheRoad**. It transitions the project from repository governance into a buildable native Android application with clean multi-module separation.

By configuring pure Kotlin JVM modules (`:core:model` and `:core:domain`) alongside Android modules (`:core:data`, `:core:ui`, and `:app`), invariant **`ARCH-001`** is mechanically enforced at the Gradle compiler level: the domain and model layers cannot import `android.*` or `androidx.*` even by accident.

---

## 2. Architecture & Module Structure

```
ontheroad/
├── gradle/
│   ├── wrapper/
│   │   ├── gradle-wrapper.properties  (Gradle 8.11.1, SHA-256 verified)
│   │   ├── gradle-wrapper.jar
│   │   └── ...
│   └── libs.versions.toml             (BUILD-001: Pinned version catalog)
├── core/
│   ├── model/                         (ARCH-001: Pure Kotlin JVM library)
│   │   ├── build.gradle.kts           (plugins { kotlin("jvm") })
│   │   └── src/main/kotlin/.../model/
│   │       ├── Trip.kt
│   │       ├── RoutePoint.kt
│   │       ├── Platform.kt
│   │       ├── Category.kt
│   │       ├── Shift.kt
│   │       ├── Expense.kt
│   │       ├── DiscrepancyResult.kt
│   │       └── ProfitabilityMetrics.kt
│   ├── domain/                        (ARCH-001: Pure Kotlin JVM library)
│   │   ├── build.gradle.kts           (plugins { kotlin("jvm") }, depends on :core:model)
│   │   ├── src/main/kotlin/.../
│   │   │   ├── repository/
│   │   │   │   ├── TripRepository.kt
│   │   │   │   └── ShiftRepository.kt
│   │   │   └── usecase/
│   │   │       ├── CalculateDiscrepancyUseCase.kt
│   │   │       └── CalculateProfitabilityUseCase.kt
│   │   └── src/test/kotlin/.../       (TEST-001: Pure JVM unit tests)
│   │       ├── CalculateDiscrepancyUseCaseTest.kt
│   │       └── CalculateProfitabilityUseCaseTest.kt
│   ├── data/                          (Android Library)
│   │   ├── build.gradle.kts           (plugins { com.android.library, ksp })
│   │   └── src/main/kotlin/.../data/
│   │       ├── local/
│   │       │   ├── OnTheRoadDatabase.kt
│   │       │   ├── entity/ (TripEntity, RoutePointEntity, etc.)
│   │       │   └── dao/ (TripDao, ShiftDao)
│   │       └── repository/
│   │           ├── TripRepositoryImpl.kt
│   │           └── ShiftRepositoryImpl.kt
│   └── ui/                            (Android Library)
│       ├── build.gradle.kts           (plugins { com.android.library, compose-compiler })
│       └── src/main/kotlin/.../ui/
│           ├── theme/ (Color.kt, Theme.kt, Type.kt)
│           └── components/
└── app/                               (Android Application)
    ├── build.gradle.kts               (plugins { com.android.application, compose-compiler })
    └── src/main/
        ├── AndroidManifest.xml        (Permissions: location, foreground-service, notifications)
        └── kotlin/.../
            ├── OnTheRoadApp.kt        (Application class)
            └── MainActivity.kt        (Compose entry point & navigation scaffold)
```

---

## 3. Dependency Catalog (`gradle/libs.versions.toml`)

All dependencies pinned without dynamic `+` versions (`BUILD-001`):

- **Build Tools**:
  - AGP: `8.8.0`
  - Kotlin: `2.0.21`
  - KSP: `2.0.21-1.0.28`
  - Compose Compiler: Built-in to Kotlin 2.0+ via `org.jetbrains.kotlin.plugin.compose`
- **AndroidX & Jetpack Compose**:
  - Compose BOM: `2024.12.01` (Material 3, Foundation, UI, Preview)
  - Core KTX: `1.15.0`
  - Activity Compose: `1.9.3`
  - Lifecycle Runtime & ViewModel Compose: `2.8.7`
  - Navigation Compose: `2.8.5`
- **Data & Location**:
  - Room: `2.6.1`
  - Coroutines: `1.9.0`
  - Play Services Location: `21.3.0`
- **Testing**:
  - JUnit 4: `4.13.2`
  - MockK: `1.13.13`
  - Turbine: `1.2.0`
  - Coroutines Test: `1.9.0`

---

## 4. Invariants Verification Matrix

| Invariant | Requirement | Phase 3 Verification |
|---|---|---|
| `BUILD-001` | All dependencies pinned in `libs.versions.toml` | Checked by static inspection and verifier |
| `BUILD-002` | Gradle wrapper uses distribution SHA-256 | Configured in `gradle-wrapper.properties` |
| `ARCH-001` | Domain and model layers are pure Kotlin | `:core:model` & `:core:domain` use `kotlin("jvm")` — zero Android SDK on classpath |
| `TEST-001` | Domain unit tests run on JVM without emulators | `./gradlew :core:domain:test` runs in <3s on JVM |
| `UI-001` | Jetpack Compose unidirectional data flow | Theme and MainActivity shell setup |

---

## 5. Execution Steps

1. **Bootstrap Gradle**:
   - Copy Gradle wrapper binaries (`gradle-wrapper.jar`, `gradlew`, `gradlew.bat`) from verified host cache.
   - Configure `gradle/wrapper/gradle-wrapper.properties` with Gradle 8.11.1 and SHA-256 checksum verification.
2. **Version Catalog & Root Configuration**:
   - Create `gradle/libs.versions.toml`.
   - Create `settings.gradle.kts` including `:app`, `:core:model`, `:core:domain`, `:core:data`, `:core:ui`.
   - Create root `build.gradle.kts` and `gradle.properties`.
3. **Scaffold Modules**:
   - Scaffold `:core:model` with domain data classes and enums.
   - Scaffold `:core:domain` with repository interfaces and core UseCases (`CalculateDiscrepancyUseCase`, `CalculateProfitabilityUseCase`).
   - Write pure JVM unit tests in `:core:domain:test`.
   - Scaffold `:core:data` with Room database, DAOs, and repository implementations.
   - Scaffold `:core:ui` with Material 3 Theme tokens (`Color.kt`, `Theme.kt`, `Type.kt`).
   - Scaffold `:app` with `AndroidManifest.xml`, `MainActivity.kt`, and Compose root.
4. **Android CI Workflow**:
   - Create `.github/workflows/android.yml` (JDK 21, Gradle cache, JVM test execution, lint).
5. **Validation**:
   - Execute `./gradlew testDebugUnitTest` -> all JVM tests pass.
   - Execute `./gradlew assembleDebug` -> debug APK builds successfully.
   - Run `bun run governance:check` -> manifest passes with all newly created files registered.
