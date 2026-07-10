---
name: issue-to-pr
description: >-
  Loop over the GitHub project board: pick up the next Todo issue and drive it
  through the step skills — next-ticket → plan-ticket → implement-ticket →
  open-pr — then repeat until the board is empty or the developer stops. Use
  when the user says "work on issues", "work through the board", "issue to PR",
  or wants the GitHub-issue-driven development loop.
disable-model-invocation: true
---

# issue-to-pr

Orchestrator loop. Each pipeline step lives in its own skill — this skill only sequences them and decides when to loop. Invoke each step with the **Skill tool** (do not inline the step's work here; the step skill owns its instructions):

| Step | Skill | Produces |
|------|-------|----------|
| 1. Discover | [`next-ticket`](../next-ticket/SKILL.md) | selected, unblocked Todo issue |
| 2. Plan | [`plan-ticket`](../plan-ticket/SKILL.md) | developer-confirmed plan (hard stop) |
| 3. Implement | [`implement-ticket`](../implement-ticket/SKILL.md) | feature branch, green logical commits |
| 4. Pull request | [`open-pr`](../open-pr/SKILL.md) | PR URL for review |

Repo: `valentyn-vb/Recap-Project-6`. Stack docs: root `CLAUDE.md`, `backend/CLAUDE.md`, `frontend/CLAUDE.md`.

## Loop

```
- [ ] Skill: next-ticket   → if board has no workable Todo issue: report and END
- [ ] Skill: plan-ticket   → wait for developer confirmation (never skip)
- [ ] Skill: implement-ticket
- [ ] Skill: open-pr       → report PR URL
- [ ] Loop decision        → next iteration or END
```

**Loop decision** after each PR:

- If the developer asked for a single ticket ("pick up a ticket"), end and report.
- If the developer asked to work through the board ("work on issues", "keep going"), start the next iteration with `next-ticket` — but first return to a clean state: `git checkout main && git pull origin main` (each iteration branches fresh from `main`; PRs are reviewed and merged by the developer, so a later ticket that depends on an unmerged PR will surface as a blocked dependency in `next-ticket`).
- Always end when `next-ticket` finds no workable issue, or the developer says stop.

## Rules

- The plan confirmation inside `plan-ticket` is a hard stop **every iteration** — the loop never makes it autonomous.
- One issue = one branch = one PR. Never batch several issues into one branch.
- If a step skill fails or aborts (dirty tree, blocked dependency, red gate), surface its report and ask the developer how to proceed rather than silently skipping to the next ticket.
