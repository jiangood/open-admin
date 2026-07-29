---
name: oa-publishing-release
description: Use when preparing a new release of an open-admin based project — checks remote version, bumps version, runs tests, tags and pushes to trigger CI
---

# OA Publishing Release

## Overview

Automates the release workflow for open-admin framework projects: check latest remote release → propose new version → bump version across pom.xml + package.json → run full test suite (backend unit + frontend build + E2E) → commit + tag + push.

## When to Use

- Before publishing a new release to Maven Central / npm
- When preparing a version bump across all pom.xml and web/package.json
- After feature work is complete and ready for release

## Prerequisites

- `gh` CLI installed and authenticated (`gh auth status`)
- Git remote configured (`git@github.com:jiangood/open-admin.git`)
- Maven + Node.js available
- Ports 8080 + 3000 free (for E2E tests)

## Workflow

### 1. Check Remote Release Version

```bash
gh release list --repo jiangood/open-admin -L 5
```

Identify the latest release tag (e.g. `v3.0.1`). Extract version number.

### 2. Propose New Version & Confirm

Compute default suggestion (patch bump), then present options:

```bash
# Current: 3.0.1 → default: 3.0.2
# Options: patch (3.0.2), minor (3.1.0), major (4.0.0), custom
```

Present to user with question tool. Only continue after user confirms.

### 3. Bump Version

```bash
node scripts/bump-version.js <confirmed_version>
```

Verify changes look correct with `git diff`.

### 4. Run Tests

All three must pass. If any fails, stop and report.

```bash
# 4a) Backend unit tests
mvn clean test -Dtest='!*RepositoryTest,!*ServiceTest'

# 4b) Frontend build
cd web && npm run build

# 4c) E2E tests (auto-starts backend + frontend)
cd web && npm run test:e2e
```

Return to `open-admin/` root after each `cd`.

### 5. Commit, Tag, Push

```bash
git add -A
git commit -m "release: v<version>"
git tag v<version>
git push origin main --tags
```

### 6. Monitor CI (Optional)

Check if `.github/workflows/` exists:

```bash
Test-Path -LiteralPath ".github/workflows"
```

- If exists: CI will trigger automatically. Monitor until green.
- If absent: skip this step. Release is complete.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `gh` not authenticated | Run `gh auth login` |
| Test failure | Fix the issue, do NOT skip tests. Re-run from step 4 |
| Wrong version bumped | `git checkout -- .` to revert, then restart |
| Tag already exists | `git tag -d v<version> && git push origin :refs/tags/v<version>` |

## Common Mistakes

- Pushing without running all tests (E2E catches integration issues)
- Forgetting to push tags (`--tags` flag required)
- Bumping version but not committing the change
- Running `mvn test` without `clean` (stale class files)
- Skipping E2E because "unit tests pass" (E2E tests auth + page lifecycle)
- Running commands from wrong working directory (always from project root)
