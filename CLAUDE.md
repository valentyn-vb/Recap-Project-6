# CLAUDE.md — Learning Companion (AI Factory Project)

## 1. Project Overview

- **App Name:** Learning Companion
- **Purpose:** A tracker for goals, learning sessions, and resources with AI-powered progress summaries and recommendations.
- **Core Workflow:** Strict Test-Driven Development (TDD) via an automated AI pipeline (see [§3 Workflow](#3-workflow-test-driven-development)).
- **Structure:** Two-app monorepo — Spring Boot backend + Vue 3 frontend.
- **Architecture style:** Folder convention, _not_ an npm/yarn/pnpm workspace — there is no `workspaces` field anywhere. `backend/` and `frontend/` build and run completely independently (Gradle wrapper vs. npm); the root only glues dev servers together via `concurrently`.

```
.
├── backend/    Spring Boot REST API (Gradle, independent build) — see backend/CLAUDE.md
└── frontend/   Vue 3 SPA (npm/Vite, independent build)          — see frontend/CLAUDE.md
```

Stack-specific commands and rules live in the nested files — read them before editing inside either folder.

## 2. Architecture & Core Domain Model

The backend uses a strict 3-layer architecture (`@RestController` → `@Service` → `@Repository`).

### Entities & Relationships

1. **User / Profile:** Managed via Spring Security. `Profile` contains `name`, `cohort`, and a list of `focus_area` tags.
2. **Goal:** Linked to User. Fields: `id`, `title`, `description`, `status` (PLANNED, IN_PROGRESS, DONE), `createdAt`, `updatedAt`.
3. **LearningSession:** Many-to-One with `Goal`. Fields: `id`, `date`, `duration` (hours/minutes), `notes`, `tags` (list/string).
4. **Resource:** Many-to-One with `Goal`. Fields: `id`, `url`, `title`, `type` (ARTICLE, VIDEO, REPO, DOC).

## 3. Workflow: Test-Driven Development

Strict TDD applies to every change, in both apps. **Always write unit/integration tests (`src/test/java`) _before_ implementing business logic in `@Service` or endpoints in `@RestController`.**

1. Write a failing test first — no implementation code before a test exists.
2. Run the relevant app's test command and confirm it fails for the expected reason.
3. Implement the minimal code needed to make it pass.
4. Re-run tests and confirm they're green.
5. Only commit once tests are green — never commit red.

See `backend/CLAUDE.md` and `frontend/CLAUDE.md` for the exact per-stack test commands and file conventions.

## 4. Build & Development Commands

### Root (monorepo orchestration)

```bash
npm install                    # installs concurrently only
npm install --prefix frontend  # frontend deps (backend deps come via Gradle)

npm run dev            # both apps together
npm run dev:backend    # Spring Boot on :8080
npm run dev:frontend   # Vite dev server on :5173
```

There is no root-level test or lint command — each app tests/lints independently.

### Backend

> ⚠️ **Layout conflict to resolve:** the tree in §1 places the backend in `backend/`, but an earlier version of this file labeled these commands as running from the repo root (`/`). Confirm the real location and delete this note.

```bash
./gradlew bootRun               # run app
./gradlew test                  # run tests
./gradlew clean build -x test   # build artifact
```

### Frontend (`/frontend`)

```bash
npm run dev          # run dev server
npx tsc --noEmit     # type-check
npm run build        # production build
```

## 5. Code Style, Conventions & Guardrails

- **Security Check:** Validate that every JPA finder method or query strictly binds to the authenticated User principal ID.
- **DTO Immutability:** Use Java `record` types for incoming request payloads and outgoing JSON structures.
- **Spring Boot 3 Conventions:** Use strictly `jakarta.persistence.*` namespaces instead of legacy `javax.*`. Use constructor-based dependency injection exclusively.
- **OpenAI key handling:** Inject the API key securely via `application.properties` from the `OPENAI_API_KEY` system environment variable. Never hardcode.
- **Keep root tooling minimal:** Don't add Lerna/Turborepo/Nx or an npm `workspaces` field unless explicitly asked — the `concurrently` glue is intentional, not an oversight.
- **Don't assume prerequisites exist:** No database, authentication, or frontend↔backend API contract exists _yet_ (only scaffolding is done — see §6). Any of that is new work, not a fix.

## 6. Feature Roadmap & Implementation Order

1. **[DONE] Project Scaffolding:** Root Spring Boot + Gradle wrapper, `/frontend` Vue 3 + TS Vite app.
2. **Authentication & Multi-Tenancy:** Secure all endpoints. A logged-in user must ONLY be able to CRUD their own data.
3. **Core CRUD Operations:** Goals and Learning Sessions tracking. Implement status-filtering for Goals.
4. **Resource Library:** Attaching learning references directly to specific Goals.
5. **OpenAI Integration:** Centralized service calling OpenAI Chat Completions API. (Key handling: see §5.)
6. **Dashboard Metrics:** Aggregation logic mapping total learning hours grouped per tag and grouped per week using JPQL or Criteria API.
7. **DevOps & CI/CD:** Native multi-stage `Dockerfile` compilation and GitHub Actions pipeline executing `./gradlew test` and `npm run test` on every push.

## 7. Git & Repository Preferences

- **Commit messages:** [Conventional Commits](https://www.conventionalcommits.org/) — `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`. Scope by app when it adds clarity: `feat(backend): ...`, `feat(frontend): ...`, `chore(root): ...`.
- **Branching:** Trunk-based — work directly on `main` or via short-lived branches merged back quickly. No long-lived feature branches, no release branches.

## 8. Don't

- Don't commit machine-specific config as if it were portable (e.g. `backend/gradle.properties` currently has a local Homebrew JDK path — flag it, don't copy it).
- Don't add CI/CD or Docker config speculatively ahead of the roadmap (§6.7) — none exists today.
