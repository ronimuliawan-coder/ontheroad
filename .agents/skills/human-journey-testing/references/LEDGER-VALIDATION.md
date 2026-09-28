# Optional artifact-only ledger validation

Use when a campaign is large enough that deterministic bookkeeping checks help. This does not
automate product tests. The checker reads local JSON and checks local evidence-file existence;
it never opens a browser, makes a network call, executes a product operation, or interprets an
image as a pass. Its success means consistent records, not correct product behavior.
The optional checker requires Python 3.9 or newer and uses only its standard library; the
human-testing method itself does not require Python or any product implementation language.

Use [ledger-index.json](../assets/templates/ledger-index.json) as the canonical structured index
if opting in. Keep narrative steps/screenshots in linked artifacts; do not maintain a competing
Markdown scenario/status index. The report derives its counts from this index. Without opting
in, the Markdown ledger and manual reconciliation remain supported and must be disclosed.

The JSON contract is schema_version 1 with context_id, discovery_reconciled, no_new_items_sweep,
features, scenarios, evidence, defects, and counts. IDs are unique within their collection.
Features have id, disposition CURRENT/EXCLUDED/UNRESOLVED, scenario_ids, and a reason for any
non-CURRENT disposition. Scenarios have id, feature_ids, role, context_id, status, expected,
actual, steps, evidence_ids, defect_ids, and reason for outstanding/excluded outcomes. PASS/FAIL
require nonempty expected/actual/steps/evidence; FAIL also requires a known defect. N/A needs
a reason. Both directions of feature/scenario links must agree. Current conclusive scenarios
and their cited evidence must match the index context; retained older attempts remain in a
separate history artifact rather than inflating current coverage.

Evidence records have id, kind FILE/OBSERVATION, context_id, observation, and either a relative
path to an existing file inside the run directory or a nonempty reference to a recorded UI
observation/transcript. URLs are not fetched. The human reviewer must still inspect content,
authenticity, role/context, and whether it supports the claimed result. Source/CI references
cannot become UI evidence simply by putting them in this structure.

Defects need id and description. Counts are exact integer totals for all six statuses plus
applicable, exercised, and passed. Zero applicable scenarios is never reported as 100%.
The supplied empty template is structurally valid preparation, with discovery and execution
incomplete. Add records from actual discovery/observations; never generate results to fill it.

```bash
python3 [skill-path]/scripts/check_ledger.py [run-directory]/ledger-index.json
# Optional: also require reconciled discovery and conclusive execution within that index.
python3 [skill-path]/scripts/check_ledger.py [run-directory]/ledger-index.json --require-complete
```

Exit 0 means bookkeeping is consistent (and complete within the recorded scope only when
--require-complete was supplied). Exit 1 means invalid/inconsistent records or a failed
requested completeness check. Exit 2 means an unreadable/invalid input or command usage.
FAIL scenarios can coexist with complete execution. Checker output always disclaims product
verification; no optional checker result overrides coordinator evidence review.
