# Governance Guide

This is the human-readable authority for executing development on **OnTheRoad**.
Its machine-checkable counterpart is [`governance.json`](governance.json); the approved
capability contract remains the product PRD.

The objective is speed with controlled authority: make one bounded change, prove it,
preserve rollback, record evidence, and confirm before crossing the next phase boundary.

---

## Authority Order

When instructions disagree, use this order and stop if the conflict would change
scope, data authority, security, money behavior, or the next phase:

1. Current system/developer instructions and the user's latest explicit decision.
2. The approved product PRD (`.claude/PRPs/prds/*.prd.md`).
3. The approved PRP for the active phase (`.claude/PRPs/plans/*.plan.md`).
4. This governance guide and `governance.json`.
5. Current `AGENTS.md` and `docs/GIT_WORKFLOW.md` production safeguards.
6. Other active project documentation.
7. Historical, archive-later, dormant, or imported reference material.

A target-state document never overrides a still-active production safeguard merely because
the target is newer.

---

## State Vocabulary

Every technical statement and governed document belongs to one of these states:

| State | Meaning | Permitted Use |
|---|---|---|
| Current production truth | Describes code or operations active now | Follow until replacement gate passes |
| Approved target | A reviewed destination that is not yet authoritative | Implement only inside its approved phase |
| Transition | Both systems exist, with one explicitly named authority | Follow the phase's one-writer and rollback contract |
| Retired | Replacement is proven and retirement explicitly approved | Remove active references; retain necessary evidence |
| Historical evidence | Point-in-time report, audit, plan, or baseline | Use as evidence, never as current architecture |

The document dispositions in `governance.json` are:
- `active`: authoritative or applicable now;
- `revise`: still needed, but owning phase must reconcile content;
- `archive-later`: retain until cleanup phase decides final disposition;
- `historical`: immutable point-in-time evidence.

---

## Invariants

IDs are stable. Change an invariant only through an approved governance change; never renumber to hide history.

### Git & Delivery

| ID | Rule | Owner Phase | Severity |
|---|---|---:|---|
| `GIT-001` | GitHub is the sole write and merge authority; any mirrors are review-only projections. | 1 | Critical |
| `GIT-002` | `main` deploys/releases and `dev` contains `main`; neither is ever force-pushed. | 1 | Critical |
| `GIT-003` | Promotions and releases use merge commits; only the sync workflow reconciles `dev`. | 1 | Critical |

### Documentation & Phases

| ID | Rule | Owner Phase | Severity |
|---|---|---:|---|
| `DOC-001` | Current truth, approved target, and historical evidence are distinct. | 1 | High |
| `DOC-002` | Every governed instruction/document has one disposition and owner phase. | 1 | High |
| `PHASE-001` | No phase starts, completes, or advances without evidence and explicit user confirmation. | 1 | Critical |

### Secrets & Security

| ID | Rule | Owner Phase | Severity |
|---|---|---:|---|
| `SEC-001` | Keystores, signing passwords, and API secrets never appear in Git URLs, files, or logs. | 1 | Critical |
| `SEC-002` | Privileged workflows never execute or inspect untrusted PR content. | 1 | Critical |

### Android Toolchain & Architecture

| ID | Rule | Owner Phase | Severity |
|---|---|---:|---|
| `BUILD-001` | All dependencies, plugins, and toolchains are pinned in `libs.versions.toml`; no dynamic `+` versions. | 3 | Critical |
| `BUILD-002` | Gradle wrapper uses distribution SHA-256 checksum verification. | 3 | High |
| `ARCH-001` | `domain` and `model` layers are pure Kotlin; they must never import `android.*` or `androidx.*`. | 3 | Critical |
| `UI-001` | UI state in Jetpack Compose is unidirectional (UDF); ViewModels expose immutable `StateFlow`. | 4 | High |
| `TEST-001` | Unit tests for domain and ViewModels run on JVM without requiring Android emulators. | 3 | High |
| `TEST-002` | UI tests prioritize fast JVM screenshot tests; emulator runs are serialized and isolated. | 4 | High |

---

## Phase Gates and Lifecycle

| Phase | Scope | Start Requires | Completion Requires | Status |
|---:|---|---|---|---|
| 1 | Governance and safety foundation | approved plan, user approval | verifier passing, baseline frozen, dev/main sync, explicit confirmation | completed |
| 2 | Product discovery and PRD | Phase 1 complete, approved PRP | approved PRD in `.claude/PRPs/prds/`, explicit confirmation | completed |
| 3 | Toolchain & architecture scaffold | Phase 2 complete, approved PRP | clean Gradle build, libs.versions.toml, architecture modules, CI passing | completed |
| 4 | Core domain & data models | Phase 3 complete, approved PRP | pure Kotlin domain models, repository interfaces, JVM unit tests | completed |
| 5 | Design system & UI shell | Phase 4 complete, approved PRP | Material 3 tokens, Compose navigation shell, preview tests | completed |
| 6 | Feature core implementation | Phase 5 complete, approved PRP | end-to-end user flows, offline caching, repository integration | completed |
| 7 | Performance, motion & UX | Phase 6 complete, approved PRP | 60fps/120fps rendering, Compose metrics, haptics, transition polish | completed |
| 8 | Security, secrets & release signing | Phase 7 complete, approved PRP | signed debug/release APK/AAB via CI secrets, zero secrets in code | completed |
| 9 | Verification & release candidate | Phase 8 complete, approved PRP | full test suite green, release notes, APK artifact validation | in-progress |
| 10 | Retirement & living docs | Phase 9 complete, release tagged | all temporary exceptions closed, living docs validated, 0 findings | pending |

---

## Rollback Policy

Every phase identifies:
- The last known-good commit/release ref;
- The exact switch or ref that changes authority;
- How rollback is executed (e.g. reverting a promotion merge commit on `main`, which automatically syncs to `dev`);
- That `main` and `dev` are **never force-pushed** as part of rollback.

---

## Maintenance

Run the governance verifier after changing instructions, documents, environment names,
phase gates, or this manifest:

```bash
bun run governance:check
```

Documentation-only changes use a cheap, secret-free workflow and do not run Gradle or consume Android build runners.
