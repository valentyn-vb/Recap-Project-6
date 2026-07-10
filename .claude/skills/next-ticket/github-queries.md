# GitHub queries for next-ticket

Requires `gh` CLI authenticated to `valentyn-vb/Recap-Project-6` with `read:project` scope:

```bash
gh auth refresh -s read:project
```

If any query returns `INSUFFICIENT_SCOPES`, run the command above and retry.

**Validation note:** GraphQL project-status queries require `read:project` and cannot be exercised non-interactively until the developer runs `gh auth refresh -s read:project`. The `gh issue view` command works with the default `repo` scope.

---

## List open issues with Project Status = Todo

Returns all open issues linked to a GitHub Project where the **Status** single-select field equals **`Todo`**.

```bash
gh api graphql -f query='
query($owner: String!, $repo: String!) {
  repository(owner: $owner, name: $repo) {
    issues(first: 50, states: OPEN, orderBy: { field: CREATED_AT, direction: ASC }) {
      nodes {
        number
        title
        url
        projectItems(first: 10) {
          nodes {
            project {
              title
            }
            fieldValues(first: 20) {
              nodes {
                ... on ProjectV2ItemFieldSingleSelectValue {
                  name
                  field {
                    ... on ProjectV2SingleSelectField {
                      name
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}' -f owner=valentyn-vb -f repo=Recap-Project-6
```

**Filter in agent logic:** keep issues where any `projectItems` entry has a `fieldValues` node with `field.name == "Status"` and `name == "Todo"`.

**Pretty-print with jq** (optional):

```bash
gh api graphql -f query='...' -f owner=valentyn-vb -f repo=Recap-Project-6 \
  | jq '[.data.repository.issues.nodes[]
    | select(.projectItems.nodes[]
      | .fieldValues.nodes[]
      | select(.field.name == "Status" and .name == "Todo"))
    | {number, title, url}]'
```

---

## Get project Status for a single issue

Use this for dependency checks (e.g. "ticket #3 must be status:done").

```bash
gh api graphql -f query='
query($owner: String!, $repo: String!, $number: Int!) {
  repository(owner: $owner, name: $repo) {
    issue(number: $number) {
      number
      title
      state
      projectItems(first: 10) {
        nodes {
          project {
            title
          }
          fieldValues(first: 20) {
            nodes {
              ... on ProjectV2ItemFieldSingleSelectValue {
                name
                field {
                  ... on ProjectV2SingleSelectField {
                    name
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}' -f owner=valentyn-vb -f repo=Recap-Project-6 -F number=<issue-number>
```

**Extract Status:** find `fieldValues` where `field.name == "Status"`; the `name` field is the status value (`Todo`, `In Progress`, `Done`, etc.).

If an issue has no project link, treat status as unknown and ask the developer.

---

For the `gh issue view` command see [SKILL.md](SKILL.md); for `gh pr create` see the [`open-pr`](../open-pr/SKILL.md) skill — each command lives in one place only, so there's one place to update.

---

## Troubleshooting

| Error | Fix |
|-------|-----|
| `INSUFFICIENT_SCOPES` / missing `read:project` | `gh auth refresh -s read:project` |
| Empty `projectItems` | Issue not added to the GitHub Project — ask developer to add it |
| Status value mismatch | Confirm exact casing in the Project board (`Todo` vs `To do`) and update the filter in Phase 1 if needed |
| `gh: not found` | Install GitHub CLI: https://cli.github.com |
