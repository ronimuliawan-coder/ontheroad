#!/usr/bin/env node

/**
 * Validates the repository-owned governance contract without installing
 * dependencies or contacting external services.
 *
 * Keep this script Node-compatible with zero third-party dependencies.
 * Its CI job runs for documentation-only and instruction changes and must stay
 * fast (<500ms), cheap, and secret-free.
 */

import { readFile, readdir, realpath, stat } from "node:fs/promises";
import path from "node:path";
import { pathToFileURL } from "node:url";

const DEFAULT_MANIFEST = "docs/governance/governance.json";
const REQUIRED_DOCUMENT_FIELDS = [
    "path",
    "kind",
    "disposition",
    "ownerPhase",
    "authority",
    "reason",
];
const REQUIRED_ENVIRONMENT_FIELDS = [
    "name",
    "visibility",
    "environments",
    "status",
    "consumers",
    "owner",
    "required",
    "rotationTrigger",
    "retirementPhase",
];
const ENVIRONMENT_VISIBILITIES = new Set(["public", "server", "ci", "test", "local-only"]);
const ENVIRONMENT_STATUSES = new Set(["current", "planned"]);
const ENVIRONMENT_REQUIREMENTS = new Set(["required", "optional", "conditional", "generated", "phase-owned"]);
const CREDENTIAL_URL_PATTERNS = [
    /https?:\/\/[^\s/:]+:[^\s/@]+@/i,
    /https?:\/\/oauth2:[^\s@]+@/i,
    /https?:\/\/[^\s@]*(?:token|password|secret)[^\s@]*@/i,
];
const ENVIRONMENT_PATTERNS = [
    /process\.env\.([A-Z][A-Z0-9_]*)/g,
    /process\.env\[['"]([A-Z][A-Z0-9_]*)['"]\]/g,
    /System\.getenv\(['"]([A-Z][A-Z0-9_]*)['"]\)/g,
    /\bsecrets\.([A-Z][A-Z0-9_]*)/g,
    /\bvars\.([A-Z][A-Z0-9_]*)/g,
];

function finding(code, filePath, message) {
    return { code, path: filePath, message };
}

export function toPosixPath(value) {
    return value.split(path.sep).join("/").replace(/^\.\//, "");
}

export function compareCodePoints(left, right) {
    return left < right ? -1 : left > right ? 1 : 0;
}

export function isSafeRelativePath(value) {
    if (typeof value !== "string" || value.length === 0 || path.isAbsolute(value)) return false;
    const normalized = path.posix.normalize(value.replaceAll("\\", "/"));
    return normalized === value && normalized !== ".." && !normalized.startsWith("../");
}

function isExcluded(relativePath, excludedRoots) {
    return excludedRoots.some(
        (excluded) => relativePath === excluded || relativePath.startsWith(`${excluded}/`),
    );
}

async function walkFiles(root, relativeRoot, excludedRoots, output) {
    const absoluteRoot = path.join(root, relativeRoot);
    let entries;

    try {
        entries = await readdir(absoluteRoot, { withFileTypes: true });
    } catch (error) {
        if (error && typeof error === "object" && "code" in error && error.code === "ENOENT") return;
        throw error;
    }

    entries.sort((left, right) => compareCodePoints(left.name, right.name));

    for (const entry of entries) {
        const relative = toPosixPath(path.posix.join(relativeRoot, entry.name)).replace(/^\.\//, "");
        if (isExcluded(relative, excludedRoots)) continue;
        if (entry.isSymbolicLink()) continue;
        if (entry.isDirectory()) {
            await walkFiles(root, relative, excludedRoots, output);
        } else if (entry.isFile()) {
            output.add(relative);
        }
    }
}

export async function discoverAllFiles(root, excludedRoots = []) {
    const output = new Set();
    await walkFiles(root, ".", excludedRoots, output);
    return [...output].sort(compareCodePoints);
}

export async function discoverGovernedDocuments(root, config) {
    const excludedRoots = config?.excludedRoots ?? [];
    const extensions = new Set(config?.extensions ?? []);
    const roots = config?.roots ?? ["."];
    const discovered = new Set();

    for (const configuredRoot of roots) {
        const candidates = new Set();
        await walkFiles(root, configuredRoot, excludedRoots, candidates);
        for (const candidate of candidates) {
            if (extensions.has(path.posix.extname(candidate))) discovered.add(candidate);
        }
    }

    for (const exactFile of config?.exactFiles ?? []) {
        try {
            const fileStat = await stat(path.join(root, exactFile));
            if (fileStat.isFile()) discovered.add(toPosixPath(exactFile));
        } catch (error) {
            if (!(error && typeof error === "object" && "code" in error && error.code === "ENOENT")) {
                throw error;
            }
        }
    }

    return [...discovered].sort(compareCodePoints);
}

export function validateManifestShape(manifest) {
    const findings = [];
    if (!manifest || typeof manifest !== "object" || Array.isArray(manifest)) {
        return [finding("MANIFEST_TYPE", DEFAULT_MANIFEST, "manifest must be a JSON object")];
    }

    if (manifest.schemaVersion !== 1) {
        findings.push(finding("SCHEMA_VERSION", DEFAULT_MANIFEST, "schemaVersion must be 1"));
    }

    const dispositions = Array.isArray(manifest.allowedDispositions)
        ? manifest.allowedDispositions
        : [];
    if (dispositions.length === 0 || new Set(dispositions).size !== dispositions.length) {
        findings.push(
            finding("DISPOSITIONS", DEFAULT_MANIFEST, "allowedDispositions must be a non-empty unique array"),
        );
    }

    if (!manifest.governedDocuments || typeof manifest.governedDocuments !== "object") {
        findings.push(finding("DISCOVERY_CONFIG", DEFAULT_MANIFEST, "governedDocuments config is required"));
    }

    const phaseGates = Array.isArray(manifest.phaseGates) ? manifest.phaseGates : [];
    const phaseNumbers = phaseGates.map((gate) => gate?.phase);
    if (
        phaseGates.length !== 10 ||
        new Set(phaseNumbers).size !== 10 ||
        phaseNumbers.some((phase) => !Number.isInteger(phase) || phase < 1 || phase > 10)
    ) {
        findings.push(finding("PHASE_GATES", DEFAULT_MANIFEST, "phaseGates must contain phases 1 through 10 once"));
    }
    for (const gate of phaseGates) {
        if (typeof gate?.owner !== "string" || gate.owner.trim() === "") {
            findings.push(finding("PHASE_OWNER", DEFAULT_MANIFEST, `phase ${gate?.phase ?? "?"} has no owner`));
        }
        if (!Array.isArray(gate?.startRequires) || !Array.isArray(gate?.completionRequires)) {
            findings.push(
                finding("PHASE_EVIDENCE", DEFAULT_MANIFEST, `phase ${gate?.phase ?? "?"} lacks evidence arrays`),
            );
        }
    }

    const invariants = Array.isArray(manifest.invariants) ? manifest.invariants : [];
    const invariantIds = new Set();
    for (const invariant of invariants) {
        if (!invariant || typeof invariant.id !== "string" || !/^[A-Z]+-\d{3}$/.test(invariant.id)) {
            findings.push(finding("INVARIANT_ID", DEFAULT_MANIFEST, "invariant IDs must match GROUP-001"));
            continue;
        }
        if (invariantIds.has(invariant.id)) {
            findings.push(finding("INVARIANT_DUPLICATE", DEFAULT_MANIFEST, `duplicate invariant ${invariant.id}`));
        }
        invariantIds.add(invariant.id);
        if (typeof invariant.statement !== "string" || invariant.statement.trim() === "") {
            findings.push(finding("INVARIANT_STATEMENT", DEFAULT_MANIFEST, `${invariant.id} has no statement`));
        }
    }

    const documents = Array.isArray(manifest.documents) ? manifest.documents : [];
    const documentPaths = new Set();
    let previousPath = null;
    for (const document of documents) {
        for (const field of REQUIRED_DOCUMENT_FIELDS) {
            if (!(field in (document ?? {}))) {
                findings.push(finding("DOCUMENT_FIELD", DEFAULT_MANIFEST, `document row is missing ${field}`));
            }
        }
        const documentPath = document?.path;
        if (!isSafeRelativePath(documentPath)) {
            findings.push(finding("DOCUMENT_PATH", String(documentPath), "path must be normalized and repository-relative"));
            continue;
        }
        if (documentPaths.has(documentPath)) {
            findings.push(finding("DOCUMENT_DUPLICATE", documentPath, "document is registered more than once"));
        }
        documentPaths.add(documentPath);
        if (previousPath !== null && compareCodePoints(previousPath, documentPath) > 0) {
            findings.push(finding("DOCUMENT_ORDER", documentPath, `must sort after ${previousPath}`));
        }
        previousPath = documentPath;
        if (!dispositions.includes(document?.disposition)) {
            findings.push(finding("DOCUMENT_DISPOSITION", documentPath, `invalid disposition ${document?.disposition}`));
        }
        if (
            document?.ownerPhase !== null &&
            (!Number.isInteger(document?.ownerPhase) || document.ownerPhase < 1 || document.ownerPhase > 10)
        ) {
            findings.push(finding("DOCUMENT_OWNER", documentPath, "ownerPhase must be null or an integer from 1 to 10"));
        }
        for (const field of ["kind", "authority", "reason"]) {
            if (typeof document?.[field] !== "string" || document[field].trim() === "") {
                findings.push(finding("DOCUMENT_METADATA", documentPath, `${field} must be a non-empty string`));
            }
        }
    }

    const environment = Array.isArray(manifest.environment) ? manifest.environment : [];
    const environmentNames = new Set();
    let previousEnvironment = null;
    for (const entry of environment) {
        for (const field of REQUIRED_ENVIRONMENT_FIELDS) {
            if (!(field in (entry ?? {}))) {
                findings.push(finding("ENVIRONMENT_FIELD", DEFAULT_MANIFEST, `environment row is missing ${field}`));
            }
        }
        if (typeof entry?.name !== "string" || !/^[A-Z][A-Z0-9_]*$/.test(entry.name)) {
            findings.push(finding("ENVIRONMENT_NAME", String(entry?.name), "environment name is invalid"));
            continue;
        }
        if (environmentNames.has(entry.name)) {
            findings.push(finding("ENVIRONMENT_DUPLICATE", entry.name, "environment name is registered more than once"));
        }
        environmentNames.add(entry.name);
        if (previousEnvironment !== null && compareCodePoints(previousEnvironment, entry.name) > 0) {
            findings.push(finding("ENVIRONMENT_ORDER", entry.name, `must sort after ${previousEnvironment}`));
        }
        previousEnvironment = entry.name;
        if ("value" in entry) {
            findings.push(finding("ENVIRONMENT_VALUE", entry.name, "environment metadata must never contain a value field"));
        }
        if (!ENVIRONMENT_VISIBILITIES.has(entry?.visibility)) {
            findings.push(finding("ENVIRONMENT_VISIBILITY", entry.name, `invalid visibility ${entry?.visibility}`));
        }
        if (!ENVIRONMENT_STATUSES.has(entry?.status)) {
            findings.push(finding("ENVIRONMENT_STATUS", entry.name, `invalid status ${entry?.status}`));
        }
        if (!ENVIRONMENT_REQUIREMENTS.has(entry?.required)) {
            findings.push(finding("ENVIRONMENT_REQUIRED", entry.name, `invalid required state ${entry?.required}`));
        }
        if (!Array.isArray(entry?.environments) || entry.environments.length === 0) {
            findings.push(finding("ENVIRONMENT_SCOPE", entry.name, "environments must be a non-empty array"));
        }
        if (!Array.isArray(entry?.consumers) || (entry.status === "current" && entry.consumers.length === 0)) {
            findings.push(finding("ENVIRONMENT_CONSUMERS", entry.name, "current variables need at least one consumer"));
        }
        if (typeof entry?.owner !== "string" || entry.owner.trim() === "") {
            findings.push(finding("ENVIRONMENT_OWNER", entry.name, "owner must be a non-empty string"));
        }
        if (typeof entry?.rotationTrigger !== "string" || entry.rotationTrigger.trim() === "") {
            findings.push(finding("ENVIRONMENT_ROTATION", entry.name, "rotationTrigger must be a non-empty string"));
        }
        if (
            entry?.retirementPhase !== null &&
            (!Number.isInteger(entry?.retirementPhase) || entry.retirementPhase < 1 || entry.retirementPhase > 10)
        ) {
            findings.push(finding("ENVIRONMENT_RETIREMENT", entry.name, "retirementPhase must be null or 1 through 10"));
        }
    }

    return findings.sort(compareFindings);
}

export async function findInventoryDrift(root, manifest) {
    const discovered = await discoverGovernedDocuments(root, manifest.governedDocuments);
    const registered = (manifest.documents ?? []).map((document) => document.path);
    const discoveredSet = new Set(discovered);
    const registeredSet = new Set(registered);
    const findings = [];

    for (const filePath of discovered) {
        if (!registeredSet.has(filePath)) {
            findings.push(finding("DOCUMENT_UNREGISTERED", filePath, "add a disposition and owner to governance.json"));
        }
    }
    for (const filePath of registered) {
        if (!discoveredSet.has(filePath)) {
            findings.push(finding("DOCUMENT_MISSING", filePath, "remove the stale row or restore the governed file"));
        }
    }
    return findings.sort(compareFindings);
}

function markdownTargets(source) {
    const targets = [];
    const stripped = source.replace(/```[\s\S]*?```/g, "").replace(/`[^`\n]+`/g, "");
    const pattern = /\[[^\]]*\]\(([^)]+)\)/g;
    for (const match of stripped.matchAll(pattern)) targets.push(match[1].trim());
    return targets;
}

function localLinkTarget(markdownPath, rawTarget) {
    let target = rawTarget;
    if (target.startsWith("<") && target.endsWith(">")) target = target.slice(1, -1);
    if (
        target === "" ||
        target.startsWith("#") ||
        target.startsWith("//") ||
        /^[a-z][a-z0-9+.-]*:/i.test(target)
    ) {
        return null;
    }
    target = target.split("#", 1)[0].split("?", 1)[0];
    try {
        target = decodeURIComponent(target);
    } catch {
        return { invalid: true, target };
    }
    return { invalid: false, target: path.resolve(path.dirname(markdownPath), target) };
}

export async function findBrokenActiveMarkdownLinks(root, manifest) {
    const findings = [];
    const activeMarkdown = (manifest.documents ?? []).filter(
        (document) => document.disposition === "active" && /\.mdx?$/i.test(document.path),
    );

    for (const document of activeMarkdown) {
        const absoluteDocument = path.join(root, document.path);
        let source;
        try {
            source = await readFile(absoluteDocument, "utf8");
        } catch {
            continue;
        }
        for (const rawTarget of markdownTargets(source)) {
            const resolved = localLinkTarget(absoluteDocument, rawTarget);
            if (!resolved) continue;
            if (resolved.invalid) {
                findings.push(finding("MARKDOWN_LINK", document.path, "contains an invalid encoded local link"));
                continue;
            }
            try {
                await stat(resolved.target);
            } catch (error) {
                if (error && typeof error === "object" && "code" in error && error.code === "ENOENT") {
                    findings.push(finding("MARKDOWN_LINK", document.path, `local target does not exist: ${rawTarget}`));
                } else {
                    throw error;
                }
            }
        }
    }
    return findings.sort(compareFindings);
}

export async function findCredentialBearingUrls(root, excludedRoots = [], exceptionPaths = []) {
    const findings = [];
    const exceptions = new Set(exceptionPaths);
    const candidateExtensions = new Set([
        ".cjs", ".gradle", ".groovy", ".java", ".js", ".json", ".jsx", ".kt", ".kts", ".md", ".mdx", ".mjs", ".properties", ".sh", ".toml", ".ts", ".tsx", ".txt", ".xml", ".yaml", ".yml",
    ]);
    const files = await discoverAllFiles(root, excludedRoots);

    for (const filePath of files) {
        if (exceptions.has(filePath)) continue;
        const basename = path.posix.basename(filePath);
        if (!candidateExtensions.has(path.posix.extname(filePath)) && basename !== ".env.example") continue;
        let source;
        try {
            source = await readFile(path.join(root, filePath), "utf8");
        } catch {
            continue;
        }
        const lines = source.split(/\r?\n/);
        for (let index = 0; index < lines.length; index += 1) {
            if (CREDENTIAL_URL_PATTERNS.some((pattern) => pattern.test(lines[index]))) {
                findings.push(
                    finding("CREDENTIAL_URL", filePath, `credential-bearing URL pattern at line ${index + 1}`),
                );
            }
        }
    }
    return findings.sort(compareFindings);
}

export async function discoverEnvironmentNames(root, excludedRoots = []) {
    const names = new Set();
    const files = await discoverAllFiles(root, excludedRoots);
    const candidateExtensions = new Set([".gradle", ".groovy", ".java", ".js", ".jsx", ".kt", ".kts", ".mjs", ".sh", ".ts", ".tsx", ".yaml", ".yml"]);

    for (const filePath of files) {
        if (!candidateExtensions.has(path.posix.extname(filePath))) continue;
        let source;
        try {
            source = await readFile(path.join(root, filePath), "utf8");
        } catch {
            continue;
        }
        for (const pattern of ENVIRONMENT_PATTERNS) {
            pattern.lastIndex = 0;
            for (const match of source.matchAll(pattern)) names.add(match[1]);
        }
    }
    return [...names].sort(compareCodePoints);
}

export async function findEnvironmentDrift(root, manifest) {
    if (manifest.environmentEnforced !== true) return [];
    const discovered = await discoverEnvironmentNames(root, manifest.governedDocuments?.excludedRoots ?? []);
    const registered = (manifest.environment ?? []).map((entry) => entry.name);
    const discoveredSet = new Set(discovered);
    const registeredSet = new Set(registered);
    const findings = [];
    for (const name of discovered) {
        if (!registeredSet.has(name)) findings.push(finding("ENVIRONMENT_UNREGISTERED", name, "add environment metadata"));
    }
    for (const name of registered) {
        const entry = manifest.environment.find((candidate) => candidate.name === name);
        if (!discoveredSet.has(name) && entry?.status === "current") {
            findings.push(finding("ENVIRONMENT_UNUSED", name, "current variable has no discovered consumer"));
        }
    }
    return findings.sort(compareFindings);
}

export function compareFindings(left, right) {
    return (
        compareCodePoints(left.path, right.path) ||
        compareCodePoints(left.code, right.code) ||
        compareCodePoints(left.message, right.message)
    );
}

export function formatFindings(findings) {
    if (findings.length === 0) return "governance: pass";
    const lines = [`governance: ${findings.length} finding(s)`];
    for (const item of findings) {
        lines.push(`- [${item.code}] ${item.path}: ${item.message}`);
    }
    return lines.join("\n");
}

export async function runGovernanceCheck({ root, manifestPath = DEFAULT_MANIFEST }) {
    const resolvedRoot = await realpath(root);
    const resolvedManifest = path.resolve(resolvedRoot, manifestPath);
    const manifest = JSON.parse(await readFile(resolvedManifest, "utf8"));
    const findings = [
        ...validateManifestShape(manifest),
        ...(await findInventoryDrift(resolvedRoot, manifest)),
        ...(await findBrokenActiveMarkdownLinks(resolvedRoot, manifest)),
        ...(await findCredentialBearingUrls(
            resolvedRoot,
            manifest.governedDocuments?.excludedRoots ?? [],
            (manifest.temporaryExceptions?.credentialUrlFiles ?? []).map((entry) => entry.path),
        )),
        ...(await findEnvironmentDrift(resolvedRoot, manifest)),
    ].sort(compareFindings);
    return { manifest, findings };
}

function parseArguments(argv) {
    const options = { root: process.cwd(), manifestPath: DEFAULT_MANIFEST, format: "text" };
    for (let index = 0; index < argv.length; index += 1) {
        const argument = argv[index];
        if (argument === "--root") options.root = argv[++index];
        else if (argument === "--manifest") options.manifestPath = argv[++index];
        else if (argument === "--format=json") options.format = "json";
        else throw new Error(`unknown argument: ${argument}`);
    }
    if (!options.root || !options.manifestPath) throw new Error("--root and --manifest require values");
    return options;
}

async function main() {
    try {
        const options = parseArguments(process.argv.slice(2));
        const result = await runGovernanceCheck(options);
        if (options.format === "json") {
            process.stdout.write(`${JSON.stringify({ ok: result.findings.length === 0, findings: result.findings }, null, 2)}\n`);
        } else {
            process.stdout.write(`${formatFindings(result.findings)}\n`);
        }
        if (result.findings.length > 0) process.exitCode = 1;
    } catch (error) {
        const message = error instanceof Error ? error.message : String(error);
        console.error(`governance: ${message}`);
        process.exitCode = 1;
    }
}

const isMain = process.argv[1] && pathToFileURL(path.resolve(process.argv[1])).href === import.meta.url;
if (isMain) void main();
