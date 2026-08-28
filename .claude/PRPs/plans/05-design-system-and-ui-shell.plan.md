# Implementation Plan: Phase 5 — Design System and UI Shell

> **PRP Plan**: `.claude/PRPs/plans/05-design-system-and-ui-shell.plan.md`  
> **Lifecycle Phase**: Phase 5 (Design system and UI shell)  
> **Status**: Approved Plan / Ready for Execution  
> **Target Platform**: Android (Kotlin + Jetpack Compose Material 3)  
> **Parent PRD**: [`.claude/PRPs/prds/ontheroad-trip-tracker.prd.md`](../prds/ontheroad-trip-tracker.prd.md)  

---

## 1. Executive Summary

Phase 5 establishes the **Cockpit Design System** and **Compose UI Navigation Shell** for **OnTheRoad**. 

Driving demands high cognitive clarity: the interface must be usable at an arm's length phone mount in direct sunlight or nighttime darkness with zero visual clutter. Every primary action button has a minimum 64dp hit area, critical metrics (distance, discrepancy, rates) use bold high-contrast typography, and the trip completion modal is designed for rapid sub-30-second driver interaction.

---

## 2. Architecture & Components

```
core/
└── ui/                                   (Android Library)
    └── src/main/kotlin/.../ui/
        ├── theme/
        │   ├── Color.kt                  (High-contrast cockpit colors & discrepancy tokens)
        │   ├── Type.kt                   (Large legible odometer/metric typography)
        │   ├── Theme.kt                  (OnTheRoadTheme supporting Dark/Light mode)
        │   └── Dimens.kt                 (Cockpit touch targets: min 56dp, primary 64dp-72dp)
        └── component/
            ├── CockpitButton.kt          (Giant high-contrast touch button)
            ├── MetricCard.kt             (Dashboard metric tile with status badge)
            ├── DiscrepancyBadge.kt       (Colored pill: green match, amber/red discrepancy)
            ├── PlatformChip.kt           (Platform identifier pill with brand colors)
            └── RapidCompleteModal.kt     (Sub-30-second drop-off & earnings completion sheet)

app/                                      (Android Application)
└── src/main/kotlin/.../
    ├── navigation/
    │   ├── Screen.kt                     (Type-safe navigation destinations)
    │   └── AppNavHost.kt                 (NavHost routing Tracker, Shift, History, Settings)
    └── MainActivity.kt                   (BottomNavigationBar shell with Scaffold)
```

---

## 3. Invariants & Conventions

| Invariant | Requirement | Phase 5 Implementation |
|---|---|---|
| `UI-001` | Cockpit usability | Primary action buttons strictly >= 64dp height. High contrast text contrast ratio > 7:1 against dark backgrounds. |
| `UI-002` | Sub-30s interaction | `RapidCompleteModal` fields ordered logically: Dropoff Address -> Platform Fee -> Cash Collected -> Quoted Distance. Real-time discrepancy preview. |
| `ARCH-001` | Pure Kotlin domain | `:core:ui` depends only on `:core:model`. No domain leakage into UI. |
| `BUILD-001` | Pinned versions | Compose BOM `2024.12.01` and Material 3 components pinned in version catalog. |

---

## 4. Execution Steps

1. **Tokens & Dimensions (`:core:ui:theme`)**:
   - Define `Dimens.kt` (touch targets, paddings, corner radii).
   - Refine `Color.kt` and `Type.kt` for cockpit visibility.
2. **Cockpit UI Components (`:core:ui:component`)**:
   - `CockpitButton`: Giant 64dp touch target with iconography and loading states.
   - `MetricCard`: Large numeric display with unit label and auxiliary comparison text.
   - `DiscrepancyBadge`: Highlights uncompensated distance in amber/red.
   - `PlatformChip`: Platform tag with color dot.
   - `RapidCompleteModal`: Rapid bottom sheet modal with real-time discrepancy preview.
3. **App Navigation Shell (`:app`)**:
   - Define `Screen` sealed hierarchy (`Tracker`, `Shift`, `History`, `Settings`).
   - Implement `AppNavHost` connecting bottom navigation tabs to clean screen placeholders.
   - Update `MainActivity.kt` with full navigation integration.
4. **Preview & Verification**:
   - Add Compose `@Preview` annotations for dark and light modes.
   - Execute `./gradlew assembleDebug` and `./gradlew test`.
   - Run `bun run governance:check` and `bun test`.
