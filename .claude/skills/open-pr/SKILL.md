---
name: open-pr
description: >-
  Push the current feature branch and open a GitHub pull request referencing
  its issue, with a summary and test plan. Use when the user says "open a PR"
  after implementation is committed, or as the final step of the issue-to-pr
  loop.
---

# open-pr

Push the current feature branch and open a PR for developer review. Input: a feature branch with all logical steps committed and gates green (run [`implement-ticket`](../implement-ticket/SKILL.md) first). Output: the PR URL.

## Preconditions

- Current branch is a feature branch (`feat/issue-<number>-...`), **not** `main`/`master`.
- Working tree is clean (`git status --short`) — everything intended for the PR is committed.
- The last run of each touched app's gate was green (`backend/`: `./gradlew test`; `frontend/`: `npm run test && npx vue-tsc -b`). If unsure, re-run before pushing.

If any precondition fails, stop and report instead of pushing.

## Steps

```bash
git push -u origin HEAD
gh pr create --title "feat: <issue title> (#<number>)" --body "$(cat <<'EOF'
## Summary
- Closes #<number>
- <bullet summary of changes>

## Test plan
- [ ] Backend: `cd backend && ./gradlew test`
- [ ] Frontend: `cd frontend && npm run test && npx vue-tsc -b`
- [ ] Manual: <from issue acceptance criteria>
EOF
)"
```

Return the PR URL to the developer. Do **not** merge — the developer reviews.

## Guardrails

- Do not force-push to `main`/`master`.
- Do not merge the PR.
- Omit the Test plan checkboxes that don't apply (e.g. backend-only change → no frontend line).
