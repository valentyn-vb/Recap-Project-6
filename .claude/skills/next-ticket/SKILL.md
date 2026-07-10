---
name: next-ticket
description: >-
  Discover the next open GitHub issue with Project Status "Todo" on the
  valentyn-vb/Recap-Project-6 board, check its dependency notes, and load its
  details. Use when the user asks "what's next on the board", "pick up the next
  ticket", or as the discovery step of the issue-to-pr loop.
---

# next-ticket

Find the next workable Todo issue on the project board and load it. This skill only **selects and loads** an issue — it does not branch, plan, or implement (see [`plan-ticket`](../plan-ticket/SKILL.md) and [`implement-ticket`](../implement-ticket/SKILL.md) for those).

Repo: `valentyn-vb/Recap-Project-6`. GraphQL queries and `gh` examples: [github-queries.md](github-queries.md).

## Prerequisites

Verify `gh` is authenticated and has project read scope:

```bash
gh auth status
gh auth refresh -s read:project   # one-time if GraphQL returns INSUFFICIENT_SCOPES
```

## Steps

1. Run the **list Todo issues** GraphQL query from [github-queries.md](github-queries.md).
2. Filter to issues where Project **Status** = `Todo` and issue **state** = `OPEN`.
3. If none found, stop and tell the developer the board has no Todo issues.
4. If multiple, present a numbered list (`#<number> — <title>`) and ask which to take. If the developer says "just pick one" (or this runs inside the issue-to-pr loop), take the lowest issue number.
5. Load the full issue:

   ```bash
   gh issue view <number> --json title,body,number,url,state
   ```

6. **Dependency check:** scan the issue body for prerequisite tickets (e.g. "do not start until ticket #3 is status:done"). For each referenced issue number, run the **get issue project status** query from [github-queries.md](github-queries.md). If any prerequisite is not `Done`, report which blocker(s) remain and — if other Todo issues exist — offer the next unblocked one instead.

## Output

End with a short summary the next step can consume: issue number, title, URL, and the acceptance criteria / tasks from the body. State explicitly that the issue is unblocked (or which blockers were found).

## Guardrails

- Do not select issues whose Project Status is not `Todo` unless the developer explicitly overrides.
- If an issue has no project link, treat its status as unknown and ask the developer.
