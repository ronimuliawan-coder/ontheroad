# OnTheRoad ECC Project Profile

OnTheRoad carries a project-local ECC profile under `.agents`. The files are repository-owned
for review, stability, and reproducibility.

Every instruction and support file is classified in [`docs/governance/governance.json`](governance/governance.json).
The project retains stack-relevant guidance and active safety mechanisms, ensuring instruction integrity.

## Provenance

- Source version: ECC 2.2.0
- Source commit: `63040e29b52f51709d0255a461a7a762edced8bb`
- Original target: `antigravity-project`
- Footprint:
  - 27 skill directories (focused on architecture, testing, UI motion, and Kotlin/Android)
  - 30 agent role definitions
  - 73 workflow command definitions (including `prp-prd.md`, `prp-plan.md`, `prp-implement.md`)
  - 15 rule files (10 common rules + 5 Kotlin rules)

`.agents/ecc-install-state.json` records the exact installation operations and content SHA-256 hashes.

Reproduce the inventory:

```bash
find .agents/skills -mindepth 1 -maxdepth 1 -type d | wc -l
find .agents/agents -maxdepth 1 -type f -name '*.md' | wc -l
find .agents/workflows -maxdepth 1 -type f -name '*.md' | wc -l
find .agents/rules -type f -name '*.md' | wc -l
```

## Inventory and Status

### Skills
Includes general engineering skills (`api-design`, `architecture-decision-records`, `coding-standards`,
`continuous-learning-v2`, `delivery-gate`, `design-system`, `error-handling`, `git-workflow`,
`hexagonal-architecture`, `intent-driven-development`, `make-interfaces-feel-better`, `motion-ui`,
`security-review`, `tdd-workflow`, `verification-loop`) and stack-specific skills
(`android-clean-architecture`, `compose-multiplatform-patterns`, `kotlin-coroutines-flows`,
`kotlin-patterns`, `kotlin-testing`).

### Agents
Markdown definitions under `.agents/agents` represent specialized roles (`architect`, `code-reviewer`,
`code-simplifier`, `kotlin-reviewer`, `kotlin-build-resolver`, `security-reviewer`). They serve as reference
guidance and review rubrics.

### Workflows
On-demand execution guides under `.agents/workflows`. The core PRP workflows (`prp-prd.md`, `prp-plan.md`,
`prp-implement.md`, `prp-commit.md`, `prp-pr.md`) govern the delivery lifecycle.

### Rules
Under `.agents/rules`:
- 10 common rules: `agents`, `code-review`, `coding-style`, `development-workflow`, `git-workflow`,
  `hooks`, `patterns`, `performance`, `security`, `testing`.
- 5 Kotlin rules: `coding-style`, `hooks`, `patterns`, `security`, `testing`.

## OnTheRoad Overrides

- Feature and fix PRs target `dev`. `main` is only for releases via promotion PRs using merge commits.
- Never force-push `main` or `dev`.
- Never commit keystores, signing credentials, or sensitive secrets (`SEC-001`).
- Clean architecture: domain and model layers are pure Kotlin, strictly free of `android.*` dependencies.
- No phase advances without its approved PRP, evidence, and explicit user confirmation (`PHASE-001`).

## Refresh Procedure

Treat ECC updates as a reviewed vendor refresh, not an automated overwrite:
1. Compare upstream definitions against project-owned copies.
2. Verify all files remain registered in `docs/governance/governance.json`.
3. Run `npm run governance:check` to ensure zero drift.
