# Persona lenses

Use all lenses relevant to the requested scope. Map them to the product's real role names;
do not invent a customer portal, staff panel, or QA account. The assigned tester performs all
lenses sequentially by default. Run ownership follows SKILL.md: one dedicated tester when
supported, otherwise a disclosed same-agent fallback. Model settings follow RUN-POLICY.md.

## Customer / end user

Start as a new or returning person with a goal, using the UI's own language. Discover what
is available, compare/search if supported, make a choice, review it, submit, verify the result,
and find it again later. Include guest vs account differences, correcting mistakes,
cancellation, help, history, preferences, and notifications when the product offers them.

Observe whether the UI explains next steps, preserves work, shows truthful totals/status,
and provides understandable recovery. Keep subjective friction separate from a failed
functional expectation. Do not let knowledge from source code make an unintuitive UI look
usable: describe when the required path was discoverable only from developer information.

## Staff / operator / administrator

Use each applicable actual permission level and workspace. Work from incoming requests to
their resolution: find and inspect the correct item, act on it, correct it where supported,
and check that the next person receives the expected result. Include queues, assignment,
status transitions, bulk actions, search/filter, records/history, reports/exports, and settings.

Ordinary staff and administrators often have different menus and restrictions. Record both
allowed and denied paths, with dedicated test accounts. Check that UI refresh/revisit preserves
changes and that another role sees only the appropriate information. For concurrent work,
operate separate authorized sessions sequentially to create a stale-view case; do not describe
that as load testing or proof of race-condition safety.

## QA tester / developer perspective

Return to the same human UI with a coverage and failure-recovery lens. Reconcile the inventory,
challenge assumptions, try invalid and boundary inputs, cancel midway, revisit a saved record,
test alternate entry points, and verify permissions and handoffs. Test affordances that a
natural happy-path journey skipped: overflow actions, reset filters, modal cancellation,
restoration, empty states, and mobile navigation.

Use keyboard and available assistive tools for actual accessibility observations. Report a
missing screen-reader or touch capability as a gap for those scenarios; keyboard success does
not prove screen-reader or touch support. Do not run scanners or assert accessibility-standard
compliance from this campaign. Record observable responsiveness without inventing benchmark
metrics, backend correctness, code coverage, security assurance, or CI results.

## Cross-role journey record

Choose a domain-appropriate lifecycle and link its scenario IDs. For example, in a generic
request-management product:

1. Requester creates a draft, edits it, and submits it through UI.
2. An operator finds that same request and performs an allowed transition.
3. The requester revisits it and sees the expected status and visible history.
4. A lower-privilege account attempts a known restricted UI entry point and sees the expected
   denial without protected content.

This is illustrative, not a mandatory feature list. Preserve the same record alias and
session identities across the chain. A pass at step 1 does not prove steps 2–4. If delivery or
another prerequisite fails, report that defect and the downstream scenarios it blocks.
