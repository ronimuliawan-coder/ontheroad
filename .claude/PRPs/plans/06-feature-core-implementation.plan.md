# Implementation Plan: Phase 6 — Feature Core Implementation

> **PRP Plan**: `.claude/PRPs/plans/06-feature-core-implementation.plan.md`  
> **Lifecycle Phase**: Phase 6 (Feature core implementation)  
> **Status**: Historical evidence / Executed
> **Target Platform**: Android (Kotlin + Jetpack Compose + FusedLocation + Room)  
> **Parent PRD**: [`.claude/PRPs/prds/ontheroad-trip-tracker.prd.md`](../prds/ontheroad-trip-tracker.prd.md)  

---

## 1. Executive Summary

Phase 6 implements the complete end-to-end feature set of **OnTheRoad**:
1. Foreground GPS tracking service (`LocationTrackingService`) providing continuous, resilient background location updates with an ongoing driver cockpit notification.
2. Unidirectional Data Flow (UDF) ViewModels (`TrackerViewModel`, `ShiftViewModel`, `HistoryViewModel`) exposing immutable `StateFlow`.
3. Seamless integration between the Compose Cockpit UI and the Room database via domain UseCases.
4. Comprehensive JVM unit tests for all ViewModels using `Turbine` and `kotlinx-coroutines-test`.

---

## 2. Architecture & Components

```
app/
├── src/main/kotlin/com/ontheroad/
│   ├── service/
│   │   └── LocationTrackingService.kt (Foreground service tracking GPS & updating active trip)
│   ├── viewmodel/
│   │   ├── TrackerViewModel.kt        (UDF state holder for Cockpit Tracker)
│   │   ├── ShiftViewModel.kt          (UDF state holder for Shift financial reconciliation)
│   │   └── HistoryViewModel.kt        (UDF state holder for Leaderboard & Trip logs)
│   └── ui/
│       ├── tracker/TrackerScreen.kt   (Bound to TrackerViewModel)
│       ├── shift/ShiftScreen.kt       (Bound to ShiftViewModel)
│       └── history/HistoryScreen.kt   (Bound to HistoryViewModel)
└── src/test/kotlin/com/ontheroad/viewmodel/
    ├── TrackerViewModelTest.kt        (Turbine StateFlow test)
    ├── ShiftViewModelTest.kt          (Shift reconciliation test)
    └── HistoryViewModelTest.kt        (Sorting & filtering test)
```

---

## 3. Invariants & Conventions

| Invariant | Requirement | Phase 6 Implementation |
|---|---|---|
| `UI-001` | Cockpit usability | 1-tap start and 1-tap complete flows connected directly to foreground service and database. |
| `ARCH-001` | Pure Kotlin domain | ViewModels depend on domain UseCases; service passes raw coordinates to `RecordRoutePointUseCase`. |
| `TEST-001` | JVM Unit testability | ViewModels tested with `TestDispatcher` and `Turbine` without requiring emulators. |
| `SEC-001` | Offline privacy | All GPS coordinates and finances stored in local Room database; zero network calls. |

---

## 4. Execution Steps

1. **Foreground Location Tracking Service**:
   - Create `LocationTrackingService.kt` with foreground notification channel, location callback, and `RecordRoutePointUseCase` dispatch.
   - Register service in `AndroidManifest.xml` with `android:foregroundServiceType="location"`.
2. **ViewModel Layer (UDF)**:
   - Implement `TrackerViewModel` managing active trip, platform selection, timer ticking, and trip completion.
   - Implement `ShiftViewModel` managing active shift, expense logging, and shift summary reconciliation.
   - Implement `HistoryViewModel` managing sorting (`Recent`, `Profitability`, `Distance`) and platform filtering.
3. **Screen Integration**:
   - Update `TrackerScreen`, `ShiftScreen`, and `HistoryScreen` to observe ViewModel `StateFlow` states and trigger actions.
4. **Unit Testing & Verification**:
   - Write unit tests for `TrackerViewModel`, `ShiftViewModel`, and `HistoryViewModel`.
   - Run `./gradlew test assembleDebug`.
   - Run `bun run governance:check && bun test`.
