# Human journey testing — run ledger (OnTheRoad, prep-only)

Adapted from the `human-journey-testing` skill RUN-LEDGER template. No scenario has been
executed: this ledger is the reconciled inventory + `NOT_RUN` queue for a future
device-capable session. Statuses must not be changed without UI observation.

## Run context

| Field | Value |
| --- | --- |
| Run ID / started / timezone | `otr-qa-2026-09-26` / 2026-09-26 / Asia/Jakarta |
| Target URL or app / environment | OnTheRoad native Android app (Kotlin + Compose); no built APK in this session |
| Observed build/version and evidence; unknown if unavailable | APK built 2026-09-26 from `origin/dev` @ `d93388d` (owner-approved tip); app code identical to prep SHA `da73770` (delta is docs/skill only, PR #18). `com.ontheroad.debug` v0.1.0 (1), API 35 emulator. NOT_RUN rows stand — no results existed to reopen |
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

Execution 2026-09-26 finished the queue (48/49 attempted; S-046 conditional NOT_RUN). Expected outcomes cite PRD sections or
source strings; `[ASSUMPTION]` marks inference to confirm on device, `[CONFIRM]` marks
source-uncertain behavior the executor must settle (INCONCLUSIVE if unresolvable, never assumed).

| Scenario ID | Feature IDs | Actual role/account alias | Variant/context | Dependencies | Current status | Latest attempt / evidence | Defect, blocker, or applicability reason |
| --- | --- | --- | --- | --- | --- | --- | --- |
| S-001 | F-001 | driver | Select each of 6 platform chips, idle | — | PASS | PASS 2026-09-26; E-001,E-006,E-007,E-008,E-010,E-011 | — |
| S-002 | F-002 | driver | Start platform trip, permission granted | S-039 | PASS | PASS 2026-09-26; E-085,E-086,E-097 (Grab 20000 saved) | — |
| S-003 | F-003 | driver | Idle placeholders (Ready/--:--/0) | — | PASS | PASS 2026-09-26; E-005 | — |
| S-004 | F-019 | driver | Notification present + live distance while tracking | S-002 | FAIL | FAIL 2026-09-26; E-038,E-039 + SystemUI dump | D-002 |
| S-005 | F-004 | driver | Complete Trip opens modal | S-002 | PASS | PASS 2026-09-26; E-040 (direct), E-088 (platform) | — |
| S-006 | F-005 | driver | Select Direct idle → quote card replaces metrics, no Start Trip button | — | PASS | PASS 2026-09-26; E-011 | — |
| S-007 | F-006 | driver | Pickup typing (2+ chars) → suggestions → select fills field | — | PASS | PASS 2026-09-26; E-016,E-017 | — |
| S-008 | F-006 | driver | GPS snap button → address filled (or fallback hint if denied/unavailable) | S-039 or S-040 | PASS | PASS 2026-09-26; E-019,E-020,E-021 | — |
| S-009 | F-007 | driver | Destination entry → top-match auto distance | S-007 | PASS | PASS 2026-09-26; E-027,E-028 | — |
| S-010 | F-007 | driver | Destination no-match → fallback hint, manual entry usable | — | PASS | PASS 2026-09-26; E-084 | — |
| S-011 | F-008 | driver | Manual distance edit → fare recalculates, caption switches to manual | S-009 | PASS | PASS 2026-09-26; E-029,E-030 (no separate manual caption shown) | — |
| S-012 | F-009 | driver | Quote matches `Max(Min, Base + max(0, km-baseKm)×rate)` for active profile (PRD §4.3) | S-009 | PASS | PASS 2026-09-26; E-027,E-028,E-030 (formula holds at 3.9/5/8.6 km) | — |
| S-013 | F-010 | driver | Switch profile chip → rates/fare update (needs 2 profiles; create via S-032) | S-032 | BLOCKED | — | D-006 (form shows only Default; E-067) |
| S-014 | F-011 | driver | Custom-fare toggle → override field; displayed fare follows override | S-009 | FAIL | FAIL 2026-09-26; E-031..E-037 | D-001 |
| S-015 | F-012 | driver | Start Direct Run → trip persists quote, tracking starts | S-009 | PASS | PASS 2026-09-26; E-038 | — |
| S-016 | F-013 | driver | Modal prefills quoted fare + distance (PRD §4.5, FR-DIR-06) | S-015 | PASS | PASS 2026-09-26; E-040 | — |
| S-017 | F-014 | driver | Cash toggle reveals cash field; off hides it | S-015 | PASS | PASS 2026-09-26; E-042,E-045 | — |
| S-018 | F-015 | driver | Invalid paid total → Save disabled + `direct_customer_total_invalid` | S-015 | PASS | PASS 2026-09-26; E-044 (wording differs from prep string id) | — |
| S-019 | F-016 | driver | Valid completion → trip saved, modal closes, service stops | S-015 | PASS | PASS 2026-09-26; E-050,E-051 | — |
| S-020 | F-017 | driver | Dismiss modal (swipe/back) → trip still active, quote intact on reopen | S-015 | PASS | PASS 2026-09-26; E-046,E-047 | — |
| S-021 | F-018 | driver | Permission denied → inline message, manual quoting works, no trip created | S-040 | PASS | PASS 2026-09-26; E-114,E-115 (manual fare 24,000 exact) | — |
| S-022 | F-020 | driver | Shift aggregates reflect completed trips | S-019 | FAIL | FAIL 2026-09-26; E-052,E-053,E-098 | D-004 |
| S-023 | F-021 | driver | Fresh install: zeros, no crash | — | PASS | PASS 2026-09-26; E-002 | — |
| S-024 | F-022 | driver | Empty history text | S-023 context | PASS | PASS 2026-09-26; E-003 | — |
| S-025 | F-023 | driver | Card fields (platform, earnings, route, rate, badge iff quoted) | S-019 | PASS | PASS 2026-09-26; E-051,E-097 ($ = en-US locale rule, OBS-04) | — |
| S-026 | F-024 | driver | Each sort order reorders list (needs ≥3 varied trips) | S-019 ×3 | PASS | PASS 2026-09-26; E-107,E-108,E-109 | — |
| S-027 | F-025 | driver | Share eligible direct trip → system chooser appears, PNG created | S-019 (direct) | PASS | PASS 2026-09-26; E-110 (chooser + PNG, cancelled, no send) | — |
| S-028 | F-027 | driver | Receipt shows date, endpoints, actual distance, paid total; no coords/notes/earnings | S-027 | PASS | PASS 2026-09-26; E-110 (content read from preview) | — |
| S-029 | F-026 | driver | Platform + incomplete trips show no share icon | S-002, S-015 | PASS | PASS 2026-09-26; E-097 | — |
| S-030 | F-029 | driver | Each theme applies app-wide immediately | — | FAIL | FAIL 2026-09-26; E-054..E-058 (restart required) | D-005 |
| S-031 | F-030 | driver | Select profile → persists across Settings revisit + booking form | — | FAIL | FAIL 2026-09-26; E-066,E-067 | D-006 |
| S-032 | F-031 | driver | Create profile (valid + max-length boundary) → appears in list | — | PASS | PASS 2026-09-26; E-059,E-060,E-063,E-065 (101-char accepted) | — |
| S-033 | F-031 | driver | Duplicate/blank name → inline error, confirm disabled | — | PASS | PASS 2026-09-26; E-068 (dup), E-059 (blank) | — |
| S-034 | F-032 | driver | Delete with confirm → removed; single-profile delete disabled | S-032 | PASS | PASS 2026-09-26; E-071,E-072,E-073 | — |
| S-035 | F-033 | driver | Invalid rate input → error outline + message, Save disabled until valid+changed | — | PASS | PASS 2026-09-26; E-074,E-076,E-077 | — |
| S-036 | F-034 | driver | Saved rates change live quotes (cross-surface, J-01) | S-035 | FAIL | FAIL 2026-09-26; E-078,E-080,E-081,E-082 | D-007 |
| S-037 | F-035 | driver | Detour help text visible and accurate | — | PASS | PASS 2026-09-26; E-074 | — |
| S-038 | F-036 | driver | Static cards display-only (no dead tappable affordance) | — | PASS | PASS 2026-09-26; E-075 | — |
| S-039 | F-037, F-039 | driver | Bottom nav all tabs; typed input survives tab switch (state restore) | — | PASS | PASS 2026-09-26; E-018,E-019 | — |
| S-040 | F-039, F-002, F-012 | driver | System Allow → pending start/GPS action proceeds | — | PASS | PASS 2026-09-26; E-019,E-020,E-021 | — |
| S-041 | F-040, F-018 | driver | System Deny → inline message, no trip, manual path works | — | PASS | PASS 2026-09-26; E-112,E-114 | — |
| S-042 | F-041, F-019 | driver | Notification tap behavior [CONFIRM — untraced in source] | S-002 | BLOCKED | — | D-002 (no notification to tap) |
| S-043 | F-002, F-012 | driver | Start failure surfacing [CONFIRM — no toast/snackbar in source; what does the user see?] | — | INCONCLUSIVE | Attempted 2026-09-26; E-118,E-120 (offline start succeeds) | Failure not inducible via UI |
| S-044 | F-001 | driver | Platform chip tap while tracking [CONFIRM — guard untraced; expect no state corruption] | S-002 | PASS | PASS 2026-09-26; E-087 | — |
| S-045 | Recovery | driver | Rotation during tracking + modal open; state survives | S-002 | NOT_APPLICABLE | — | Portrait-locked; rotation no-op (E-089) |
| S-046 | F-028 | driver | Share failure toast (only if failure inducible; else stays NOT_RUN with note) | S-027 | NOT_RUN | — | Conditional; failure not inducible |
| S-047 | F-013 | driver | Confirm no notes input exists in modal (source: notes always "") | S-005 | PASS | PASS 2026-09-26; E-040,E-041 | — |
| S-048 | Recovery | driver | Rapid double-tap Start → single trip created | S-002 | PASS | PASS 2026-09-26; E-086,E-097 | — |
| S-049 | Entry/nav | driver | System back per tab (back-stack vs exit behavior) | — | PASS | PASS 2026-09-26; E-122,E-123 | — |

Scenario detail records (steps/expected/evidence) are intentionally left as index rows:
the executing session must write per-attempt records at execution time, not in advance.

## Cross-role journeys

| Journey ID / human goal | Record alias | Initiator scenario | Recipient/next role scenarios | Return/receipt scenario | Chain outcome and dependencies |
| --- | --- | --- | --- | --- | --- |
| J-01 / Rate change reaches quote | settings-profile-A | S-035, S-036 | S-012 (quote reflects saved rates) | S-031 (selection persists) | FAIL (D-007) — save persists but quote updates only after restart (E-078..E-082) |
| J-02 / Completed run visible everywhere | trip-direct-1 | S-015, S-019 | S-025 (history card), S-022 (shift aggregates) | — | FAIL (D-004) — history card correct (S-025 PASS), shift aggregates stay zero (S-022 FAIL) |
| J-03 / Receipt out of history | trip-direct-1 | S-027 | System chooser (external) | S-028 (content check) | PASS — chooser appeared with PNG preview, cancelled with no send (E-110, E-111); receipt content verified (S-028) |

## Defects and blockers

| ID | Kind / affected scenarios | Role/context and UI reproduction | Expected + basis vs actual | Impact/severity | Evidence | Needed action / retest attempts |
| --- | --- | --- | --- | --- | --- | --- |
| B-001 | BLOCKER, RESOLVED | driver / devbox 2026-09-26: JDK 21 + SDK + emulator available; APK built from `origin/dev` @ `d93388d` (app code identical to prep SHA `da73770`) | Prep expectation met | — (prep only) | Run context + build log | No action; retained for history |
| OBS-01 | Observation, OPEN / S-043 | Source shows `errorMessage` handled in ViewModel but no Toast/Snackbar/Dialog renders it in TrackerScreen | [CONFIRM] attempted: start succeeds in airplane mode and fully offline; no error UI seen in any flow | Unknown — could hide start failures from drivers | E-118, E-120 | Failure surfacing stays unconfirmed; S-043 INCONCLUSIVE |
| OBS-02 | Observation, RESOLVED / S-044 | `selectPlatform` guard while tracking untraced | Guard exists: chip taps ignored while tracking, Grab retained (E-087) | None | E-087 | None |
| OBS-03 | Observation, RESOLVED / S-047 | `notesText` passed to `onCompleteTrip` but no notes field rendered | Confirmed: full modal inventoried, no notes field (E-040, E-041) | Low — dead parameter | E-040, E-041 | None |
| D-001 | DEFECT / S-014 | driver / Direct quote, custom fare entered | Expected: toggling back to auto restores auto fare. Actual: stale custom value keeps driving the quote (Rp 30,000 vs auto 27,500), persisting across distance edits | Medium — wrong quote persisted into trips | E-031..E-037 | Fix toggle-off to clear/recompute; retest S-014 round-trip |
| D-002 | DEFECT / S-004, S-042 | driver / tracking direct trip | Expected: foreground-service notification with live distance. Actual: no app notification in shade (SystemUI dump confirms); GPS active, timer counts | Medium — background-awareness + tap-to-return missing | E-038, E-039 | Post notification; retest S-004/S-042 |
| D-004 | DEFECT / S-022, J-02 | driver / Shift after 1-2 completed trips (direct + platform) | Expected: aggregates reflect trips. Actual: all zeros, revisit-proof | High — shift reconciliation (screen headline purpose) never updates | E-052, E-053, E-098 | Fix aggregation; retest S-022 + J-02 |
| D-005 | DEFECT / S-030 | driver / Settings theme buttons | Expected: theme applies app-wide immediately. Actual: applies only after force-stop + relaunch; no restart prompt | Low–medium | E-054..E-058 | Apply live or prompt restart; retest S-030 |
| D-006 | DEFECT / S-031, S-013 | driver / Settings vs Tracker booking form | Expected: selected profile persists to booking form. Actual: Settings keeps Night; form still shows Default; form offers no switch | Medium — multi-profile quoting broken cross-surface | E-066, E-067 | Fix propagation; retest S-031/S-013 |
| D-007 | DEFECT / S-036, J-01 | driver / Settings rates vs Tracker quote | Expected: saved rates change live quotes. Actual: quote uses old rates until restart (27,500 stale vs 29,500) | Medium — J-01 journey broken live | E-078..E-082 | Make quote observe settings; retest S-036/J-01 |
| OBS-04 | Observation / S-025, S-028 | driver / en-US emulator locale | History/Shift render `$` per the Currency Format card's locale rule while quote/config surfaces hardcode `Rp`; values exact | Low — cross-surface symbol inconsistency | E-051, E-075, device locale en-US | Design decision; not failed |
| OBS-05 | Observation | build identity | APK versionName 0.1.0 vs in-app `OnTheRoad 0.2.0` card | Low | aapt dump, E-081 context | Align versions |
| OBS-06 | Observation / platform modal | driver / platform completion modal | Typed payout/quoted values are discarded on modal dismiss/reopen (direct computed quote survives, S-020) | Low–medium (re-entry burden) | E-091 | Retain draft or confirm discard |
| OBS-07 | Observation / S-041 follow-up | driver / denied permission | After denial, GPS snap is a silent no-op (no re-request, no settings guidance) | Low | E-116 | Guide to settings or re-request |
| OBS-08 | Observation / history residue | driver / History | Trips 4/5 earnings ($500061.00/$800063.00) are tester keyboard-focus artifacts; verified-typed trip 6 saved exactly $9000.00. No edit/delete path exists | Residue stands (test device) | E-124..E-128 | Uninstall wipes local trips when device is recycled |

## Evidence index

No execution evidence yet. Executor: add rows with screenshots around decisive transitions
(quote → start, modal → save, share chooser), failures, and ambiguous states.

| Evidence ID | Actual artifact or observation reference | Timestamp / build / role | Scenario attempts | What is visible and supported | Redactions / limitations |
| --- | --- | --- | --- | --- | --- |
| E-001..E-129 | `evidence/E-*.png` (129 screenshots, full 1440x3120 `adb screencap`) | 2026-09-26 / `com.ontheroad.debug` from `origin/dev@d93388d` / driver | All attempted scenarios (see per-row refs; ledger-index.json maps every scenario to evidence IDs) | Decisive transitions, failures, ambiguous states, zero states, chooser, permission dialog, receipt preview | Street addresses visible in screenshots (redact if shared externally); no credentials exist |

## Reconciliation log

| Sweep ID / time | Roles, surfaces, sources rechecked and order | Unmapped items / new feature IDs | Scenario or disposition updates | No-new-item pass? / unresolved gaps |
| --- | --- | --- | --- | --- |
| REC-01 / 2026-09-26 | driver; SUR-01..07 in listed order; SRC-* + DOC-PRD | None (source pass) | Ledger created at rev 1 | No — UI sweep impossible (B-001). Executor must run navigation-led + task-led sweeps and log REC-02 |
| REC-02 / 2026-09-26 | driver; reverse order History → Settings → Shift → Tracker + task-led revisit of quote/modal/share/permission flows | None — no new items. Known untested gap (not new): permanent-denial ('don't ask again') path; S-046 conditional failure path | 38 PASS / 6 FAIL / 2 BLOCKED / 1 INCONCLUSIVE / 1 N/A / 1 NOT_RUN | Yes — no-new-item pass within discoverable scope |

## Coordinator evidence review

- Reviewer alias / date / reviewed inventory revision and target context: executor (SAME_AGENT execution session, Muse Spark) / 2026-09-26 / rev 1 / APK from `origin/dev@d93388d` on emulator (Android 15, qa-pixel AVD).
- Report, source inventory, scenario rows and evidence references inspected: this ledger + ledger-index.json (checker-clean) + 129 evidence screenshots, all present on disk.
- Requested scope, actual roles, candidate dispositions, and status/count reconciliation: whole-app scope kept; 41 features CURRENT, 0 EXCLUDED, 0 UNRESOLVED; counts recomputed and checker-validated (44/48 exercised, 38/48 passed).
- Directly inspected material outcomes, handoffs/failures, and any UI spot-check evidence: every FAIL/INCONCLUSIVE/BLOCKED row inspected against its screenshots (defects D-001, D-002, D-004..D-007 reproduced/observed firsthand).
- Missing evidence/new candidates returned to tester and resulting status/attempt updates: n/a — SAME_AGENT; REC-02 found no new items.
- Artifact/browser ownership handoff for follow-up and completion: this directory; owner = executor; transport branch `qa-ledger` never merged.
- Review method: SELF_REVIEW (SAME_AGENT execution; requested tester model unavailable in this harness — disclosed).
- Review outcome: FOLLOW_UP_REQUIRED; rationale: 6 defects (D-001, D-002, D-004..D-007) need fixes + retest; S-046 conditional and permanent-denial path untested.
- Final report limits and engineering evidence links, if applicable: acceptance evidence only — no release approval. JVM/CI results deliberately NOT recorded as scenario evidence (skill method boundary). Emulator evidence is SIMULATED/EMULATED context, not physical-device proof.

## Counts at checkpoint

| Role | Features / fully exercised features | NOT_RUN | PASS | FAIL | BLOCKED | INCONCLUSIVE | NOT_APPLICABLE | E/A | PASS/A |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| driver | 41 / 38 | 1 | 38 | 6 | 2 | 1 | 1 | 44/48 | 38/48 |

T=49, A=48 (S-045 NOT_APPLICABLE), E=44. Fully exercised features: 38/41 (D-002 blocks F-019/F-041 proof; D-004 blocks F-020; D-005 blocks F-029; D-006 blocks F-010/F-030; D-007 blocks F-034; D-001 blocks F-011).

## Continuation and cleanup

### Consequential action reconciliation — 6 test trips created through UI (2 direct: 27500/39984.28; 4 platform: 20000/500061*/800063*/9000; * = tester keyboard-focus artifacts, OBS-08); 3 test rate profiles created then deleted via UI; rates changed then restored; theme changed then restored; location permission revoked/granted via dialog + `pm` (OS-level fixture); share sheet invoked twice, cancelled both times with no send. No production, no real sends, no account actions.

### Continuation packet

- Effective policy link/revision and DUE/DEFERRED trigger decision: MANUAL execution FINISHED 2026-09-26 (owner-authorized side effects + owner-approved build revision `origin/dev`).
- Engineering session checkpoint link, when applicable: APK source `origin/dev` @ `d93388d` (app code identical to prep SHA `da73770`); Linear project `OnTheRoad: Direct Booking & Fare Estimator` (no QA comments posted).
- Optional canonical ledger-index path and artifact-check result/limitations: `ledger-index.json` in this directory; `check_ledger.py` PASS (0 errors; recorded_execution_complete=false — correct with NOT_RUN/BLOCKED/INCONCLUSIVE rows). Checker validates bookkeeping only, not product behavior.
- Current inventory revision and the last completed attempt/sweep: rev 1; REC-02 (reverse-order UI sweep, no-new-item pass).
- Current tester identity/mode, artifact/session owner, pending authentication request: SAME_AGENT executor (requested gpt-5.6-luna/xhigh unavailable — disclosed); owner = executor; no auth exists in-app.
- Last observed UI location, role/session alias, and record state: Settings tab (restored values), driver, 6 local trips in history.
- Exact next scenario and next UI action; remaining queue: none queued, campaign complete. Retest queue after fixes: S-014 (D-001), S-004/S-042 (D-002), S-022 + J-02 (D-004), S-030 (D-005), S-031/S-013 (D-006), S-036 + J-01 (D-007); still-open probes: S-043, S-046, permanent-denial path.
- Blocking questions, missing fixtures/accounts/tools, and independent work still possible: none — campaign executed.
- Changes in build/environment/data that require results to be reopened: results bound to `d93388d` build; any new build reopens affected rows per evidence-reuse rule.
- Created/modified record aliases, original settings where needed, cleanup through UI: theme restored to System (E-058/E-129), profiles restored to Default-only (E-073), rates restored to 10000/3500/15000/0.0/1.25 (E-083/E-129), permission re-granted (E-117), network/wifi restored, rotation setting restored to auto. Residue: 6 local trips (uninstall wipes them); emulator + AVD `qa-pixel` left running for inspection.
- Remaining residue, permitted next action, and owner if known: ledger files + 129 evidence screenshots (governance-excluded reports path, uncommitted). Next action: owner reviews report; fixes retested through the same UI scenarios on a new build. Never merge `qa-ledger`.
