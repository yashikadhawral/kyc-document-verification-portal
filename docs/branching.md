# Branching and commit policy

## Branches

| Branch | Purpose |
|--------|---------|
| `main` | Stable, release-ready code. Only changed by merging `develop` or a hotfix. |
| `develop` | Integration branch. Feature branches merge here. |
| `feature/<issue>-<short-name>` | One new feature or backlog item, for example `feature/12-create-kyc-record` |
| `bugfix/<issue>-<short-name>` | A fix found during development, for example `bugfix/20-search-empty-result` |
| `hotfix/<short-name>` | An urgent fix to `main` |
| `release/<version>` | Preparing a tagged release, for example `release/1.0.0` |

Rules:
- Lowercase letters, digits and hyphens only. No spaces.
- Start from `develop` (except hotfixes, which start from `main`).
- One branch per issue. Delete the branch after it is merged.

## Commit messages

Format: `type: short summary in the imperative`

| Type | Use for |
|------|---------|
| `feat` | A new feature |
| `fix` | A bug fix |
| `docs` | Documentation only |
| `test` | Adding or changing tests |
| `build` | Maven, Docker or dependency changes |
| `ci` | Jenkinsfile and pipeline changes |
| `chore` | Housekeeping |
| `refactor` | Code change that does not alter behaviour |

Keep the summary under 72 characters. Add the issue number when there is one, for example
`feat: add create record form (#12)`.

## Pull requests

- Open a pull request from the feature branch into `develop`.
- Link the issue with `Closes #<number>`.
- `mvn test` must pass before merging.
- Review the changes against the pull request checklist, leave review comments, then merge.
- Merge into `main` only from `develop`, and tag releases as `v<major>.<minor>.<patch>`.