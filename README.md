# OnTheRoad 🚗💨

> **Independent Cockpit Trip Tracker & Financial Reconciliation Tool for Gig Drivers**  
> *Rideshare • Courier • Food Delivery*

[![Android CI](https://github.com/rons-space/ontheroad/actions/workflows/android.yml/badge.svg)](https://github.com/rons-space/ontheroad/actions/workflows/android.yml)
[![Governance](https://github.com/rons-space/ontheroad/actions/workflows/governance.yml/badge.svg)](https://github.com/rons-space/ontheroad/actions/workflows/governance.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.12.01-green.svg)](https://developer.android.com/jetpack/compose)
[![120Hz](https://img.shields.io/badge/Display-120Hz%20ProMotion-blue.svg)](#cockpit-ergonomics)
[![Offline First](https://img.shields.io/badge/Storage-100%25%20Offline%20SQLite-orange.svg)](#privacy--offline-sovereignty)

---

## 📖 Overview

**OnTheRoad** is an open-source, 100% local-first Android application built to solve a critical reality faced by gig drivers: **uncompensated detours, inaccurate platform trip quotes, and messy end-of-shift cash reconciliation**.

When driving for platforms like Grab, Gojek, Uber, Lyft, ShopeeFood, Maxim, or InDrive, the quoted distance provided by the platform often underestimates the actual route driven due to traffic detours, roadblocks, or inaccurate routing algorithms. OnTheRoad runs in the cockpit background, continuously measuring the vehicle's true odometer distance against the platform quote and calculating true **$/km** and **$/hour** profitability.

---

## ✨ Key Features

### 1. 🎯 True Odometer vs. Quoted Platform Discrepancy Engine
- **Continuous Foreground GPS Tracking**: High-accuracy `LocationTrackingService` records breadcrumbs even when the screen is locked or while navigation apps (Google Maps, Waze) are active.
- **Instant Discrepancy Pill**:
  - 🟢 **Green ("Matches quote" / "Saved")**: Route matches platform quote within ±50m.
  - 🟡 **Amber ("+X.X km detour")**: Small detour (+50m to +500m).
  - 🔴 **Red ("+X.X km uncompensated")**: Uncompensated detour exceeding +500m.

### 2. ⚡ Pure Kotlin GPS Jitter & Teleport Filter (`ARCH-001`)
- **Stationary Drift Suppression**: Ignores GPS wandering (<4m fixes when stationary at red lights with speed <0.5 m/s).
- **Spike Rejection**: Drops inaccurate fixes (>25m accuracy) and physical teleport spikes (>200 km/h).
- **Pure Domain Engine**: 100% pure Kotlin domain layer with 0 Android framework dependencies for fast, deterministic JVM unit testing.

### 3. 🏎️ Cockpit Usability & 120Hz High Refresh Rate UI (`UI-001`, `PERF-001`)
- **64dp Touch Target Buttons**: Oversized primary buttons (`CockpitButton`) for effortless 1-tap operation on phone mounts.
- **High-Contrast Dark Cockpit Theme**: Designed for bright sunlight glare and night driving.
- **120Hz Display Support**: Automatically requests peak display refresh rates (90Hz / 120Hz) on Android 11+ (API 30+) devices with low-latency physics spring animations.
- **Sub-30s Rapid Complete**: 1-tap drop-off bottom sheet with auto-populated address, cash toggle, platform fee, and instant discrepancy preview.

### 4. 💵 Shift Financial & Cash-in-Pocket Reconciliation
- **Digital Balance vs. Cash in Hand**: Accurately tracks physical cash collected from riders versus digital bank transfers.
- **Expense Logging**: Instant logging for fuel, tolls, and maintenance during active shifts.
- **Real-Time Hourly & Distance Rates**: Live metrics for `$/km` and `$/hour`.

### 5. 🏆 Profitability Leaderboard & History
- Sort trips by **Recent Runs**, **Most Profitable ($/km)**, or **Longest Distance**.
- View complete route breadcrumb records offline.

### 6. 🔒 100% Privacy & Offline Sovereignty (`SEC-001`)
- **Zero Cloud Accounts**: No logins, no telemetry, no tracking servers.
- **Local Room Database**: All coordinates, earnings, and shift data remain strictly on your device.

---

## 🏗️ Architecture

OnTheRoad follows strict **Clean Architecture & Unidirectional Data Flow (UDF)**:

```
┌─────────────────────────────────────────────────────────┐
│                        :app                             │
│  MainActivity • AppNavHost • LocationTrackingService   │
│  ViewModels (Tracker, Shift, History) • Compose Screens │
└───────────────┬─────────────────────────┬───────────────┘
                │                         │
┌───────────────▼─────────┐     ┌─────────▼───────────────┐
│        :core:ui         │     │       :core:data        │
│  CockpitDesignSystem    │     │  OnTheRoadDatabase      │
│  CockpitButton, Cards   │     │  Room DAOs & Entities   │
│  120Hz Physics Springs  │     │  Repository Implementations
└───────────────┬─────────┘     └─────────┬───────────────┘
                │                         │
┌───────────────▼─────────────────────────▼───────────────┐
│                      :core:domain                       │
│  UseCases: StartTrip, RecordPoint, CompleteTrip         │
│  Geo Engine: Haversine, GpsJitterFilter                 │
│  (Pure Kotlin — Zero Android Dependencies)              │
└───────────────────────────┬─────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────┐
│                       :core:model                       │
│  Trip, Shift, Expense, Platform, RoutePoint             │
│  (Pure Kotlin Domain Models)                            │
└─────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack & Toolchain

- **Runtime & Language**: OpenJDK 21, Kotlin 2.0.21
- **Build System**: Gradle 8.11.1 with version catalogs (`gradle/libs.versions.toml`)
- **UI Framework**: Jetpack Compose (BOM `2024.12.01`), Material 3
- **Location Engine**: Google Play Services Location (`FusedLocationProviderClient`)
- **Local Storage**: Android Room SQLite 2.6.1 with KSP
- **Async Concurrency**: Kotlin Coroutines 1.9.0, StateFlow, Turbine
- **Code Minification**: ProGuard & R8 (current release APK is **2.24 MB**)
- **Governance**: Bun 1.4 for repository manifest validation and invariant enforcement

---

## 🚀 Building & Testing

### Prerequisites
- **JDK 21** (`export JAVA_HOME=/path/to/jdk-21`)
- **Bun 1.4+** (`bun install` in repository root for governance scripts)

### Run Multi-Module JVM Unit Tests
```bash
./gradlew test
```
*Executes all 43 JVM unit tests across `:core:model`, `:core:domain`, `:core:data`, `:core:ui`, and `:app` without an emulator.*

### Build Production Release APK & App Bundle
```bash
./gradlew assembleRelease bundleRelease
```
*Generates optimized release APK at `app/build/outputs/apk/release/app-release.apk` (2.24 MB).*

### Verify Repository Governance
```bash
bun run governance:check && bun test
```

---

## 📦 Verified Working-Tree Artifacts (post-v0.1.0)

The identities below were measured from the verified working tree at
`ab782714dcad3ab0bd8cb77bf6abec705bb3fd07`. They are not replacements for the
version-specific artifacts recorded in [`RELEASE_NOTES.md`](RELEASE_NOTES.md).

| Artifact | File Size | SHA-256 Checksum |
|---|---|---|
| `app-release.apk` | **2,239,791 bytes (2.24 MB)** | `1072752b0f6104a643311149f31caec909bac0b40a585c3f49bab662561f2426` |
| `app-release.aab` | **4,051,181 bytes (4.05 MB)** | `93a3454c29ff2aac9fd1477611026d6315477fd02567d037a953f64e57ff38f6` |
| `app-debug.apk` | **17,743,073 bytes (17.74 MB)** | `4c178ab4e796b22a45883f6835e2dcf51533b7701efbb5ed1ffe229361f9f6c2` |

---

## 📄 License

```
Copyright 2026 OnTheRoad Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
