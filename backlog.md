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
| `stage-custom-frames` | Custom frames | What a design that is not Material's needs from the frame: drawing under the bar, leaving parts of it out, while the controls, the drag and the resize stay appframe's. |
| `stage-outreach` | Outreach | Telling people. The owner's decisions only. |

## Order of work

1. [B-01](docs/backlog/B-01-changelog-and-upgrade-notes.md) first: the next release needs the place
   its notes go, and 0.3.0's binary break is unrecorded.
2. [B-07](docs/backlog/B-07-windows-hand-check.md): every Windows observation in one sitting, by
   the owner. [B-02](docs/backlog/B-02-windows-snap.md) and
   [B-05](docs/backlog/B-05-multi-monitor-on-windows.md) are blocked by it and take the decision
   half after; the Windows rows of [B-04](docs/backlog/B-04-document-the-resize-border.md) come from
   it.
3. [B-03](docs/backlog/B-03-linux-wayland-gnome.md) needs a GNOME Wayland desktop.
4. [B-06](docs/backlog/B-06-announce-0-3-0.md) is closed: nothing goes to jetc.dev and no post for
   now (the owner, 2026-11-09).
5. [B-08](docs/backlog/B-08-background-under-the-title-bar.md) came from a caller's design and
   depends on nothing above; it goes out as a `0.3.1.<run>` build.

## Marks

`[ ]` open · `[~]` in progress · `[x]` done · `[?]` open question · `[-]` dropped

<!-- BEGIN INDEX -->

## Open (6)

| Task | | Priority | Size | Blocked by |
|---|---|---|---|---|
| [B-02](docs/backlog/B-02-windows-snap.md) `[ ]` | Windows 11: find out whether Snap Layouts and Aero Snap work on an AppFrame window, and act on the answer | P1 | M | B-07 |
| [B-07](docs/backlog/B-07-windows-hand-check.md) `[?]` | Owner: one sitting at Windows 11 — snapping, two monitors and the edge scrollbar on an AppFrame window, checked by hand | P1 | S | - |
| [B-08](docs/backlog/B-08-background-under-the-title-bar.md) `[~]` | Let a caller draw behind the whole frame, title bar included, and keep the window's title out of the bar | P1 | M | - |
| [B-03](docs/backlog/B-03-linux-wayland-gnome.md) `[?]` | Linux, GNOME on Wayland: check that an AppFrame window moves and resizes, on the JetBrains Runtime and on Temurin | P2 | M | - |
| [B-04](docs/backlog/B-04-document-the-resize-border.md) `[?]` | Tell README readers about the resize strip and resizeBorder, and check by hand that a thinner border frees an edge scrollbar | P2 | S | - |
| [B-05](docs/backlog/B-05-multi-monitor-on-windows.md) `[ ]` | Windows with two monitors: check the README's multi-monitor claim, or narrow it | P2 | S | B-07 |

## Closed (2)

**First contact**

- [B-01](docs/backlog/B-01-changelog-and-upgrade-notes.md) `[x]` - Keep a CHANGELOG, and say in it how to move from 0.2.0 to 0.3.0 — a source and a binary break

**Outreach**

- [B-06](docs/backlog/B-06-announce-0-3-0.md) `[x]` - Owner: announce 0.3.0 — a post, and whether to send it to jetc.dev again

<!-- END INDEX -->
