# Backlog: appframe as the window frame a Compose Desktop app reaches for first

> Role of this document: the product backlog. **One file per item in
> [`docs/backlog/`](docs/backlog/)**, named `B-NN-<slug>.md`. What lives here is the index (generated)
> and everything that is not an item: the goal, the stages, and the order of work.
>
> New item: copy [`docs/templates/backlog-item.md`](docs/templates/backlog-item.md), take the next
> free `B-NN`, and run `make fix` after editing; `make check` before pushing.

## Goal

Make appframe the default frame for a Compose Desktop window by closing what a newcomer notices
first: what changed between versions and how to upgrade, the gestures every native window answers
(snapping, edge resize, multiple monitors), and the desktops nobody has tried it on. The
[research](docs/research/research-architecture.md) separates what was checked by hand from what was
only read in the code; most items here start by turning a hypothesis into an observation.

## Stages

A stage is a field on the item, not a directory.

| Stage id | Stage | What it is |
|---|---|---|
| `stage-first-contact` | First contact | What a newcomer reads before and while upgrading: the changelog, the upgrade notes, the README. |
| `stage-native-behaviour` | Native behaviour | What a system frame gives a window for free, checked by hand on the desktop in question, then made to work or refused in writing. |
| `stage-outreach` | Outreach | Telling people. The owner's decisions only. |

## Order of work

1. [B-01](docs/backlog/B-01-changelog-and-upgrade-notes.md) first: the next release needs the place
   its notes go, and 0.3.0's binary break is unrecorded.
2. [B-02](docs/backlog/B-02-windows-snap.md) and [B-05](docs/backlog/B-05-multi-monitor-on-windows.md)
   in one Windows session; [B-04](docs/backlog/B-04-document-the-resize-border.md)'s Windows half can
   join it.
3. [B-03](docs/backlog/B-03-linux-wayland-gnome.md) needs a GNOME Wayland desktop.
4. [B-06](docs/backlog/B-06-announce-0-3-0.md) waits on the owner.

## Marks

`[ ]` open · `[~]` in progress · `[x]` done · `[?]` open question · `[-]` dropped

<!-- BEGIN INDEX -->

## Open (5)

| Task | | Priority | Size | Blocked by |
|---|---|---|---|---|
| [B-02](docs/backlog/B-02-windows-snap.md) `[?]` | Windows 11: find out whether Snap Layouts and Aero Snap work on an AppFrame window, and act on the answer | P1 | M | - |
| [B-03](docs/backlog/B-03-linux-wayland-gnome.md) `[?]` | Linux, GNOME on Wayland: check that an AppFrame window moves and resizes, on the JetBrains Runtime and on Temurin | P2 | M | - |
| [B-04](docs/backlog/B-04-document-the-resize-border.md) `[?]` | Tell README readers about the resize strip and resizeBorder, and check by hand that a thinner border frees an edge scrollbar | P2 | S | - |
| [B-05](docs/backlog/B-05-multi-monitor-on-windows.md) `[ ]` | Windows with two monitors: check the README's multi-monitor claim, or narrow it | P2 | S | - |
| [B-06](docs/backlog/B-06-announce-0-3-0.md) `[?]` | Owner: announce 0.3.0 — a post, and whether to send it to jetc.dev again | P3 | XS | - |

## Closed (1)

**First contact**

- [B-01](docs/backlog/B-01-changelog-and-upgrade-notes.md) `[x]` - Keep a CHANGELOG, and say in it how to move from 0.2.0 to 0.3.0 — a source and a binary break

<!-- END INDEX -->
