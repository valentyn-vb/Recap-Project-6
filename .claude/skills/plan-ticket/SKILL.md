---
name: plan-ticket
description: >-
  Plan the implementation of a specific GitHub issue in plan mode: read the
  issue and repo docs, produce a TDD plan with commit boundaries, and hard-stop
  for developer confirmation. Use when the user says "plan issue #N" or as the
  planning step of the issue-to-pr loop.
---

# plan-ticket

Produce a confirmed implementation plan for one issue. Input: an issue number (and ideally its loaded body — run [`next-ticket`](../next-ticket/SKILL.md) first if no issue is selected yet). Output: a developer-approved plan. No implementation code is written here.

## Mode

- Call `EnterPlanMode`. Do not write implementation code while in plan mode.
- Codebase exploration that feeds the plan can be delegated to an `Agent` call with `subagent_type: "Explore"` (or `"general-purpose"`).

## Steps

1. Load the issue if not already in context: `gh issue view <number> --json title,body,number,url`. Read title, body, acceptance criteria, tasks.
2. Read repo context: root `CLAUDE.md`, and `backend/CLAUDE.md` / `frontend/CLAUDE.md` for whichever app(s) the issue touches.
3. Draft a plan covering:
   - Scope and files to touch
   - TDD steps (failing test → implement → green) per root `CLAUDE.md`
   - Test commands per app (`backend/`: `./gradlew test`; `frontend/`: `npm run test`, `npx vue-tsc -b`)
   - Logical commit boundaries (one commit per coherent step)
4. **Hard stop:** call `ExitPlanMode` with the plan and wait for explicit developer confirmation ("confirmed", "looks good", "go ahead", etc.). Do not implement until confirmed.
5. If rejected, revise the plan and re-present via `ExitPlanMode`.

## Guardrails

- Do not implement in plan mode.
- Do not skip developer plan confirmation — even when invoked from the issue-to-pr loop, each ticket's plan needs its own confirmation.
