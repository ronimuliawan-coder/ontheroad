# Road Test Guide — OnTheRoad v0.2.0 (post-promotion)

Real-world acceptance drive on `main` after the Direct Booking promotion. The emulator
campaign (`otr-qa-2026-09-26`, closed 46/48) proved the flows; this drive proves them
against real GPS, real movement, and real cockpit conditions.

## Part A — Build the APK on the devbox, install on your phone

Your local machine has no JDK/SDK, so build where the toolchain already works.

1. Resume the `android-qa` devbox. Open a terminal (any session — no agent needed).
2. Fetch and verify the release line:
   ```bash
   git fetch origin
   git checkout main
   git pull --ff-only
   git rev-parse HEAD   # expect dcc4799 (promotion merge); record the SHA below
   ```
3. Build:
   ```bash
   ./gradlew assembleDebug
   ```
   Expect `BUILD SUCCESSFUL`. The APK lands at
   `app/build/outputs/apk/debug/app-debug.apk`.
4. Transfer the APK to your phone using any **one** path:
   - **VS Code web**: right-click `app-debug.apk` → Download, then copy to the phone
     over USB, Bluetooth, or a cloud drive; or
   - **Your own computer**: `adb install -r app-debug.apk` with the phone on USB
     debugging (needs local platform-tools only, no JDK).
5. On the phone: allow **Install unknown apps** for the installer source (one-time),
   install, then **uninstall any older OnTheRoad test build first** if present, so
   this drive starts from zero state.
6. Record here: build SHA `________`, install date `________`.

> Do not sign, tag, or publish this APK anywhere. It is an unsigned debug build for
> your own device only. Release signing (`release.yml`) runs in CI on a `v*` tag,
> which comes after this drive, not before.

## Part B — Before you drive

1. Fresh install confirmed (no old trips in History, Shift all zeros).
2. Record baseline settings (photo them): theme mode, active profile, all five
   rates, detour factor.
3. Grant location when asked (keep the notification prompt answer as-is the first
   time; you will exercise denial paths only if you choose the optional item 9).
4. Mount the phone as usual; note whether it is car or motorcycle (vibration and
   GPS quality differ).

## Part C — Drive checklist

Do these in order. For each trip, capture: quoted fare/distance (screenshot),
completion modal values (screenshot), History card, and Shift aggregates at day end.

1. **Platform trip with a real detour.** Start a Grab/Gojek trip, deliberately take
   a detour (or hit traffic that forces one). Expect: live metrics count up,
   DiscrepancyBadge goes amber/red, completion records actual vs quoted.
2. **Full direct quote flow.** Pickup (GPS snap) → destination (typed + suggestion)
   → auto distance → fare → Start Direct Run → drive → Complete with actual payment.
   Expect: modal prefills quote fare and distance; paid-total validation gates Save.
3. **Destination-edit re-quote (review fix, unverified on device).** Pick a
   destination, get a quote, then edit the destination text to something else.
   Expect: coordinates reset, auto-caption drops, distance text kept editable, and
   the new suggestion re-quotes. Start only when the visible distance looks right.
4. **Fare toggle after platform switch (review fix, unverified on device).** Set a
   custom fare, switch platform chip away and back. Expect: override field visible
   again with your value (not silently driving the quote while hidden). Then toggle
   back to auto and confirm the auto fare returns.
5. **Settings round-trip.** Change Rate/Km, return to Tracker, confirm the live
   quote updates with no restart. Switch profile, confirm the form follows.
6. **Detour factor restart read (review fix, unverified on device).** Set detour to
   `1.3`, force-stop the app, reopen Settings. Expect: exactly `1.3`, not
   `1.29999995`.
7. **Theme.** Cycle System/Dark/Light. Expect: immediate application, no restart.
8. **Notification.** While tracking, pull the shade. Expect: `OnTheRoad: Active Run`
   with live km; tapping returns to the app.
9. **Optional — denial path.** Only on a spare install: deny location, confirm the
   red inline message, no trip starts, manual quoting still works. (Skip if you do
   not want to revoke your grant mid-drive.)
10. **End of day.** Open Shift: totals must reflect every completed trip. Open
    History: every card shows platform, earnings, route, rate, and badge where a
    quote existed. Share one direct-trip receipt to yourself; verify amount,
    addresses, distance, date — and that coordinates/notes/earnings are absent.

## Part D — Report back

For each numbered item: PASS/FAIL plus what you saw. For any FAIL: steps to
reproduce, expected vs actual, screenshots, and whether it blocks the `v0.2.0`
tag in your judgment. Also report: device model, Android version, car/motorcycle,
approximate total km driven, and any GPS-quality notes (urban canyon, tunnels).

Decide at the end: keep the test trips in History or uninstall to wipe them.
