import test from "node:test";
import assert from "node:assert/strict";
import {
    validateManifestShape,
    findCredentialBearingUrls,
    isSafeRelativePath,
    runGovernanceCheck
} from "../../scripts/verify-governance.mjs";

test("Governance: repository manifest passes full check", async () => {
    const result = await runGovernanceCheck({ root: process.cwd() });
    assert.equal(result.findings.length, 0, `Expected 0 findings, got: ${JSON.stringify(result.findings)}`);
});

test("Governance: validateManifestShape detects invalid schemaVersion", () => {
    const findings = validateManifestShape({ schemaVersion: 2 });
    const versionError = findings.find(f => f.code === "SCHEMA_VERSION");
    assert.ok(versionError, "Should report SCHEMA_VERSION error");
});

test("Governance: validateManifestShape detects invalid invariant format", () => {
    const findings = validateManifestShape({
        schemaVersion: 1,
        allowedDispositions: ["active"],
        governedDocuments: {},
        phaseGates: Array.from({ length: 10 }, (_, i) => ({
            phase: i + 1,
            owner: "owner",
            startRequires: [],
            completionRequires: []
        })),
        invariants: [
            { id: "bad_format", statement: "Bad ID", ownerPhase: 1 }
        ]
    });
    const invError = findings.find(f => f.code === "INVARIANT_ID");
    assert.ok(invError, "Should report INVARIANT_ID error");
});

test("Governance: isSafeRelativePath validates safe paths", () => {
    assert.equal(isSafeRelativePath("docs/governance/GOVERNANCE.md"), true);
    assert.equal(isSafeRelativePath("../escape/path"), false);
    assert.equal(isSafeRelativePath("/absolute/path"), false);
    assert.equal(isSafeRelativePath(""), false);
});
