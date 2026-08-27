# Agent Instructions

OnTheRoad is a native Android application written in Kotlin and using Jetpack Compose,
held to the higher standard established by `wandernest`.
Read [`docs/governance/GOVERNANCE.md`](docs/governance/GOVERNANCE.md) for authority order,
invariants, and phase gates, and read [`docs/GIT_WORKFLOW.md`](docs/GIT_WORKFLOW.md)
before doing anything involving branches, merges, or releases.

## Branching, in one paragraph

`main` is the default branch and produces releases. `dev` is the integration branch.
Feature and fix branches are cut from `dev` and open pull requests **into `dev`**. Batches
of `dev` are promoted to `main` through a promotion pull request. A workflow syncs `dev`
after anything lands on `main`: a promotion is a fast-forward, so the two branches sit at
the **same commit** between promotions, while a hotfix landing while `dev` has moved on is
merged down instead and leaves `dev` containing `main` but ahead of it.

GitHub is the sole write and merge authority. Any replicas or mirrors are strictly review-only.

## Rules that break things if broken

1. **Promotion and hotfix pull requests must be merged with "Create a merge commit".**
   Squash and rebase rewrite history into commits `dev` has never seen, which makes the
   automatic fast-forward impossible and needs a force push to recover. Both are disabled
   in repository settings. Do not re-enable them.

2. **Never force push `main` or `dev`, and never sync them by hand.**
   `.github/workflows/sync-dev-to-main.yml` owns that. If it fails, read its run summary
   rather than fixing the branches manually.

3. **Never commit keystores, certificates, or secrets to Git (`SEC-001`).**
   Signing keys (`*.jks`, `*.keystore`) and passwords belong in CI secrets or `local.properties`.
   They must never be committed.

4. **Never introduce dynamic or floating dependency versions (`BUILD-001`).**
   All dependencies and compiler versions must be pinned in `gradle/libs.versions.toml`.

5. **`domain` and `model` layers must remain pure Kotlin (`ARCH-001`).**
   Never import `android.*` or `androidx.*` in domain UseCases or model classes.

## CI

CI is split into two tiers:
1. `.github/workflows/governance.yml`: Independent, secret-free check running on documentation,
   rules, and manifest changes. Runs in seconds without starting Gradle.
2. `.github/workflows/android.yml`: Pinned JDK 21, Gradle cache, lint, and JVM unit tests.
   Skips documentation-only changes via `paths-ignore`.

Do not run heavy end-to-end device suites locally if CI or headless JVM tests suffice.
Avoid pushing many tiny commits to an open PR to conserve runner minutes.

## Conventions

- Match the surrounding code. Favour explanatory comments that record *why* a non-obvious
  choice was made, especially in build configuration and architecture boundaries.
- Jetpack Compose state is unidirectional (UDF). ViewModels expose immutable `StateFlow`.
- User-facing strings belong in `strings.xml`, not hardcoded string literals.

## Phase gates

- Work proceeds strictly phase-by-phase. Only the phase marked `in-progress` may be implemented.
- Do not begin, complete, or advance a phase without explicit user confirmation and recorded
  evidence (`PHASE-001`).
- Linear project `OnTheRoad` tracks task start, completion evidence, deviations, and rollback impact.
  Never put secret values in Linear.
