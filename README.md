# Recap Project 6

Monorepo with a Spring Boot backend and a Vue 3 frontend.

## Layout

- `backend/` — Spring Boot (Java 21, Gradle) REST API
- `frontend/` — Vue 3 app (Vite)

## Prerequisites

- JDK 21
- Node.js (LTS)

## Setup

```bash
npm install                    # installs root dev tooling (concurrently)
npm install --prefix frontend  # installs frontend dependencies
```

## Running

Start both apps together:

```bash
npm run dev
```

Or individually:

```bash
npm run dev:frontend   # Vite dev server
npm run dev:backend    # Spring Boot on http://localhost:8080
```
