# OnTheRoad v0.1.0 Release Notes

**Version**: `0.1.0` (Build `1`)  
**Release Date**: August 28, 2026  
**License**: Apache-2.0 / Open Source  
**Target Platform**: Android 8.0+ (API 26 to API 35)  

---

## 🚦 Welcome to OnTheRoad

**OnTheRoad** is an independent, 100% offline-first cockpit trip tracker and financial reconciliation tool designed specifically for gig drivers (rideshare, courier, and food delivery). It empowers drivers with real-time truth: comparing actual driven distance from GPS against the platform's quoted trip distance to uncover uncompensated detours and true $/km profitability.

---

## ✨ Key Features & Capabilities

### 1. True Odometer vs. Platform Quoted Distance Engine
- **Real-Time Route Tracking**: Records continuous breadcrumbs via high-accuracy Android Foreground Service (`LocationTrackingService`).
- **Discrepancy Pill**: Instant color-coded feedback comparing actual distance vs platform quoted distance:
  - 🟢 **Green ("Matches quote" / "Saved")**: Driven within ±50m of platform route.
  - 🟡 **Amber ("+X.X km detour")**: Small detour (+50m to +500m).
  - 🔴 **Red ("+X.X km uncompensated")**: Significant uncompensated detour (>+500m).

### 2. Pure Kotlin GPS Jitter & Teleport Filter (`ARCH-001`)
- **Stationary Drift Suppression**: Rejects fake distance accumulation (<4m movements when vehicle is stopped at traffic lights with speed <0.5 m/s).
- **Spike Rejection**: Ignores inaccurate GPS fixes (>25m accuracy radius) and physical teleport anomalies (>200 km/h).
- **Pure Domain Engine**: Zero Android dependencies in domain UseCases, allowing 100% JVM unit testability.

### 3. Cockpit Usability & 120Hz High Refresh Rate UI (`UI-001`, `PERF-001`)
- **64dp Touch Targets**: Oversized action buttons designed for effortless 1-tap operation on phone mounts.
- **High-Contrast Dark Theme**: Readable in glaring sunlight or dark night driving.
- **120Hz Fluid Rendering**: Automatically requests the peak refresh rate mode (90Hz / 120Hz) on supported Android 11+ devices with low-latency physics spring animations.
- **Sub-30s Rapid Complete**: 1-tap drop-off modal with auto-populated address, cash toggle, platform fee, and instant discrepancy preview.

### 4. Shift Financial & Cash-in-Pocket Reconciliation
- **Digital vs. Cash Breakdown**: Separates in-app digital balances from physical cash collected from riders.
- **Expense Tracking**: Quick logging for fuel, tolls, and maintenance during active shifts.
- **Profitability Metrics**: Real-time average `$/km` and `$/hour` calculations.

### 5. Leaderboard & Trip History
- Filterable and sortable by **Recent Runs**, **Most Profitable ($/km)**, or **Longest Distance**.
- Delete and inspect individual trip breadcrumbs.

### 6. Absolute Privacy & Offline Sovereignty (`SEC-001`)
- **Zero Cloud Accounts**: No logins, no tracking, no external telemetry.
- **Local SQLite Storage**: All coordinates and financial logs remain encrypted and stored strictly on the driver's device via Room Database.

---

## 📦 Release Artifacts & Checksums

| Artifact | File Size | SHA-256 Checksum | Description |
|---|---|---|---|
| `app-release.apk` | **2.2 MB** | `2002e4ab30bed7375a716c21d5596e230ae8425302ef4b14180beee1766b5ab1` | Optimized production Android APK (R8 minified & shrunk) |
| `app-release.aab` | **3.9 MB** | `49916ecf50494b8136184be181ca4b169d2163da42620c2db9d7367aac49d989` | Android App Bundle for Google Play distribution |
| `app-debug.apk` | **28 MB** | `9c8a47f118f22feab7abdd29fa8ca061e39dfda7e26bf1620f07526ed7e5bbb5` | Developer debug build with tooling & previews |

---

## 🛠️ Build & Architecture Summary

- **JDK**: OpenJDK 21
- **Gradle**: 8.11.1
- **Kotlin**: 2.0.21
- **Jetpack Compose BOM**: 2024.12.01
- **Room Database**: 2.6.1
- **Architecture**: Multi-module Clean Architecture (`:core:model`, `:core:domain`, `:core:data`, `:core:ui`, `:app`)
- **Test Suite**: 38 JVM unit tests (100% passing in <2s)
