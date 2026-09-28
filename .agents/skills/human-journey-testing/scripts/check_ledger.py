#!/usr/bin/env python3
"""Validate local QA bookkeeping. Never executes or verifies product behavior."""
import argparse
from collections import Counter
import json
from pathlib import Path
import sys

STATUSES = {"NOT_RUN", "PASS", "FAIL", "BLOCKED", "INCONCLUSIVE", "NOT_APPLICABLE"}


def nonblank(value):
    return isinstance(value, str) and bool(value.strip())


def unique_keys(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"duplicate JSON key: {key}")
        result[key] = value
    return result


def validate(data, root):
    errors = []
    def require(condition, message):
        if not condition:
            errors.append(message)
    if not isinstance(data, dict):
        return ["top level must be an object"], {}, False
    require(type(data.get("schema_version")) is int and data["schema_version"] == 1,
            "schema_version must be 1")
    context = data.get("context_id")
    require(nonblank(context), "context_id is required")
    for key in ("discovery_reconciled", "no_new_items_sweep"):
        require(type(data.get(key)) is bool, f"{key} must be a boolean")
    indexes = {}
    for key in ("features", "scenarios", "evidence", "defects"):
        rows = data.get(key)
        require(isinstance(rows, list), f"{key} must be a list")
        index = {}
        for row in rows if isinstance(rows, list) else []:
            if not isinstance(row, dict) or not nonblank(row.get("id")):
                errors.append(f"{key}: each record needs a nonblank id")
                continue
            rid = row["id"]
            require(rid not in index, f"{key}: duplicate id {rid}")
            index[rid] = row
        indexes[key] = index
    features, scenarios = indexes["features"], indexes["scenarios"]
    evidence, defects = indexes["evidence"], indexes["defects"]

    def links(row, key, known, label, nonempty=False):
        values = row.get(key)
        if not isinstance(values, list) or not all(nonblank(v) for v in values):
            errors.append(f"{label}: {key} must be a list of IDs")
            return []
        require(len(values) == len(set(values)), f"{label}: duplicate {key}")
        require(not nonempty or bool(values), f"{label}: empty {key}")
        for value in values:
            require(value in known, f"{label}: unknown {key} {value}")
        return values

    for eid, row in evidence.items():
        require(nonblank(row.get("context_id")), f"{eid}: evidence context required")
        require(nonblank(row.get("observation")), f"{eid}: observation required")
        if row.get("kind") == "FILE":
            path = row.get("path")
            if not nonblank(path):
                errors.append(f"{eid}: relative evidence path required")
                continue
            relative = Path(path)
            target = (root / relative).resolve()
            safe = not relative.is_absolute() and target.is_relative_to(root.resolve())
            require(safe, f"{eid}: evidence path escapes run directory")
            if safe:
                require(target.is_file(), f"{eid}: evidence file missing: {path}")
        elif row.get("kind") == "OBSERVATION":
            require(nonblank(row.get("reference")), f"{eid}: observation reference required")
        else:
            errors.append(f"{eid}: evidence kind must be FILE or OBSERVATION")
    for did, row in defects.items():
        require(nonblank(row.get("description")), f"{did}: defect description required")
    feature_links = {}
    for fid, row in features.items():
        value = row.get("disposition")
        disposition = value if isinstance(value, str) else None
        require(disposition in {"CURRENT", "EXCLUDED", "UNRESOLVED"}, f"{fid}: invalid disposition")
        feature_links[fid] = links(row, "scenario_ids", scenarios, fid, disposition == "CURRENT")
        if disposition != "CURRENT":
            require(nonblank(row.get("reason")), f"{fid}: disposition reason required")
        if disposition == "EXCLUDED":
            require(not feature_links[fid], f"{fid}: excluded feature has scenario links")

    actual_counts = Counter({s: 0 for s in STATUSES})
    for sid, row in scenarios.items():
        value = row.get("status")
        status = value if isinstance(value, str) else None
        require(status in STATUSES, f"{sid}: invalid status")
        if status in STATUSES:
            actual_counts[status] += 1
        require(nonblank(row.get("role")), f"{sid}: role required")
        require(row.get("context_id") == context, f"{sid}: stale/missing scenario context")
        fids = links(row, "feature_ids", features, sid, True)
        eids = links(row, "evidence_ids", evidence, sid, status in {"PASS", "FAIL"})
        links(row, "defect_ids", defects, sid, status == "FAIL")
        for fid in fids:
            if fid in features:
                require(sid in feature_links[fid], f"{sid}/{fid}: missing reverse feature link")
        if status in {"PASS", "FAIL"}:
            require(nonblank(row.get("expected")), f"{sid}: expected result required")
            require(nonblank(row.get("actual")), f"{sid}: actual observation required")
            steps = row.get("steps")
            require(isinstance(steps, list) and bool(steps) and all(nonblank(s) for s in steps),
                    f"{sid}: observed UI steps required")
            for eid in eids:
                if eid in evidence:
                    require(evidence[eid].get("context_id") == context, f"{sid}/{eid}: stale evidence context")
        if status in {"BLOCKED", "INCONCLUSIVE", "NOT_APPLICABLE"}:
            require(nonblank(row.get("reason")), f"{sid}: status reason required")
    for fid, sids in feature_links.items():
        for sid in sids:
            if sid in scenarios:
                fids = scenarios[sid].get("feature_ids", [])
                require(isinstance(fids, list) and fid in fids, f"{fid}/{sid}: missing reverse scenario link")
    actual_counts["applicable"] = len(scenarios) - actual_counts["NOT_APPLICABLE"]
    actual_counts["exercised"] = actual_counts["PASS"] + actual_counts["FAIL"]
    actual_counts["passed"] = actual_counts["PASS"]
    counts = data.get("counts")
    require(isinstance(counts, dict), "counts must be an object")
    if isinstance(counts, dict):
        require(set(counts) == set(actual_counts), "counts keys must match the documented contract")
        for key, count in actual_counts.items():
            require(type(counts.get(key)) is int and counts[key] == count, f"counts.{key}: expected {count}")
    complete = (not errors and data.get("discovery_reconciled") is True
                and data.get("no_new_items_sweep") is True
                and all(f.get("disposition") != "UNRESOLVED" for f in features.values())
                and actual_counts["applicable"] > 0
                and actual_counts["exercised"] == actual_counts["applicable"])
    return errors, dict(actual_counts), complete


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("ledger", type=Path)
    parser.add_argument("--require-complete", action="store_true")
    args = parser.parse_args()
    try:
        data = json.loads(args.ledger.read_text(), object_pairs_hook=unique_keys)
        errors, counts, complete = validate(data, args.ledger.resolve().parent)
    except (OSError, UnicodeError, ValueError, RecursionError) as exc:
        print(f"Input error: {exc}", file=sys.stderr)
        return 2
    for error in errors:
        print(f"ERROR: {error}")
    print(json.dumps({"counts": counts, "recorded_execution_complete": complete}, sort_keys=True))
    print("Artifact consistency only; product behavior and evidence meaning are not verified.")
    if errors or (args.require_complete and not complete):
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
