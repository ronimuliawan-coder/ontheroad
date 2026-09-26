# Human journey testing — run ledger (OnTheRoad, prep-only)

Adapted from the `human-journey-testing` skill RUN-LEDGER template. No scenario has been
executed: this ledger is the reconciled inventory + `NOT_RUN` queue for a future
device-capable session. Statuses must not be changed without UI observation.

## Run context

| Field | Value |
| --- | --- |
| Run ID / started / timezone | `otr-qa-2026-09-26` / 2026-09-26 / Asia/Jakarta |
| Target URL or app / environment | OnTheRoad native Android app (Kotlin + Compose); no built APK in this session |
| Observed build/version and evidence; unknown if unavailable | Source revision `dev` @ `da73770` (PR #17 merge). Running-build version UNKNOWN — no APK was built here |
| Requested scope and explicit exclusions | Whole human-facing app. Excluded: code fixes, CI/unit evidence as pass proof, production deploy |
| Discovery mode / available sources / unavailable sources | Source + docs read-only (app/ui sources, PRD, plan 11). UI inspection and device execution UNAVAILABLE |
| Actual roles and account aliases; no credentials | Single human role: driver (no accounts — offline app). QA is the testing perspective, not an account |
| Workspace/data aliases and fixture provenance | No fixtures created. Execution session must create trips through the UI on a test device |
| Browser/app, viewport/device, input and assistive capabilities | None available. Target for execution: physical device or emulator, touch input |
| Emulation vs actual hardware | Neither yet; label future evidence UI_OBSERVED / SIMULATED / PHYSICAL_OBSERVED |
| Authorized side effects / actions needing authorization | Prep only — no side effects authorized in this session. Execution needs: local trips + GPS foreground service on a TEST device, share-sheet invocation (no external send) |
| Evidence directory and sensitive-data handling | This directory. Redact street addresses from screenshots if shared; no credentials exist in-app |
| Inventory revision and last reconciliation | Rev 1, 2026-09-26 (source reconciliation; no-new-item UI sweep NOT yet possible) |

## Ownership and engineering context

- Requested/actual mode: SAME_AGENT (prep only); fallback reason: DEDICATED_QA tester dispatch with UI handoff unavailable in this harness; tester model request (gpt-5.6-luna/xhigh) not applicable — no tester spawned.
- Coordinator/tester aliases and assigned execution roles; no recursive delegation: coordinator = this session (prep). No tester assigned yet.
- Tester UI capability confirmed; exclusive session/artifact ownership and handoff state: UI capability NOT confirmed — execution BLOCKED (B-001). Artifact owner: coordinator.
- Current approved engineering phase/scope and acceptance-plan link, if integrated: standalone prep; repo governance lifecycle Phase 10 complete, Units 1–4 accepted. This ledger does not authorize fixes or release.
- Criteria → feature/scenario mapping: PRD `.claude/PRPs/prds/direct-booking-and-fare-estimator.prd.md` (FR-DIR-01..10) → F-005..F-019, F-025..F-036.
- Canonical evidence-log/tracker link and posting authorization, if applicable: Linear project `OnTheRoad: Direct Booking & Fare Estimator`; no QA comments posted (not authorized in this prep session).

## Roles, accounts, and sessions

Single role (driver), no sign-in, no sessions. Session map not applicable — basis: offline
single-user app, no auth in source. Revisit if a future build adds accounts.

### Effective policy and trigger

- Canonical policy location / revision: skill defaults (no saved engagement policy in this repo).
- Current trigger decision: DEFERRED for execution (B-001); prep work performed under the user's explicit MANUAL request for ledger preparation only.
- Trigger event/request, campaign ID/scope/context and pending/in-progress/finished state: user request 2026-09-26, campaign `otr-qa-2026-09-26`, whole-app scope, preparation FINISHED / execution PENDING.
- Requested tester model/effort; actual model/effort or UNVERIFIED: none requested for prep; no tester dispatched.
- Runtime capability/fallback reason and actual execution mode: no JDK/SDK/device in this environment → SAME_AGENT prep only, execution BLOCKED.

| Setting | Effective value | Source | Change reason/date |
| --- | --- | --- | --- |
| tester_mode | SAME_AGENT (prep) | current prompt + capability fallback | B-001, 2026-09-26 |
| tester_model / tester_reasoning | n/a for prep | default | no tester dispatched |
| qa_trigger | MANUAL (prep scope only) | current prompt | explicit user request |
| discovery / scope / roles / devices | source+docs / whole app / driver / none yet | current prompt | — |
| target / permitted side effects | app source @ da73770 / none authorized | current prompt | prep only |
| promotion_rule / evidence_reuse | n/a | default | — |

### Promotion identity — NOT_APPLICABLE (no promotion candidate; standalone prep)

### Session map — NOT_APPLICABLE (no accounts/sessions; see above)

## Discovery source register

| Source ID | Product area and inspected code/docs/UI references | Date/version relevance | Roles/conditions inspected | Candidates found | Unavailable/uninspected areas; next action |
| --- | --- | --- | --- | --- | --- |
| SRC-NAV | `navigation/Screen.kt`, `AppNavHost.kt`, `MainActivity.kt` (4 routes, start=tracker, no args/deep links) | dev @ da73770 | driver, default config | F-037, F-038 | UI rendering uninspected — execute on device |
| SRC-TRACKER | `ui/tracker/TrackerScreen.kt` (+TrackerViewModel skim for guards) | dev @ da73770 | driver idle/tracking/direct, permission states | F-001..F-005, F-018, F-019 | Error-toast rendering absent in source (see S-043); platform-tap-while-tracking guard untraced (see S-044) |
| SRC-DIRECT | `core/ui/.../DirectBookingCard.kt` (full read) | dev @ da73770 | driver, all card states | F-006..F-012 | Start-button enablement not enforced in card — downstream behavior to confirm (S-015) |
| SRC-MODAL | `core/ui/.../RapidCompleteModal.kt` | dev @ da73770 | driver platform/direct completion | F-013..F-017 | Notes input not rendered though `notesText` is passed (see S-047) |
| SRC-SHIFT | `ui/shift/ShiftScreen.kt` | dev @ da73770 | driver read-only | F-020, F-021 | None |
| SRC-HISTORY | `ui/history/HistoryScreen.kt` | dev @ da73770 | driver | F-022..F-029 | Share-failure path needs induced failure (S-046) |
| SRC-SETTINGS | `ui/settings/SettingsScreen.kt` | dev @ da73770 | driver | F-029..F-036 | Success surfacing beyond button enablement unconfirmed in source (S-031..S-036 check persistence cross-surface) |
| SRC-RECEIPT | `receipt/DirectTripReceiptContent.kt`, `DirectTripReceiptShare.kt` | dev @ da73770 | driver | F-025, F-027, F-028 | Rendered PNG layout uninspected — screenshot on device |
| SRC-PERMS | Permission flow in TrackerScreen; MainActivity has none | dev @ da73770 | driver grant/deny | F-039, F-040 | Permanent-denial vs first-denial path untraced; notification tap behavior untraced (S-042) |
| DOC-PRD | Direct Booking PRD (FR-DIR-01..10, critical user flow) | approved 2026-09-23 | driver | Expected outcomes for S-007..S-019, S-027 | PRD success metrics (15s quote, 0-network) need device measurement, not source proof |
| UI-LIVE | Rendered UI on device/emulator | — | — | — | ENTIRELY UNINSPECTED — execution session must perform navigation-led + task-led sweeps and update this register |

## Surfaces

| Surface ID | UI entry point / navigation path | Roles and contexts inspected | Controls/capabilities → feature IDs | Uninspected states or gaps |
| --- | --- | --- | --- | --- |
| SUR-01 | Tracker tab (start destination) | driver (source only) | F-001..F-005, F-018, F-019 | All rendered states; error surfacing |
| SUR-02 | Direct quote card (Tracker, idle + Direct platform) | driver (source only) | F-006..F-012 | All rendered states; suggestion-dropdown behavior |
| SUR-03 | RapidCompleteModal (bottom sheet from Tracker) | driver (source only) | F-013..F-017 | Prefill values; validation gating; dismiss |
| SUR-04 | Shift tab | driver (source only) | F-020, F-021 | Rendered layout |
| SUR-05 | History tab | driver (source only) | F-022..F-029 | Cards, sort animation, share icon visibility |
| SUR-06 | Settings tab | driver (source only) | F-029..F-036 | Dialogs, validation styling, theme application |
| SUR-07 | System: permission dialog, foreground notification, share chooser | driver (source only) | F-019, F-025, F-039, F-040, F-041 | All — device-only surfaces |

## Feature inventory

| Feature ID | Capability/action | Surface/source IDs | Roles and meaningful states | Candidate disposition and basis | Scenario IDs |
| --- | --- | --- | --- | --- | --- |
| F-001 | Select platform (Grab/Gojek/Uber/Lyft/ShopeeFood/Direct) | SUR-01 / SRC-TRACKER | driver; idle + tracking(?) | CURRENT (source-confirmed) | S-001, S-044 |
| F-002 | Start Trip (platform), permission-gated | SUR-01 / SRC-TRACKER | driver; granted/denied | CURRENT | S-002, S-040 |
| F-003 | Live metrics (distance/duration/speed) + idle placeholders | SUR-01 / SRC-TRACKER | driver; idle/tracking | CURRENT | S-003 |
| F-004 | Open completion modal via Complete Trip | SUR-01, SUR-03 / SRC-TRACKER | driver; tracking | CURRENT | S-005 |
| F-005 | Direct platform swaps cockpit to quote card | SUR-02 / SRC-TRACKER | driver; idle+Direct | CURRENT (PRD §4.1) | S-006 |
| F-006 | Pickup entry: type + suggestions + GPS snap | SUR-02 / SRC-DIRECT | driver; typing/searching/denied | CURRENT (FR-DIR-07) | S-007, S-008 |
| F-007 | Destination entry + suggestions + auto distance | SUR-02 / SRC-DIRECT | driver; match/no-match | CURRENT (FR-DIR-03) | S-009, S-010 |
| F-008 | Manual distance override → fare recalculates | SUR-02 / SRC-DIRECT | driver; auto/manual | CURRENT (FR-DIR-04) | S-011 |
| F-009 | Live fare quote display vs configured rates | SUR-02 / SRC-DIRECT | driver | CURRENT (FR-DIR-02) | S-012 |
| F-010 | Rate-profile selector in booking form | SUR-02 / SRC-DIRECT | driver; 1..n profiles | CURRENT (FR-DIR-09) | S-013 |
| F-011 | Custom fare override toggle + field | SUR-02 / SRC-DIRECT | driver; auto/custom | CURRENT (FR-DIR-04) | S-014 |
| F-012 | Start Direct Run, permission-gated, persists quote | SUR-02 / SRC-DIRECT | driver; granted/denied | CURRENT (FR-DIR-05) | S-015, S-040 |
| F-013 | Completion modal prefill (fare, distance) | SUR-03 / SRC-MODAL | driver; direct/platform | CURRENT (FR-DIR-06) | S-016 |
| F-014 | Cash toggle reveals cash field | SUR-03 / SRC-MODAL | driver; cash on/off | CURRENT | S-017 |
| F-015 | Direct paid-total validation gates Save | SUR-03 / SRC-MODAL | driver; valid/invalid | CURRENT | S-018, S-019 |
| F-016 | Save & Finish finalizes trip, stops service | SUR-03 / SRC-MODAL | driver | CURRENT | S-019 |
| F-017 | Dismiss modal without saving keeps trip active | SUR-03 / SRC-MODAL | driver | CURRENT | S-020 |
| F-018 | Permission-denied inline message + manual fallback | SUR-01, SUR-02 / SRC-TRACKER | driver; denied | CURRENT | S-021 |
| F-019 | Foreground-service notification while tracking | SUR-07 / SRC-TRACKER + service | driver; tracking | CURRENT | S-004, S-042 |
| F-020 | Shift aggregates (revenue/balance/odometer/rates) | SUR-04 / SRC-SHIFT | driver; populated | CURRENT | S-022 |
| F-021 | Shift zero state (fresh install) | SUR-04 / SRC-SHIFT | driver; empty | CURRENT | S-023 |
| F-022 | History empty state | SUR-05 / SRC-HISTORY | driver; empty | CURRENT | S-024 |
| F-023 | Trip card content (platform, earnings, route, rate, badge) | SUR-05 / SRC-HISTORY | driver; populated | CURRENT | S-025 |
| F-024 | Sort orders (Recent/Profitable/Longest) | SUR-05 / SRC-HISTORY | driver | CURRENT | S-026 |
| F-025 | Receipt share via chooser (eligible direct trips) | SUR-05, SUR-07 / SRC-HISTORY, SRC-RECEIPT | driver; eligible | CURRENT (FR-DIR-10) | S-027 |
| F-026 | Share icon absent (platform/incomplete trips) | SUR-05 / SRC-HISTORY | driver; ineligible | CURRENT | S-029 |
| F-027 | Receipt content accuracy (amount, addresses, distance, date) | SUR-07 / SRC-RECEIPT | driver | CURRENT (FR-DIR-10) | S-028 |
| F-028 | Receipt-generation failure toast | SUR-05 / SRC-HISTORY | driver; induced failure | CURRENT (conditional) | S-046 |
| F-029 | Theme switching (System/Dark OLED/Light) | SUR-06 / SRC-SETTINGS | driver | CURRENT | S-030 |
| F-030 | Profile select persists | SUR-06 / SRC-SETTINGS | driver; 1..n profiles | CURRENT (FR-DIR-09) | S-031 |
| F-031 | Profile create + name validation | SUR-06 / SRC-SETTINGS | driver; valid/dup/blank | CURRENT (FR-DIR-09) | S-032, S-033 |
| F-032 | Profile delete + confirm + last-profile guard | SUR-06 / SRC-SETTINGS | driver; 2..n vs 1 profile | CURRENT (FR-DIR-09) | S-034 |
| F-033 | Rate input validation (invalid → error, Save disabled) | SUR-06 / SRC-SETTINGS | driver; invalid/valid-unchanged/valid-changed | CURRENT (FR-DIR-01, FR-DIR-08) | S-035 |
| F-034 | Save rates persists and drives quotes | SUR-06 → SUR-02 / SRC-SETTINGS | driver | CURRENT (FR-DIR-01) | S-036 |
| F-035 | Detour help text | SUR-06 / SRC-SETTINGS | driver | CURRENT | S-037 |
| F-036 | Static preference cards (display only) | SUR-06 / SRC-SETTINGS | driver | CURRENT | S-038 |
| F-037 | Bottom-nav across 4 tabs + state restore | SUR-01..06 / SRC-NAV | driver | CURRENT | S-039 |
| F-038 | Screen slide transitions | SUR-01..06 / SRC-NAV | driver | CURRENT (visual) | S-041 |
| F-039 | System permission Allow → pending action proceeds | SUR-07 / SRC-PERMS | driver; first ask | CURRENT | S-039 |
| F-040 | System permission Deny → inline message, no trip | SUR-07 / SRC-PERMS | driver; denied | CURRENT | S-040 |
| F-041 | Notification tap behavior | SUR-07 / SRC-PERMS | driver; tracking | CURRENT (details uncertain) | S-042 |

## Coverage family decisions

| Family | Applicable / irrelevant / unresolved | Feature/scenario IDs or reason/source |
| --- | --- | --- |
| Entry and navigation | Applicable | F-037, F-005; S-006, S-039. No onboarding/login/deep links exist in source — verified absence, not a gap |
| Identity and access | NOT_APPLICABLE (justified) | Offline single-user app; no auth/accounts in source (SRC-NAV, MainActivity) |
| Discovery and collections | Applicable | F-024 sort (S-026); suggestion search/no-results (S-007, S-009, S-010). No pagination/bulk actions exist |
| Record lifecycle | Applicable | Trips: create (S-002, S-015) → read (S-025) → complete (S-019); no edit/delete/duplicate in source — verified absence |
| Inputs and validation | Applicable | F-006..F-008, F-011, F-014, F-031, F-033; S-007..S-011, S-014, S-017, S-032, S-033, S-035 |
| Workflow states | Applicable | Idle/tracking (S-002..S-005), auto/manual quote (S-008, S-011), empty/populated (S-023, S-024), granted/denied (S-039, S-040) |
| Cross-role handoffs | Applicable (single-role chains) | J-01 (settings→tracker), J-02 (tracker→history), J-03 (history→share). No second role exists |
| Money and entitlements | Applicable | F-009, F-014..F-016, F-020, F-027; S-012, S-017..S-019, S-022, S-028. No tax/discount/refund concepts — verified absence |
| Files and output | Applicable | F-025, F-027 (PNG receipt + chooser); S-027, S-028. No upload path exists |
| Communication | Applicable (narrow) | F-019 notification (S-004, S-042). No in-app messaging/mailbox — verified absence |
| Settings and administration | Applicable | F-029..F-036; S-030..S-038. Restore-original-settings step required at campaign end (see continuation packet) |
| Interaction and presentation | Applicable | F-038 transitions, dialogs/dismiss (S-020, S-034), readable errors (S-018, S-035), touch targets; real-device-only behavior stays device-bound |
| Recovery and continuity | Applicable | S-020 (dismiss), S-045 (rotation/process death), S-048 (double-tap), S-039 (state restore across tabs) |

## Feature dimensions and combination decisions

| Feature ID | Relevant dimensions and values | Scenario IDs | Omitted dimensions / rationale | Remaining gaps |
| --- | --- | --- | --- | --- |
| F-006, F-007 | Match / no-match / GPS-denied; 1-char vs 2+ chars (debounce threshold) | S-007..S-010 | Single locale (en/ID) sampled; other locales deferred — rationale: string resources exist but locale matrix is out of prep scope | Locale coverage |
| F-012, F-002 | Permission granted / denied; direct vs platform | S-002, S-015, S-039, S-040 | Permanent-denial ("don't ask again") path untraced in source — must be discovered on device, not omitted | Permanent-denial behavior |
| F-015, F-016 | Transfer-only / cash-only / mixed / invalid-total | S-017..S-019 | Currency formats beyond default locale deferred | Currency-locale matrix |
| F-031, F-032 | Valid / blank / duplicate / max-length names; delete with 1 vs n profiles | S-032..S-034 | Max-length boundary value (MAX_PROFILE_NAME_LENGTH) — scenario queued (S-032 includes boundary probe) | None |
| F-024 | 3 sort orders × populated list | S-026 | Empty-list sort n/a (no control effect) — justified | None |
| F-009, F-034 | Rate change → quote propagation (cross-surface) | S-012, S-036, J-01 | Full fare-formula edge matrix already covered by JVM tests (not UI evidence) | UI-observed formula spot-checks only |

## Scenario index

All rows `NOT_RUN` (prep only; B-001 blocks execution). Expected outcomes cite PRD sections or
source strings; `[ASSUMPTION]` marks inference to confirm on device, `[CONFIRM]` marks
source-uncertain behavior the executor must settle (INCONCLUSIVE if unresolvable, never assumed).

| Scenario ID | Feature IDs | Actual role/account alias | Variant/context | Dependencies | Current status | Latest attempt / evidence | Defect, blocker, or applicability reason |
| --- | --- | --- | --- | --- | --- | --- | --- |
| S-001 | F-001 | driver | Select each of 6 platform chips, idle | — | NOT_RUN | — | B-001 |
| S-002 | F-002 | driver | Start platform trip, permission granted | S-039 | NOT_RUN | — | B-001 |
| S-003 | F-003 | driver | Idle placeholders (Ready/--:--/0) | — | NOT_RUN | — | B-001 |
| S-004 | F-019 | driver | Notification present + live distance while tracking | S-002 | NOT_RUN | — | B-001 |
| S-005 | F-004 | driver | Complete Trip opens modal | S-002 | NOT_RUN | — | B-001 |
| S-006 | F-005 | driver | Select Direct idle → quote card replaces metrics, no Start Trip button | — | NOT_RUN | — | B-001 |
| S-007 | F-006 | driver | Pickup typing (2+ chars) → suggestions → select fills field | — | NOT_RUN | — | B-001 |
| S-008 | F-006 | driver | GPS snap button → address filled (or fallback hint if denied/unavailable) | S-039 or S-040 | NOT_RUN | — | B-001 |
| S-009 | F-007 | driver | Destination entry → top-match auto distance | S-007 | NOT_RUN | — | B-001 |
| S-010 | F-007 | driver | Destination no-match → fallback hint, manual entry usable | — | NOT_RUN | — | B-001 |
| S-011 | F-008 | driver | Manual distance edit → fare recalculates, caption switches to manual | S-009 | NOT_RUN | — | B-001 |
| S-012 | F-009 | driver | Quote matches `Max(Min, Base + max(0, km-baseKm)×rate)` for active profile (PRD §4.3) | S-009 | NOT_RUN | — | B-001 |
| S-013 | F-010 | driver | Switch profile chip → rates/fare update (needs 2 profiles; create via S-032) | S-032 | NOT_RUN | — | B-001 |
| S-014 | F-011 | driver | Custom-fare toggle → override field; displayed fare follows override | S-009 | NOT_RUN | — | B-001 |
| S-015 | F-012 | driver | Start Direct Run → trip persists quote, tracking starts | S-009 | NOT_RUN | — | B-001 |
| S-016 | F-013 | driver | Modal prefills quoted fare + distance (PRD §4.5, FR-DIR-06) | S-015 | NOT_RUN | — | B-001 |
| S-017 | F-014 | driver | Cash toggle reveals cash field; off hides it | S-015 | NOT_RUN | — | B-001 |
| S-018 | F-015 | driver | Invalid paid total → Save disabled + `direct_customer_total_invalid` | S-015 | NOT_RUN | — | B-001 |
| S-019 | F-016 | driver | Valid completion → trip saved, modal closes, service stops | S-015 | NOT_RUN | — | B-001 |
| S-020 | F-017 | driver | Dismiss modal (swipe/back) → trip still active, quote intact on reopen | S-015 | NOT_RUN | — | B-001 |
| S-021 | F-018 | driver | Permission denied → inline message, manual quoting works, no trip created | S-040 | NOT_RUN | — | B-001 |
| S-022 | F-020 | driver | Shift aggregates reflect completed trips | S-019 | NOT_RUN | — | B-001 |
| S-023 | F-021 | driver | Fresh install: zeros, no crash | — | NOT_RUN | — | B-001 (needs clean install) |
| S-024 | F-022 | driver | Empty history text | S-023 context | NOT_RUN | — | B-001 |
| S-025 | F-023 | driver | Card fields (platform, earnings, route, rate, badge iff quoted) | S-019 | NOT_RUN | — | B-001 |
| S-026 | F-024 | driver | Each sort order reorders list (needs ≥3 varied trips) | S-019 ×3 | NOT_RUN | — | B-001 |
| S-027 | F-025 | driver | Share eligible direct trip → system chooser appears, PNG created | S-019 (direct) | NOT_RUN | — | B-001 |
| S-028 | F-027 | driver | Receipt shows date, endpoints, actual distance, paid total; no coords/notes/earnings | S-027 | NOT_RUN | — | B-001 |
| S-029 | F-026 | driver | Platform + incomplete trips show no share icon | S-002, S-015 | NOT_RUN | — | B-001 |
| S-030 | F-029 | driver | Each theme applies app-wide immediately | — | NOT_RUN | — | B-001 |
| S-031 | F-030 | driver | Select profile → persists across Settings revisit + booking form | — | NOT_RUN | — | B-001 |
| S-032 | F-031 | driver | Create profile (valid + max-length boundary) → appears in list | — | NOT_RUN | — | B-001 |
| S-033 | F-031 | driver | Duplicate/blank name → inline error, confirm disabled | — | NOT_RUN | — | B-001 |
| S-034 | F-032 | driver | Delete with confirm → removed; single-profile delete disabled | S-032 | NOT_RUN | — | B-001 |
| S-035 | F-033 | driver | Invalid rate input → error outline + message, Save disabled until valid+changed | — | NOT_RUN | — | B-001 |
| S-036 | F-034 | driver | Saved rates change live quotes (cross-surface, J-01) | S-035 | NOT_RUN | — | B-001 |
| S-037 | F-035 | driver | Detour help text visible and accurate | — | NOT_RUN | — | B-001 |
| S-038 | F-036 | driver | Static cards display-only (no dead tappable affordance) | — | NOT_RUN | — | B-001 |
| S-039 | F-037, F-039 | driver | Bottom nav all tabs; typed input survives tab switch (state restore) | — | NOT_RUN | — | B-001 |
| S-040 | F-039, F-002, F-012 | driver | System Allow → pending start/GPS action proceeds | — | NOT_RUN | — | B-001 |
| S-041 | F-040, F-018 | driver | System Deny → inline message, no trip, manual path works | — | NOT_RUN | — | B-001 |
| S-042 | F-041, F-019 | driver | Notification tap behavior [CONFIRM — untraced in source] | S-002 | NOT_RUN | — | B-001 |
| S-043 | F-002, F-012 | driver | Start failure surfacing [CONFIRM — no toast/snackbar in source; what does the user see?] | — | NOT_RUN | — | B-001 |
| S-044 | F-001 | driver | Platform chip tap while tracking [CONFIRM — guard untraced; expect no state corruption] | S-002 | NOT_RUN | — | B-001 |
| S-045 | Recovery | driver | Rotation during tracking + modal open; state survives | S-002 | NOT_RUN | — | B-001 |
| S-046 | F-028 | driver | Share failure toast (only if failure inducible; else stays NOT_RUN with note) | S-027 | NOT_RUN | — | B-001 + conditional |
| S-047 | F-013 | driver | Confirm no notes input exists in modal (source: notes always "") | S-005 | NOT_RUN | — | B-001 |
| S-048 | Recovery | driver | Rapid double-tap Start → single trip created | S-002 | NOT_RUN | — | B-001 |
| S-049 | Entry/nav | driver | System back per tab (back-stack vs exit behavior) | — | NOT_RUN | — | B-001 |

Scenario detail records (steps/expected/evidence) are intentionally left as index rows:
the executing session must write per-attempt records at execution time, not in advance.

## Cross-role journeys

| Journey ID / human goal | Record alias | Initiator scenario | Recipient/next role scenarios | Return/receipt scenario | Chain outcome and dependencies |
| --- | --- | --- | --- | --- | --- |
| J-01 / Rate change reaches quote | settings-profile-A | S-035, S-036 | S-012 (quote reflects saved rates) | S-031 (selection persists) | NOT_RUN — needs S-035→S-036→S-012 chain |
| J-02 / Completed run visible everywhere | trip-direct-1 | S-015, S-019 | S-025 (history card), S-022 (shift aggregates) | — | NOT_RUN — needs completed direct trip |
| J-03 / Receipt out of history | trip-direct-1 | S-027 | System chooser (external) | S-028 (content check) | NOT_RUN — external share target must not receive a real send; cancel at chooser |

## Defects and blockers

| ID | Kind / affected scenarios | Role/context and UI reproduction | Expected + basis vs actual | Impact/severity | Evidence | Needed action / retest attempts |
| --- | --- | --- | --- | --- | --- | --- |
| B-001 | BLOCKER / S-001..S-049, J-01..J-03 | driver / any device context: no APK can be built (no JDK/SDK in this environment) and no UI operation capability (device access off) | Expected: executable target + operable UI (skill method boundary). Actual: neither available | P1 for campaign execution (prep unaffected) | `java -version` absent; `t3-code_device_list` → access off; 2026-09-26 | Build APK from `dev` ≥ da73770 on a capable machine; rerun in device-capable session; reset NOTHING (all rows already NOT_RUN) |
| OBS-01 | Observation (not a defect) / S-043 | Source shows `errorMessage` handled in ViewModel but no Toast/Snackbar/Dialog renders it in TrackerScreen | [CONFIRM] on device | Unknown — could hide start failures from drivers | SRC-TRACKER register | Executor must attempt/induce a start failure and record the visible outcome |
| OBS-02 | Observation (not a defect) / S-044 | `selectPlatform` guard while tracking untraced | [CONFIRM] on device | Unknown — possible mid-trip platform switch | SRC-TRACKER register | Tap platform chip mid-tracking; record outcome |
| OBS-03 | Observation (not a defect) / S-047 | `notesText` passed to `onCompleteTrip` but no notes field rendered | [CONFIRM] on device | Low — dead parameter vs hidden field | SRC-MODAL register | Inspect modal fully; record either way |

## Evidence index

No execution evidence yet. Executor: add rows with screenshots around decisive transitions
(quote → start, modal → save, share chooser), failures, and ambiguous states.

| Evidence ID | Actual artifact or observation reference | Timestamp / build / role | Scenario attempts | What is visible and supported | Redactions / limitations |
| --- | --- | --- | --- | --- | --- |
| (none) | — | — | — | — | — |

## Reconciliation log

| Sweep ID / time | Roles, surfaces, sources rechecked and order | Unmapped items / new feature IDs | Scenario or disposition updates | No-new-item pass? / unresolved gaps |
| --- | --- | --- | --- | --- |
| REC-01 / 2026-09-26 | driver; SUR-01..07 in listed order; SRC-* + DOC-PRD | None (source pass) | Ledger created at rev 1 | No — UI sweep impossible (B-001). Executor must run navigation-led + task-led sweeps and log REC-02 |

## Coordinator evidence review

- Reviewer alias / date / reviewed inventory revision and target context: coordinator (prep session) / 2026-09-26 / rev 1 / source @ da73770, no running build.
- Report, source inventory, scenario rows and evidence references inspected: this ledger only; no execution evidence exists to inspect.
- Requested scope, actual roles, candidate dispositions, and status/count reconciliation: whole-app scope kept; 41 features CURRENT, 0 EXCLUDED, 0 UNRESOLVED (source gaps recorded as OBS-01..03 + S-042..S-044 CONFIRM items, not exclusions); counts below recomputed.
- Directly inspected material outcomes, handoffs/failures, and any UI spot-check evidence: none possible (B-001).
- Missing evidence/new candidates returned to tester and resulting status/attempt updates: n/a — no tester dispatched.
- Artifact/browser ownership handoff for follow-up and completion: this directory; owner = coordinator until execution session adopts it.
- Review method: SELF_REVIEW (SAME_AGENT prep; no tester to independently review).
- Review outcome: FOLLOW_UP_REQUIRED; rationale: execution BLOCKED by B-001; ledger structurally complete for handoff.
- Final report limits and engineering evidence links, if applicable: prep artifact only — supplies no pass/fail evidence, no release approval. JVM/CI results are deliberately NOT recorded as scenario evidence (skill method boundary).

## Counts at checkpoint

| Role | Features / fully exercised features | NOT_RUN | PASS | FAIL | BLOCKED | INCONCLUSIVE | NOT_APPLICABLE | E/A | PASS/A |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| driver | 41 / 0 | 49 | 0 | 0 | 0 | 0 | 0 | 0/49 | 0/49 |

T=49, A=49, E=0. (Rows stay NOT_RUN rather than BLOCKED per the template: execution is
future-phase work, and B-001 is recorded once in the blockers table instead of 49 duplicates.
The executor flips affected rows to BLOCKED only if the capability is still missing then.)

## Continuation and cleanup

### Consequential action reconciliation — none (no actions taken)

### Continuation packet

- Effective policy link/revision and DUE/DEFERRED trigger decision: MANUAL prep FINISHED; execution DEFERRED to B-001 resolution.
- Engineering session checkpoint link, when applicable: repo `dev` @ `da73770`; Linear project `OnTheRoad: Direct Booking & Fare Estimator` (no QA comments posted).
- Optional canonical ledger-index path and artifact-check result/limitations: this file; structure follows the skill template (ledger checker not run — no ledger-validation tooling in this harness; noted limitation).
- Current inventory revision and the last completed attempt/sweep: rev 1; REC-01 (source pass, no-new-item UI sweep pending).
- Current tester identity/mode, artifact/session owner, pending authentication request: no tester; owner = coordinator; no auth exists in-app.
- Last observed UI location, role/session alias, and record state: none observed.
- Exact next scenario and next UI action; remaining queue: execution session starts with fresh APK install → S-023/S-024 (zero states) → S-039 (navigation) → S-001 journey order; full queue S-001..S-049 + J-01..J-03.
- Blocking questions, missing fixtures/accounts/tools, and independent work still possible: B-001 (build + device). Nothing else executable from here.
- Changes in build/environment/data that require results to be reopened: n/a (no results yet). If the APK build differs from `da73770`, record the new SHA in Run context before executing.
- Created/modified record aliases, original settings where needed, cleanup through UI: execution must restore theme, profiles, and rates changed during S-030..S-036 (record originals at session start).
- Remaining residue, permitted next action, and owner if known: ledger file only (governance-excluded reports path). Next action: device-capable execution session adopts this ledger.
