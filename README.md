# OnTheRoad

OnTheRoad is a native Android application built with Kotlin and Jetpack Compose,
developed under the strict, confirmation-gated engineering standard established
by [`wandernest`](/home/ron/Projects/wandernest).

## Prerequisites

- **JDK**: OpenJDK 21+ (recommended: `/home/ron/.jdks/jbr-21.0.11`)
- **Android SDK**: Android API 35+ / Build Tools 35.0.0+ (`/home/ron/Android/Sdk`)
- **Node.js**: Node 20+ (used solely for lightweight repository governance verification)

## Governance & Verification

All repository instructions, phase gates, and delivery rules are machine-checked
without installing dependencies or starting Gradle:

```bash
npm run governance:check
```

Or invoke the validator directly:

```bash
node scripts/verify-governance.mjs
```

## Documentation & Architecture

- [`AGENTS.md`](AGENTS.md) — Load-bearing agent rules, invariants, and constraints.
- [`docs/governance/GOVERNANCE.md`](docs/governance/GOVERNANCE.md) — Authority hierarchy, phase gates, and rollback policies.
- [`docs/governance/BASELINE.md`](docs/governance/BASELINE.md) — Pre-development environment evidence.
- [`docs/GIT_WORKFLOW.md`](docs/GIT_WORKFLOW.md) — Branching, merge commit requirement, and automated sync.
- [`docs/ECC_PROJECT_PROFILE.md`](docs/ECC_PROJECT_PROFILE.md) — ECC agent profile provenance and inventory.

## Git Workflow

- `main` is the release branch.
- `dev` is the active integration branch.
- Feature and fix branches open pull requests into `dev`.
- Promotion pull requests from `dev` into `main` must use **"Create a merge commit"**.
- `.github/workflows/sync-dev-to-main.yml` automatically fast-forwards `dev` back to `main` upon merge.
