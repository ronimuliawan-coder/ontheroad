# Implementation Plan: Phase 4 — Core Domain and Data Models

> **PRP Plan**: `.claude/PRPs/plans/04-core-domain-and-data.plan.md`  
> **Lifecycle Phase**: Phase 4 (Core domain and data models)  
> **Status**: Historical evidence / Executed
> **Target Platform**: Android (Kotlin + Jetpack Compose)  
> **Parent PRD**: [`.claude/PRPs/prds/ontheroad-trip-tracker.prd.md`](../prds/ontheroad-trip-tracker.prd.md)  

---

## 1. Executive Summary

Phase 4 deepens the business logic and reactive data layer of **OnTheRoad**. It implements the full suite of domain UseCases, pure Kotlin geo-math (Haversine formula and GPS jitter suppression), pre-seeded gig platforms, and shift financial reconciliation.

All domain logic remains pure Kotlin conforming to invariant **`ARCH-001`**, and all business rules are verified with pure JVM unit tests conforming to **`TEST-001`**.

---

## 2. Architecture & Components

```
core/
├── model/                                (ARCH-001: Pure Kotlin JVM)
│   └── src/main/kotlin/.../model/
│       └── [EXTEND] ShiftSummary.kt     (Consolidated shift metrics model)
│
├── domain/                               (ARCH-001: Pure Kotlin JVM)
│   └── src/main/kotlin/.../
│       ├── geo/
│       │   ├── HaversineDistanceCalculator.kt (Pure Kotlin spherical distance)
│       │   └── GpsJitterFilter.kt             (Pure Kotlin stationary noise filter)
│       └── usecase/
│           ├── StartTripUseCase.kt            (Validates active trip exclusivity & inits)
│           ├── CompleteTripUseCase.kt         (Finalizes trip & calculates metrics)
│           ├── RecordRoutePointUseCase.kt     (Filters jitter & accumulates odometer)
│           ├── GetShiftSummaryUseCase.kt      (Reconciles cash vs digital & expenses)
│           └── GetTripHistoryUseCase.kt       (Queries trips sorted by date or $/km)
│   └── src/test/kotlin/.../              (TEST-001: Pure JVM unit tests)
│       ├── geo/HaversineDistanceCalculatorTest.kt
│       ├── geo/GpsJitterFilterTest.kt
│       ├── usecase/StartTripUseCaseTest.kt
│       ├── usecase/CompleteTripUseCaseTest.kt
│       ├── usecase/RecordRoutePointUseCaseTest.kt
│       └── usecase/GetShiftSummaryUseCaseTest.kt
│
└── data/                                 (Android Library)
    └── src/main/kotlin/.../data/
        ├── local/
        │   ├── dao/
        │   │   ├── [UPDATE] TripDao.kt   (Flow query filters & leaderboard ordering)
        │   │   └── [UPDATE] ShiftDao.kt  (Date-range shift queries)
        │   └── PrepopulateDataCallback.kt(Pre-seeds Grab, Gojek, Uber, ShopeeFood, etc.)
        └── repository/
            ├── [UPDATE] TripRepositoryImpl.kt
            └── [UPDATE] ShiftRepositoryImpl.kt
```

---

## 3. Invariants Verification Matrix

| Invariant | Requirement | Phase 4 Verification |
|---|---|---|
| `ARCH-001` | `domain` and `model` layers are pure Kotlin | Zero `android.*` or `androidx.*` imports. Haversine distance uses pure `kotlin.math`. Checked via static audit. |
| `TEST-001` | Unit tests for domain run on JVM without emulators | Comprehensive JUnit test suite for all UseCases and Geo-math runs in <3s. |
| `SEC-001` | Zero secrets or cloud dependencies | 100% offline-first Room database; zero network calls. |

---

## 4. Execution Steps

1. **Model Extension**:
   - Add `ShiftSummary` data class to `:core:model` capturing total earnings, cash in hand, digital balance, total actual km, total quoted km, total uncompensated km, net profit after expenses.
2. **Pure Kotlin Geo Engine**:
   - Implement `HaversineDistanceCalculator` in `:core:domain:geo`.
   - Implement `GpsJitterFilter` in `:core:domain:geo`.
   - Write comprehensive unit tests verifying distance accuracy and stationary jitter rejection.
3. **Domain UseCases**:
   - Implement `StartTripUseCase`, `CompleteTripUseCase`, `RecordRoutePointUseCase`, `GetShiftSummaryUseCase`, `GetTripHistoryUseCase`.
   - Write comprehensive unit tests for all UseCases with 100% test pass.
4. **Data Layer Enrichment**:
   - Add database pre-population callback for default platforms (`Grab`, `Gojek`, `Uber`, `Lyft`, `ShopeeFood`, `Maxim`, `Indrive`, `Lalamove`, `Direct`) and categories.
   - Enhance `TripDao` with sorted queries (by start time, by profitability `$/km`).
5. **Validation**:
   - Run `./gradlew test` across all modules.
   - Run `bun run governance:check` and `bun test`.
