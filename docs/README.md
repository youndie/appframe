# docs — appframe

A window frame with a Compose-drawn title bar for Compose Multiplatform desktop applications. The
documentation is layered; links run top to bottom.

```
[ Research (what the library is; what was checked by hand, where; what is a hypothesis) ]
                     │
[ Backlog (what to do next, one file per item) ]
```

| Layer | Directory | Answers | Source of truth |
|---|---|---|---|
| Research | `research/` | what the library is and why it is built this way; what is verified, what is a hypothesis | the artefacts and pull requests each fact names |

There is **no `features/`, `screens/`, `api/` or `services/` layer** yet: appframe is one published
module and a demo, its public surface is Kotlin, and the root [README](../README.md) is the user
guide. A layer is added when a document has something to say that the README and the KDoc do not.

**Backlog** — [backlog.md](../backlog.md): the index and the order of work; the items themselves are
one file each in [`backlog/`](backlog/), cited as `[B-01](backlog/B-01-changelog-and-upgrade-notes.md)`.

## Conventions

- **`id`** in the frontmatter is unique and equals the filename.
- Every document carries code anchors — paths to the source file — so that the reader reaches the
  code in one hop. Do not copy signatures; give the path.
- A behaviour of a window is a fact only with the desktop it was seen on and the pull request or
  item that records it. Anything else is a hypothesis and says so.
- Language: English. Code identifiers verbatim as in the code.

## Templates

`templates/` holds a copy of the document templates, so the format travels with the repository.

## Checks

The repository's Makefile runs docs-bootstrap's checks at the version `.github/workflows/check.yaml`
pins, and CI runs the same target:

```bash
pip install pyyaml
make check      # the gate, then the reports: what CI runs
make fix        # regenerate the backlog index, append missing coverage-map lines
```

## Coverage map

The list below is **checked** against the files on disk: a document missing here, or an entry with
no file behind it, fails the check.

### Research (1)

- [x] [research-architecture](research/research-architecture.md) — modules and platforms, what was checked by hand on which desktop, how the window is moved and resized, the 0.3.0 signature change, hypotheses with their items
