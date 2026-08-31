# Implementation Plan: Phase 7 — Performance, Motion & UX Polish (120Hz Support)

> **PRP Plan**: `.claude/PRPs/plans/07-performance-motion-and-ux.plan.md`  
> **Lifecycle Phase**: Phase 7 (Performance, motion and UX)  
> **Status**: Historical evidence / Executed
> **Target Platform**: Android (Jetpack Compose + 120Hz High Refresh Rate + Haptics)  
> **Parent PRD**: [`.claude/PRPs/prds/ontheroad-trip-tracker.prd.md`](../prds/ontheroad-trip-tracker.prd.md)  

---

## 1. Executive Summary

Phase 7 polishes the user experience of **OnTheRoad** into a hyper-responsive, high-performance cockpit application:
1. **120Hz High Refresh Rate Display Support**: Configures the Android Window to request the device's peak display mode (120Hz/90Hz) on supported devices (Android 11+ / API 30+).
2. **Fluid Cockpit Motion & Transitions**: Adds physics-based spring animations for screen transitions in `AppNavHost`, animated number counting in `MetricCard`, and animated reveal for `DiscrepancyBadge`.
3. **Tactile Cockpit Haptics**: Tunes haptic feedback on `CockpitButton` and platform chips so drivers get physical confirmation when wearing riding gloves or operating a mounted phone.
4. **Compose Recomposition Optimization**: Optimizes Compose lambdas and state flows to ensure 0 jank and smooth 120fps frame pacing.

---

## 2. Architecture & Components

```
app/
├── src/main/kotlin/com/ontheroad/
│   ├── MainActivity.kt                (Enables 120Hz high refresh rate display mode)
│   ├── navigation/AppNavHost.kt       (Animated slide/fade screen transitions)
│   └── ui/
│       ├── tracker/TrackerScreen.kt   (Animated live distance / speed)
│       └── shift/ShiftScreen.kt       (Animated financial counters)
core/ui/
├── src/main/kotlin/com/ontheroad/core/ui/
│   ├── animation/
│   │   └── CockpitMotion.kt           (Spring tokens tuned for 120Hz fluidity)
│   └── component/
│       ├── MetricCard.kt              (Animated numeric rolling transitions)
│       └── DiscrepancyBadge.kt        (Scale & color transition animations)
```

---

## 3. Invariants & Conventions

| Invariant | Requirement | Phase 7 Implementation |
|---|---|---|
| `UI-001` | Cockpit usability & ergonomics | Haptics provide tactile confirmation; animations communicate state changes without visual distraction. |
| `PERF-001` | 60fps/120fps fluid display | Window requests peak refresh rate mode; Compose transitions use physics springs. |
| `ARCH-001` | Pure Kotlin domain | UI and motion remain encapsulated inside `:core:ui` and `:app`. |
| `BUILD-001` | Pinned versions | Built with pinned Compose BOM and Kotlin compiler. |

---

## 4. Execution Steps

1. **120Hz Window Display Mode**:
   - Implement `setupHighRefreshRateMode()` in `MainActivity.kt` using `Display.supportedModes` to request 120Hz peak mode.
2. **Motion Tokens & Spring Physics**:
   - Create `core/ui/src/main/kotlin/com/ontheroad/core/ui/animation/CockpitMotion.kt` with spring presets (stiffness, damping ratio) optimized for 120fps.
3. **Animated Screen Navigation**:
   - Update `AppNavHost.kt` with horizontal slide and crossfade enter/exit transitions.
4. **Component Micro-Interactions & Haptics**:
   - Add animated numeric transitions to `MetricCard.kt`.
   - Add spring entrance animation to `DiscrepancyBadge.kt`.
   - Ensure tactile haptic response on `CockpitButton.kt` and `PlatformChip.kt`.
5. **Verification & Tests**:
   - Run `./gradlew test assembleDebug`.
   - Run `bun run governance:check && bun test`.
