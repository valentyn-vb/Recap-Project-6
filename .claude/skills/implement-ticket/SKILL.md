---
name: implement-ticket
description: >-
  Implement a confirmed plan for a GitHub issue: create the feature branch,
  work strictly TDD, and commit in logical steps via the ship skill. Use when
  the user says "implement issue #N" after a plan is approved, or as the
  implementation step of the issue-to-pr loop.
---

# implement-ticket

Execute a developer-approved plan for one issue on a feature branch, TDD-first, committing per logical step. Input: an issue number and a confirmed plan (run [`plan-ticket`](../plan-ticket/SKILL.md) first — do not start without an approved plan). Output: a feature branch with green, committed work, ready for [`open-pr`](../open-pr/SKILL.md).

**Model:** Sonnet is the default working model for implementation. If the session was switched to Opus for planning, switch back (`/model sonnet`) before implementing. Any `Agent` subagent handling an isolated chunk should pass `model: "sonnet"`.

## Step 1 — Feature branch

Do this before any implementation code.

1. Check working tree: `git status --short`. If dirty, ask the developer before stashing or discarding.
2. Update `main` and branch:

   ```bash
   git fetch origin main
   git checkout main
   git pull origin main
   git checkout -b feat/issue-<number>-<short-slug>
   ```

3. **Branch slug:** lowercase, hyphenated, ~3 words from the issue title (e.g. issue #2 → `feat/issue-2-status-filtering`).

Never commit on `main` or `master`.

## Step 2 — TDD implementation

1. Follow the approved plan strictly. TDD order: write failing test → run test (confirm red) → implement minimal code → run test (confirm green).
2. Work in **logical steps** aligned with the plan (e.g. backend endpoint, then frontend UI, then integration). Complete one step fully before starting the next.
3. Re-read stack-specific docs (`backend/CLAUDE.md`, `frontend/CLAUDE.md`) before editing inside either app.

## Step 3 — Commit per logical step

After each logical step passes its app gate:

| App touched | Gate |
|-------------|------|
| `backend/` | `cd backend && ./gradlew test` |
| `frontend/` | `cd frontend && npm run test && npx vue-tsc -b` |

1. Stage only files for that step (`git add <paths>`).
2. Commit via the [`ship`](../ship/SKILL.md) skill (preferred), or manually with Conventional Commits per `CLAUDE.md` §7 (`feat(backend):`, `feat(frontend):`, etc.).
3. Include `Refs #<number>` in the commit body.
4. Repeat until all plan steps are committed.

Do not push — that's [`open-pr`](../open-pr/SKILL.md)'s job.

Note: a project `PreToolUse` hook (`.claude/settings.json`) re-runs the staged apps' test gates on every `git commit` and blocks the commit if they fail. It is a backstop — still run the gates yourself before committing; don't lean on the hook to schedule them.

## Guardrails

- Do not start without a developer-confirmed plan.
- Never commit on `main`/`master`; never commit red.
- Do not commit secrets (`.env`, API keys).
