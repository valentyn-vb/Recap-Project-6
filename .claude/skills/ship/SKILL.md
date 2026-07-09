---
name: ship
description: Run the relevant app's test suite (and frontend type-check) as gates, then commit staged changes with a generated Conventional Commits message. Aborts if tests fail or type-checking reports errors. Use when the user says "ship it", "ship", or wants to run checks and commit.
---

# ship

Gate staged changes through this monorepo's test suites, then commit. Stop at the first failing gate — do not commit if a gate fails.

This repo has two independently-built apps (`backend/`, `frontend/`) and no linter configured in either (see `backend/CLAUDE.md`, `frontend/CLAUDE.md`) — type-checking (`vue-tsc`) is the closest equivalent gate on the frontend. Follow the TDD rule in root `CLAUDE.md`: never commit red.

## Steps

1. **Confirm there is something to commit.** Run `git status --short`. If nothing is staged, tell the user and stop (offer to stage with `git add`).

2. **Scope the gates to what's staged.** Run `git diff --staged --name-only` and check which app(s) the staged files touch:
   - Any path under `backend/` → run the backend gate.
   - Any path under `frontend/` → run the frontend gates.
   - Root-only files (e.g. `CLAUDE.md`, root `package.json`) → no test gate needed, skip to step 5.
   - If both apps have staged changes, run both apps' gates.

3. **Backend gate** (if scoped in): run `./gradlew test` from `backend/`.
   - If any test fails, **abort**: report the failing tests and stop. Do not run remaining gates or commit.

4. **Frontend gates** (if scoped in), from `frontend/`:
   - `npm run test` (Vitest, single-run mode).
   - `npx vue-tsc -b` (type-check — lighter than a full `npm run build`).
   - If either reports failures/errors, **abort**: report them and stop. Do not commit.

5. **Generate the commit message.** Inspect `git diff --staged` and write a concise Conventional Commits message (`feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`), scoped by app when it adds clarity (`feat(backend): ...`, `feat(frontend): ...`, `chore(root): ...`) per root `CLAUDE.md`. Imperative subject line; body only if it adds signal. Match the style of recent commits (`git log --oneline -10`).

6. **Commit.** Run `git commit -m "<generated message>"`, committing only what is already staged. Then show `git log --oneline -1` to confirm.

## Notes

- Only staged changes are committed — never `git add -A` on the user's behalf unless they ask.
- Do not push. Committing is the last step.
- If the working directory has both staged and unstaged changes, commit only the staged ones and mention the leftover unstaged files.
- If a gate can't run (e.g. `frontend/node_modules` missing), report that clearly instead of silently skipping it — don't let a broken environment look like a passing gate.
