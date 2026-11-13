---
id: B-08
title: "Let a caller draw behind the whole frame, title bar included, and keep the window's title out of the bar"
status: wip
priority: P1
size: M
stage: stage-custom-frames
---

# B-08 — Let a caller draw behind the whole frame, title bar included, and keep the window's title out of the bar

`AppFrame` lays its window out as a `Column` of the title bar and the caller's content
(`appframe/src/desktopMain/kotlin/io/github/youndie/appframe/AppFrame.kt`, `AppFrameContent`), and
the bar is a Material `Surface` filled with `MaterialTheme.colorScheme.surfaceVariant`
(`appframe/src/desktopMain/kotlin/io/github/youndie/appframe/TitleBar.kt`). A design whose window
background — a texture, a gradient, an image — runs under the title bar cannot be drawn: the bar
paints a flat strip over it, and the content starts below the bar, so nothing the caller draws can
sit behind it. A theme can recolour the strip, not make it show what is under it. The bar also always
draws `title`. A design with no text in the bar can pass `title = ""`, but that takes the title away
from the system too — the Dock, the taskbar, the window switcher — because `AppFrame` hands the same
string to the `Window`.

The case that raised it: a desktop app on appframe whose design puts a textured stage under a bar
that carries only the window controls and one action button. Today it bridges the bar's colours
through a `MaterialTheme`, and the bar shows as a flat strip without the texture, and with a title
the design does not have.

- **The decision and its reason.** Two parameters on `AppFrame`, both doing nothing by default.
  `background: (@Composable BoxScope.() -> Unit)?` is drawn under the whole frame, bar included and
  inside the frame's rounded clip, and while it is set the bar draws no surface of its own. A slot
  rather than a bar colour: a transparent bar alone would show what is behind the frame's column,
  which is nothing the caller draws; the slot is the one place the caller can draw that the bar sits
  over. A slot rather than letting the content run under the bar: that would move every existing
  layout up by the bar's height, or need a flag and an inset, and it would change what the column
  behind `AppFrameScope` holds. `showTitle: Boolean = true` keeps `title` for the window and, when
  false, draws none in the bar.
- **Where they go, and the bytecode.** After `actions`, before `content`, so every call that
  compiles against 0.3.0 with a trailing `content` still compiles. A new parameter does not rename
  `AppFrame`, but it changes its JVM descriptor, which is the 0.2.0 → 0.3.0 break again
  ([research §1.4](../research/research-architecture.md)); so the 0.3.0 signature stays, as a hidden
  overload that calls the new one, and code compiled against 0.3.0 keeps linking.
- The alternatives rejected: a field on `TitleBarStyle` — a data class, whose constructor and `copy`
  change their descriptors with every new field, and a style cannot hold a composable anyway; a
  `titleBarColor` on `AppFrame` without a slot, for the reason above.
- Not this item: the bar's content colour, which still comes from
  `MaterialTheme.colorScheme.onSurfaceVariant` (controls included); `TitleBar`'s own parameters — a
  hand-rolled frame already passes `color` and `title` itself; an ABI gate in `check`.

- AC: with a `background` its pixels show through the bar (a test); over a background a double click
  on the bar still toggles the window, the close button still closes it, and the background receives
  none of the bar's presses (a test); with neither parameter the frame draws, pixel for pixel, what
  0.3.0's `Column` of the bar and the content drew, for every built-in style, with menus and without
  (a test); `showTitle = false` draws no title while the window keeps it; a golden of the frame over a
  background; `javap` shows the 0.3.0 `AppFrame` method still in the jar; the README and the
  CHANGELOG's `Unreleased` say how a caller uses it; the sample has a toggle for it; a snapshot
  published.
- Anchors: `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/AppFrame.kt`,
  `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/TitleBar.kt`,
  `appframe/src/desktopTest/kotlin/io/github/youndie/appframe/TitleBarScreenshots.kt`,
  `app/src/desktopMain/kotlin/Main.kt`, `README.md`, `CHANGELOG.md`.
