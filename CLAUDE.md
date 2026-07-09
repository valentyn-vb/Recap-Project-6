# Recap Project 6

## 1. Project Overview

- **Name**: Recap Project 6
- **Structure**: Two-app monorepo — Spring Boot backend + Vue 3 frontend.
- **Architecture style**: Folder convention, not an npm/yarn/pnpm workspace — there is no
  `workspaces` field anywhere. `backend/` and `frontend/` build and run completely
  independently (Gradle wrapper vs. npm); the root only glues dev servers together via
  `concurrently`.
- **State**: Both apps are fresh scaffolds — no business logic, no persistence, no auth,
  no wiring between them yet. Don't assume features exist just because the repo name
  implies a future purpose; there is no domain logic today.

```
.
├── backend/    Spring Boot REST API (Gradle, independent build) — see backend/CLAUDE.md
└── frontend/   Vue 3 SPA (npm/Vite, independent build) — see frontend/CLAUDE.md
```

Stack-specific commands and rules live in the nested files — read them before editing
inside either folder.

## 2. Build & Development Commands

```bash
npm install                    # root: installs concurrently only
npm install --prefix frontend  # frontend deps (backend deps come via Gradle, see backend/CLAUDE.md)

npm run dev            # both apps together
npm run dev:backend    # Spring Boot on :8080
npm run dev:frontend   # Vite dev server on :5173
```

There is no root-level test or lint command — each app tests/lints independently
(`backend/CLAUDE.md`, `frontend/CLAUDE.md`).

## 3. Code Style & Conventions

The root has no application source of its own (just `package.json` and repo-wide
config), so there's no root-level style guide. Guardrails that apply here:

- Keep root-level tooling minimal. Don't add Lerna/Turborepo/Nx or an npm `workspaces`
  field unless explicitly asked — the current `concurrently` glue is intentional, not
  an oversight.
- Don't assume a database, authentication, or a frontend↔backend API contract exist —
  none currently do. Any of that is new work, not a fix.

## 4. Repository & Git Preferences

- **Commit messages**: [Conventional Commits](https://www.conventionalcommits.org/) —
  `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`. Scope by app when it adds
  clarity: `feat(backend): ...`, `feat(frontend): ...`, `chore(root): ...`.
- **Branching**: Trunk-based — work directly on `main` or via short-lived branches
  merged back quickly. No long-lived feature branches, no release branches, matching
  the project's current early-scaffold state.

## Don't

- Don't commit machine-specific config as if it were portable (e.g.
  `backend/gradle.properties` currently has a local Homebrew JDK path — that's a
  smell to flag, not a pattern to copy).
- Don't add CI workflows or Docker config speculatively — none exist today.
