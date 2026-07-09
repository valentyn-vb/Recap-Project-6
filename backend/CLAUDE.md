# Backend (Spring Boot)

## 1. Project Overview

- **Stack**: Spring Boot 4.1.0, Java 21 (via Gradle toolchain), Gradle 9.5.1 wrapper.
- **Package**: `com.example.demo` (group `com.example`, version `0.0.1-SNAPSHOT`).
- **Architecture style**: layered REST API (`@RestController` → `@Service` →
  `@Repository`), packages by domain under `com.example.demo`: `user/`, `auth/`,
  `profile/`, `security/` (+ `security/jwt/`), `web/` (exception handling).
- **Dependencies present**: web, data-jpa, security, validation starters; jjwt
  (JWT sign/verify); Postgres driver. Tests: starter-test, data-jpa-test,
  webmvc-test, spring-boot-testcontainers + Testcontainers postgresql/junit-jupiter.
- **Auth model**: stateless JWT (`Authorization: Bearer`). `/api/auth/**` is public
  (register/login); everything else returns 401 without a valid token.
  `JwtAuthenticationFilter` sets an `AuthenticatedUser(userId, email)` principal.
  **Multi-tenancy rule**: services take the user id from that principal
  (`@AuthenticationPrincipal` or `CurrentUserService`) — never from the request —
  and every repository query for user-owned data is scoped to it
  (see `ProfileService`/`ProfileController` as the reference pattern).

## 2. Build & Development Commands

```bash
./gradlew bootRun   # start the app on http://localhost:8080
./gradlew test       # JUnit 5 (useJUnitPlatform is already configured)
./gradlew build
./gradlew clean
```

Run `./gradlew test` before considering a backend change done.

### Local dev setup

- **Database (bootRun only)**: `docker compose up -d` in `backend/` starts Postgres 18
  on host port **5433** (5432 is taken by another container on this machine).
  Credentials default to `learning_companion` all around; override via `.env`
  (gitignored — see `.env.example`).
- **`JWT_SECRET` is required for `bootRun`** (no fallback by design):
  `JWT_SECRET=$(openssl rand -base64 48) ./gradlew bootRun`.
- **Tests need no setup**: `./gradlew test` boots its own throwaway Postgres via
  Testcontainers (Docker must be running) and uses the `test` profile
  (`src/test/resources/application-test.properties`) for the JWT secret.
- Schema is managed by `ddl-auto=update` for now — a known dev simplification until
  a migration tool (Flyway/Liquibase) is introduced.

## 3. Code Style & Conventions

- **TDD (see root `CLAUDE.md`)**: tests live under `src/test/java/com/example/demo`,
  mirroring the package structure under `src/main/java/com/example/demo`. Cycle:
  write/extend a JUnit 5 test → `./gradlew test` (expect failure) → implement →
  `./gradlew test` (expect pass) → commit. JUnit 5 + `useJUnitPlatform()` is already
  configured in `build.gradle` — no tooling setup needed to start.
- **Language rules**: Java 21. Prefer Java records for DTOs once endpoints exist.
- **Architectural guardrails**: Constructor injection only — no field-level
  `@Autowired` — once beans are introduced.
- **Naming conventions**: PascalCase for classes (`DemoApplication`), camelCase for
  methods/variables, lowercase no-underscore packages under `com.example.demo`.
- Place new code under `com.example.demo`. If the domain outgrows that placeholder
  package/name (`rootProject.name = 'demo'` in `settings.gradle`), agree on a rename
  with the user rather than renaming it unilaterally.
- Add dependencies deliberately and only when actually needed — e.g. pull in Spring
  Data JPA + a driver when persistence work actually starts, not ahead of it.

## 4. Repository & Git Preferences

- **Commit messages**: Conventional Commits scoped to this app —
  `feat(backend): ...`, `fix(backend): ...`, `chore(backend): ...`.
- **Branching**: Trunk-based on `main`, consistent with the rest of the monorepo (see
  root `CLAUDE.md`).

## Don't

- Don't hardcode secrets in `application.properties` — `JWT_SECRET` deliberately has
  no default. (The datasource defaults mirror the committed docker-compose dev
  credentials and are not secrets.)
- Don't commit personal machine paths. `gradle.properties` currently has
  `org.gradle.java.installations.paths=/opt/homebrew/opt/openjdk@21` — that's
  machine-specific; treat it as something to flag, not to extend.
- Don't take a user id from a path/query/body for user-owned data — always resolve it
  from the authenticated principal (see Multi-tenancy rule above).
