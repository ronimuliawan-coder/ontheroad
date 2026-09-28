# Accounts, application roles, and UI sessions

Read for authenticated or multi-role campaigns. This procedure applies to any identity
provider. Existing Google/OAuth sign-in is one possible prerequisite, not proof of an
application role or tenant/workspace membership.

## Build the role/session map

Reuse the owner's supplied mappings and existing signed-in sessions. Record for each actual
role: intended account alias, application workspace, browser/profile/session handle, observed
account/role evidence, authentication state, verification time, and assigned tester. Never
record passwords, MFA codes, tokens, cookies, or identity-provider storage.

Prefer already authenticated separate profiles or genuinely isolated sessions. Different tabs
may share cookies; record actual isolation or treat them as shared. Do not assume a new
subagent inherits browser access or authentication. Inspect the application UI's account,
workspace, role indicators, and relevant permissions before choosing role-specific scenarios.
If the UI cannot establish the intended role sufficiently, record ROLE_UNVERIFIED; ask for
the missing mapping/evidence and block role-specific conclusions until resolved. Test safe
role-independent behavior meanwhile. Do not use database/claims inspection to bypass this.

Map each access problem to a specific blocker category; these refine BLOCKED, not new scenario
statuses:

| Category | Evidence | Next action |
| --- | --- | --- |
| SIGN_IN_REQUIRED | App/provider displays sign-in, expired-session, MFA, or reauthentication UI. | Use an available authorized account through UI; request user sign-in/MFA when interaction is needed. |
| WRONG_ACCOUNT | Visible application identity differs from the assigned account. | Select the intended authorized account through the supported UI and reverify the app role. |
| MISSING_PERMISSION | Correct intended account/workspace is verified but the UI denies required role access. | Report denied access; request the owner resolve permissions. Do not grant roles yourself. |
| ACCOUNT_UNAVAILABLE | No usable designated account/session has been supplied or can be selected. | Ask for an account/session reference; do not conclude that no such account exists globally. |
| ROLE_UNVERIFIED | Authentication succeeds but intended application role cannot be established. | Record known identity/context and request role evidence or clarification. |
| SESSION_TOOL_UNAVAILABLE | Required logged-in session cannot be controlled with available UI tools. | Arrange a supported handoff or same-agent fallback; otherwise preserve the blocker. |

If a scenario is specifically testing access denial, expected denial can PASS. If the verified
account should have access under a supported requirement and does not, record that access
defect as FAIL and block downstream work. Ordinary inability to authenticate is not evidence
that an application lacks a role, nor automatically a product defect.

## Sequential switching when isolation is unavailable

Testing with the owner's supplied roles authorizes routine application account selection
within the run; no extra approval is needed for every switch. First checkpoint pending work,
record aliases, and confirm a usable sign-in path for the next role. Prefer the application's
account/workspace switcher. Otherwise sign out of the **application**, then use its normal
sign-in/account-selection UI. Do not sign out of Google or another identity provider, clear
browser data, revoke sessions, or change credentials merely to force a different identity.

Before submitting a role-specific action, confirm the actual application identity, workspace,
and role again; single sign-on may return to the previous account. If the intended account
cannot be selected, preserve the visible state and request assistance. Do not repeatedly
retry authentication or lock out accounts. Document that sequential switching cannot prove
simultaneous-session behavior; keep any required concurrent-session scenarios blocked.

Cross-role handoffs may run sequentially on the same UI-visible record. Return to the
initiator account through UI to verify its final result. Keep history bound to the session
and role that actually performed each action.

## User interaction and continuation

Ask precisely for the missing action, for example: “Store-manager scenarios are blocked at
Google sign-in for account alias manager-test. Complete sign-in in session B; staff testing
can continue.” Use the actual provider and problem observed. Never ask the user to paste
passwords/MFA codes into reports or chats. Do not bypass challenges or create/upgrade accounts.

Record the pending request once, affected scenario IDs, independent work, and the next UI
verification after the user completes it. If no independent work remains, checkpoint and wait
for the required input rather than claiming completion. On resumption or unexpected access
changes, reverify the application role/workspace and reopen affected results if context changed.
Leave existing user sessions signed in unless logout is itself an authorized test step or the
owner requests cleanup; clean test records without blanket identity-provider logout.
