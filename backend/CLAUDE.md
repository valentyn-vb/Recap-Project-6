# Backend (Spring Boot)

## 1. Project Overview

- **Stack**: Spring Boot 4.1.0, Java 21 (via Gradle toolchain), Gradle 9.5.1 wrapper.
- **Package**: `com.example.demo` (group `com.example`, version `0.0.1-SNAPSHOT`).
- **Architecture style**: REST API skeleton, intended layered architecture
  (`controller` → `service` → `repository`) once real endpoints are added. Today it's
  just `DemoApplication` — a bare `@SpringBootApplication` with no controllers,
  services, or repositories yet.
- **Dependencies present**: `spring-boot-starter`, `spring-boot-starter-web`,
  `spring-boot-starter-test` + JUnit 5 platform launcher. No persistence, no security
  starter.

## 2. Build & Development Commands

```bash
./gradlew bootRun   # start the app on http://localhost:8080
./gradlew test       # JUnit 5 (useJUnitPlatform is already configured)
./gradlew build
./gradlew clean
```

Run `./gradlew test` before considering a backend change done.

## 3. Code Style & Conventions

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

- Don't hardcode secrets or credentials in `application.properties`.
- Don't commit personal machine paths. `gradle.properties` currently has
  `org.gradle.java.installations.paths=/opt/homebrew/opt/openjdk@21` — that's
  machine-specific; treat it as something to flag, not to extend.
- Don't add a database or security layer speculatively — only when asked or when the
  feature genuinely requires it.
