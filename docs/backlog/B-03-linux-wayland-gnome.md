---
id: B-03
title: "Linux, GNOME on Wayland: check that an AppFrame window moves and resizes, on the JetBrains Runtime and on Temurin"
status: question
priority: P2
size: M
stage: stage-native-behaviour
---

# B-03 — Linux, GNOME on Wayland: check that an AppFrame window moves and resizes, on the JetBrains Runtime and on Temurin

**Hypothesis, not observed.** Nothing on Linux has been checked beyond the screenshot goldens
([research §1.2](../research/research-architecture.md)), and GNOME's default session is Wayland. The
title bar's KDoc says that on Linux "Compose hands the move to the window manager"
(`appframe/src/desktopMain/kotlin/io/github/youndie/appframe/TitleBarDrag.kt:29-30`). The bytecode
says that holds only on the JetBrains Runtime: `WindowDraggableArea` uses `JbrMoveHandler` when
`JBR.isWindowMoveSupported()`, and otherwise `StandardMoveHandler`, which is `Window.setLocation`
from `MouseInfo`; the resize strip is `setSize`/`setLocation` on every runtime
(research §1.3, Consequence 3). The README asks only for Java 21, so Temurin is a supported runtime.

- **The check.** On a GNOME Wayland session, the demo with the default AWT toolkit (XWayland), once
  on a JetBrains Runtime 21+ and once on Temurin 21+: drag by the bar; resize from each of the four
  edges and a corner (the left and top ones move the window as well as size it); double-click to
  maximize and back; minimize; close. Record per runtime and gesture: works / does not / works badly
  (how). Also record whether the controls sit where GNOME's `button-layout` says
  (`LinuxDecorations.kt`).
- **Then** fix what is appframe's, file what is Compose's (as a minimal reproducer, the owner files
  it), and correct the KDoc sentence either way — it is wrong today on Temurin whatever Wayland does.
- Not this item: JBR's native Wayland toolkit (`-Dawt.toolkit.name=WLToolkit`), KDE, X11 sessions —
  recorded as not checked unless they come for free.

- AC: the item's Findings hold the table runtime × gesture for GNOME on Wayland with the GNOME and
  JDK versions; every "does not" is fixed, or has a reproducer and a written reason; the KDoc of
  `TitleBarDragArea` says what happens on a non-JBR runtime.
- Anchors: `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/TitleBarDrag.kt`,
  `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/LinuxDecorations.kt`,
  `app/src/desktopMain/kotlin/Main.kt`.

## Findings

2026-11-09.

- **The KDoc half is done** without a Wayland desktop, because it was wrong on Temurin whatever
  Wayland does. `TitleBarDragArea`'s KDoc now says that on Linux the move goes to the window manager
  only on the JetBrains Runtime, and that on any other JDK Compose moves the window by hand. Re-read
  from `foundation-desktop-1.12.1.jar` with `javap -c -p`: the gesture calls
  `WindowMoveHandler.startMovingTogetherWithMouse` right after `awaitFirstDown` (no slop), and
  `StandardMoveHandler.onDrag` is `Window.setLocation(start + pointer delta)` with no look at the
  placement — so on a non-JBR runtime a maximized window is dragged as it is, the same defect
  `youndie/appframe#61` fixed on Windows. That is read from bytecode, not seen on a desktop.
- **Not checked: the table.** No gesture was tried on GNOME, on Wayland or anywhere on Linux: the
  agent loop working this backlog has no GNOME desktop session to drive. No result
  is recorded for any runtime × gesture cell.

## Question

**Who runs the GNOME-on-Wayland check, and when?** It needs a GNOME Wayland session with a JetBrains
Runtime 21+ and a Temurin 21+, and `./gradlew :app:run` on each (the default AWT toolkit, so
XWayland); the gestures and what to record are in the item above.

The choices, for the owner:

1. **The owner runs it** (or names a machine with a GNOME Wayland session the loop may drive) and
   writes the table here; the item goes back to `open` and the loop takes the fixes and reproducers.
2. **Act on the bytecode alone** for the one defect it shows: route Linux on a non-JBR runtime
   through appframe's own drag (`dragWindow`, which restores a maximized window), as Windows already
   is — knowing it still needs the hand check before the item can close.
3. **Drop the Wayland half** for now, with the reason written here; the KDoc fix stands.
