# Implementation Plan: Direct Booking & Fare Estimator

> **Document Status**: Approved planning artifact; Unit 1 implementation in progress, validation pending  
> **Product authority**: [Direct Booking & Fare Estimator PRD](../prds/direct-booking-and-fare-estimator.prd.md)  
> **Planning approval**: Project owner approved the Phase 1 baseline/reconciliation and Phase 3 architecture-plan gates on 2026-09-23  
> **Target**: `v0.2.0`, feature delivery into GitHub `dev`  

## 1. Exact baseline

- Remote `origin/dev` and `origin/main`: `19ee2e271c9e38b51d09cf9f0aa4246b1acc0b2e`.
- The direct-booking implementation is present only in the local dirty working tree: 29
  modified or untracked entries. It is not yet an accepted revision, branch, PR, or release.
- Earlier `bun test` and `bun run governance:check` results are retained as informational
  observations only; they are not acceptance evidence for the current implementation.
- Local Gradle test/build verification is intentionally skipped by explicit project-owner
  decision. GitHub CI is the acceptance authority for the exact authorized revision.
- `git diff --check`: two pre-existing blank-line-at-EOF warnings in feature files; not
  normalized during this planning gate.

## 2. Current architecture and ownership

```text
TrackerScreen
  -> TrackerViewModel / TrackerUiState
     -> direct fare and distance use cases (pure Kotlin)
     -> UserPreferencesRepository (domain contract)
     -> LocationRepository (domain contract)
        -> LocationRepositoryImpl (Fused Location + Android Geocoder)
     -> StartTripUseCase / CompleteTripUseCase
        -> TripRepository -> Room TripEntity / TripDao
```

The dependency direction is acceptable: `core:model` and `core:domain` remain free of Android
imports; Android location and persistence are owned by `core:data`; Compose owns rendering and
the ViewModel exposes immutable state.

## 3. Reconciliation findings

### P1 — Quote and realized-earnings fields are conflated

The direct-start path writes the calculated quote into `Trip.platformFeeAmountCents`, while the
completion path overwrites that field with the completion modal's platform-payout input. The
modal currently initializes payout, cash, and quoted-distance fields as empty. A direct run can
therefore lose its quoted fare and quote distance on completion, which violates `FR-DIR-06` and
can change persisted earnings.

### P1 — Tracking has no runtime permission gate

The manifest declares location permissions, but no runtime permission request or denial path was
found. Direct-run start can create a trip and attempt the foreground service without proving that
location updates are available. The implementation unit must make permission state explicit before
starting tracking and preserve a safe manual-quote path when permission is denied.

### P2 — Address lookup is a best-effort platform boundary, not guaranteed offline

`LocationRepositoryImpl` uses Android `Geocoder` and adds `INTERNET`. Fare calculation and local
persistence are offline-capable, but address search/reverse geocoding behavior depends on the
device provider. The product claim must distinguish local quote calculation from optional address
lookup and retain manual address/distance entry as the deterministic fallback.

### P2 — Rate editing persists transient invalid values

Settings writes on every keystroke and converts blank or malformed input to zero. Negative or
otherwise invalid rates are not rejected at the boundary. The implementation unit should keep
editing text local and persist only validated rate values.

## 4. Architecture choices

### Quote persistence

1. **Recommended — distinct `quotedFareAmountCents` field.** Add a nullable quote field to the
   pure model and Room entity, migrate schema v1 to v2 without changing existing rows, preserve
   quote through completion, and keep `platformFeeAmountCents` for realized platform/direct
   transfer earnings. This is the clearest ownership and the only option that preserves both
   quote and realized payment semantics.
2. **Reuse `platformFeeAmountCents` for direct runs.** Avoids a schema migration, but requires
   platform-specific semantics and careful completion rewriting; it is the current failure mode
   and remains vulnerable to data loss or misleading history.
3. **Do not persist the fare.** Recompute or ask again at completion. This is the smallest code
   path but violates the PRD's quote handoff and becomes incorrect if rates change mid-trip.

Recommendation: option 1. The migration is additive and reversible at the source level; a
database downgrade must not be attempted after v2 is installed. Recovery is a forward-compatible
build or an explicit migration, not a destructive downgrade.

### Address and routing boundary

1. **Recommended — native Geocoder/Fused Location with manual fallback.** Keep Android platform
   ownership, accept that lookup may be unavailable, and make manual address plus distance entry
   the guaranteed offline path. No routing backend is added.
2. **Offline address dataset.** Guarantees lookup independence but adds a large, stale data asset
   and update/coverage ownership that the current product does not need.
3. **External Places/routing API.** Improves search and road distance but adds network, secrets,
   cost, privacy, and availability obligations outside the approved v0.2.0 anti-goals.

Recommendation: option 1, with the PRD/status wording narrowed to local calculation and
persistence being offline-guaranteed.

## 5. Implementation units (each needs a separate approval)

### Unit 1 — Quote contract and completion handoff

- Add distinct quote fare persistence with a forward Room migration.
- Pass quote fare through start/complete use cases without overwriting realized earnings.
- Prefill the completion modal from the active direct trip: quote fare into the direct payment
  field and quote distance into discrepancy input; allow edits and preserve explicit zero values.
- Add domain, Room mapping, ViewModel, and modal regression tests.

**Done when:** a direct trip's quote survives process/database reload, completion preserves the
quote and records the final payment independently, and all existing trip earnings tests remain
green.

### Unit 2 — Permission and address fallback

- Add a user-visible runtime location permission gate before tracking starts.
- Keep address lookup cancellable/best-effort and expose manual address/distance entry when it
  fails or is unavailable.
- Verify the network/permission contract and remove any claim that the Geocoder path is strictly
  offline.

**Done when:** permission denial cannot create a falsely tracking trip, manual quoting remains
usable without lookup, and the foreground service starts only after the required permission path.

### Unit 3 — Validated rate editing and UI acceptance

- Keep transient text local while editing and persist only non-negative, finite, bounded rates.
- Verify fare formula edge cases, custom overrides, settings persistence, and direct cockpit UDF.
- Run device/emulator acceptance for the critical quote-to-start-to-complete flow.

**Done when:** invalid input cannot corrupt stored rates and the critical user flow is observable
on a declared Android device profile.

## 6. Verification matrix

| Area | Evidence required |
|---|---|
| Pure domain | JVM tests for fare formula, negative/blank inputs, overrides, distance multiplier, and model contracts |
| Persistence | SharedPreferences tests plus Room v1-to-v2 migration and round-trip tests |
| User flow | ViewModel and Compose-facing tests for quote, start, completion prefill, edit, and save |
| Permissions | Denied/granted runtime permission paths; service start only on an allowed path |
| Offline behavior | Calculation/persistence with no network; address lookup failure falls back to manual input |
| Architecture | No Android imports in `core:model`/`core:domain`; one owner per state/data responsibility |
| Repository gates | GitHub CI: governance checks, Gradle JVM tests, lint, and debug/release build |
| Review/delivery | Exact branch head, GitHub checks, CodeRabbit/GitLab advisory review, no unresolved valid findings |

Local Android verification is intentionally out of scope. CI must run the repository's Gradle
tests, lint, and debug/release build on the exact authorized remote revision before any
implementation completion claim.

## 7. Source control, rollback, and human-only actions

- Start the implementation branch from the current GitHub `dev` tip using the repository's
  `rons/` prefix; target PRs at GitHub `dev`.
- GitHub remains the sole write/merge authority. GitLab is review-only; its mirror and advisory
  MR are not delivery paths.
- Keep the current dirty user work intact until the implementation unit is explicitly selected.
  Do not commit, push, open a PR, merge, or promote from this planning gate.
- Roll back a source change by reverting its atomic commit. For the Room migration, preserve the
  forward schema and use a corrective migration/build; never downgrade an installed v2 database
  destructively.
- Human-only actions later: approve each implementation unit, approve branch/commit/push/PR
  actions that trigger CI, and accept device-flow evidence if the phase requires it.

## Next gate

The active gate is **Unit 1 implementation: quote contract and completion handoff**. Application
edits are authorized for this unit only. CI validation, commit, push, PR, merge, and release
remain separate gates; no local JDK setup or local test/build run is required.
