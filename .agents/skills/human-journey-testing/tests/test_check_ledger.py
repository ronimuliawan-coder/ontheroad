"""Checks bookkeeping failure modes, never product UI or behavior."""
import copy
import importlib.util
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

SCRIPT = Path(__file__).resolve().parents[1] / "scripts/check_ledger.py"
spec = importlib.util.spec_from_file_location("check_ledger", SCRIPT)
checker = importlib.util.module_from_spec(spec)
spec.loader.exec_module(checker)


def sample():
    return {
        "schema_version": 1, "context_id": "build-a",
        "discovery_reconciled": True, "no_new_items_sweep": True,
        "features": [{"id": "F1", "disposition": "CURRENT", "scenario_ids": ["S1"]}],
        "scenarios": [{"id": "S1", "feature_ids": ["F1"], "role": "operator",
                       "context_id": "build-a", "status": "PASS", "expected": "Record persists",
                       "actual": "Record visible after reopening", "steps": ["Save", "Reopen"],
                       "evidence_ids": ["E1"], "defect_ids": []}],
        "evidence": [{"id": "E1", "kind": "FILE", "context_id": "build-a",
                      "observation": "Reopened record", "path": "observation.txt"}],
        "defects": [],
        "counts": {"NOT_RUN": 0, "PASS": 1, "FAIL": 0, "BLOCKED": 0,
                   "INCONCLUSIVE": 0, "NOT_APPLICABLE": 0,
                   "applicable": 1, "exercised": 1, "passed": 1}}


class LedgerChecks(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(dir=Path(__file__).parent)
        self.root = Path(self.temp.name)
        (self.root / "observation.txt").write_text("Synthetic checker fixture; not product evidence.")

    def tearDown(self):
        self.temp.cleanup()

    def test_valid_and_fail_are_conclusive_but_different_quality(self):
        data = sample()
        self.assertEqual(checker.validate(data, self.root)[0], [])
        self.assertTrue(checker.validate(data, self.root)[2])
        data["scenarios"][0].update(status="FAIL", actual="Record missing", defect_ids=["D1"])
        data["defects"] = [{"id": "D1", "description": "Save did not persist"}]
        data["counts"].update(PASS=0, FAIL=1, passed=0)
        errors, counts, complete = checker.validate(data, self.root)
        self.assertEqual(errors, [])
        self.assertTrue(complete)
        self.assertEqual(counts["passed"], 0)

    def test_rejects_orphans_duplicates_stale_and_unsupported_results(self):
        mutations = [
            lambda d: d["features"].append(copy.deepcopy(d["features"][0])),
            lambda d: d["features"][0].update(scenario_ids=[]),
            lambda d: d["scenarios"][0].update(feature_ids=["missing"]),
            lambda d: d["scenarios"][0].update(evidence_ids=[]),
            lambda d: d["scenarios"][0].update(actual=""),
            lambda d: d["scenarios"][0].update(steps=[]),
            lambda d: d["scenarios"][0].update(status="FAIL"),
            lambda d: d["evidence"][0].update(context_id="old-build"),
            lambda d: d["evidence"][0].update(path="missing.txt"),
            lambda d: d["evidence"][0].update(path="../outside.txt"),
            lambda d: d["counts"].update(PASS=True),
            lambda d: d["scenarios"][0].update(status=[]),
            lambda d: d["features"][0].update(disposition={}),
        ]
        for mutation in mutations:
            with self.subTest(mutation=mutation):
                data = sample()
                mutation(data)
                errors, _, complete = checker.validate(data, self.root)
                self.assertTrue(errors)
                self.assertFalse(complete)

    def test_consistent_blocked_and_unresolved_cannot_be_complete(self):
        data = sample()
        data["scenarios"][0].update(status="BLOCKED", reason="Account unavailable")
        data["counts"].update(PASS=0, BLOCKED=1, exercised=0, passed=0)
        errors, _, complete = checker.validate(data, self.root)
        self.assertEqual(errors, [])
        self.assertFalse(complete)
        data = sample()
        data["features"].append({"id": "F2", "disposition": "UNRESOLVED",
                                 "scenario_ids": [], "reason": "Unknown role visibility"})
        self.assertFalse(checker.validate(data, self.root)[2])

    def test_cli_distinguishes_empty_preparation_and_complete(self):
        empty = SCRIPT.parent.parent / "assets/templates/ledger-index.json"
        result = subprocess.run([sys.executable, str(SCRIPT), str(empty)], capture_output=True)
        self.assertEqual(result.returncode, 0)
        result = subprocess.run([sys.executable, str(SCRIPT), str(empty), "--require-complete"], capture_output=True)
        self.assertEqual(result.returncode, 1)
        self.assertIn(b'"recorded_execution_complete": false', result.stdout)

    def test_cli_rejects_duplicate_keys_and_malformed_input(self):
        path = self.root / "bad.json"
        for content in ['{"counts": {}, "counts": {}}', '{', 'null']:
            path.write_text(content)
            result = subprocess.run([sys.executable, str(SCRIPT), str(path)], capture_output=True)
            self.assertNotEqual(result.returncode, 0)
            self.assertNotIn(b'Traceback', result.stderr)


if __name__ == "__main__":
    unittest.main()
