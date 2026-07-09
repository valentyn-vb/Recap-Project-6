# Frontend (Vue 3 + Vite + TypeScript)

## 1. Project Overview

- **Stack**: Vue 3.5.39, Vite 8.1.1, TypeScript ~6.0.2, `vue-tsc` 3.3.5,
  `@vue/tsconfig` 0.9.1, `@vitejs/plugin-vue` 6.0.7.
- **Architecture style**: Composition API with `<script setup lang="ts">`. Currently
  the unmodified `npm create vite -- --template vue-ts` scaffold (`HelloWorld.vue`
  counter/hero boilerplate) — no router, no state management, no UI library, no HTTP
  client, no ESLint/Prettier, no test runner, and no dev-server proxy to the backend.
- **TS project layout**: `tsconfig.json` is a references-only root pointing at
  `tsconfig.app.json` (app code, DOM lib, `src/**/*.{ts,tsx,vue}`) and
  `tsconfig.node.json` (Node-context files like `vite.config.ts`). This is the
  standard split used by the official template — keep new config in the file that
  matches its context rather than merging them back into one.

## 2. Build & Development Commands

```bash
npm install
npm run dev       # Vite dev server, http://localhost:5173
npm run build     # vue-tsc -b (type-check) && vite build
npm run preview
```

There's no separate `type-check`/`lint` script — `npm run build` is currently the
only thing that runs `vue-tsc`. Run it (or `npx vue-tsc -b` directly) before
considering a change done if you're not already running a full build.

## 3. Code Style & Conventions

- **Language rules**: Use `<script setup lang="ts">` for every component — match the
  existing style in `App.vue` / `HelloWorld.vue`. Type new code properly (props,
  emits, function signatures); avoid `any` — reach for `unknown` + narrowing instead
  when a type genuinely isn't known.
- **Architectural guardrails**: Before adding a router, state library, UI kit, or HTTP
  client, confirm the choice with the user — none exist yet, so whatever gets picked
  first sets the pattern for the rest of the app. If it's a library with its own Vue
  types (vue-router, Pinia, etc.) that's an argument in favor now that the project is
  typed. If the frontend needs to call the backend, add explicit wiring for it (Vite
  `server.proxy` or a full URL + CORS on the backend side) — it doesn't exist today.
- **Naming conventions**: PascalCase for component files under `src/components/`
  (`HelloWorld.vue`), camelCase for variables/functions, kebab-case for CSS classes.

## 4. Repository & Git Preferences

- **Commit messages**: Conventional Commits scoped to this app —
  `feat(frontend): ...`, `fix(frontend): ...`, `chore(frontend): ...`.
- **Branching**: Trunk-based on `main`, consistent with the rest of the monorepo (see
  root `CLAUDE.md`).

## Don't

- Don't add ESLint/Prettier config unless asked — none is configured today.
- Don't loosen type safety project-wide (e.g. `"strict": false`, blanket
  `// @ts-ignore`) to make an error disappear — fix the underlying type instead.
- Don't leave unrelated default Vite/Vue boilerplate (hero image, counter demo) mixed
  in once real UI work starts — clean it up as part of that work, not ahead of it.
