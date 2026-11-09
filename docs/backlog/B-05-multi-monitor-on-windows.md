---
id: B-05
title: "Windows with two monitors: check the README's multi-monitor claim, or narrow it"
status: open
priority: P2
size: S
stage: stage-native-behaviour
---

# B-05 — Windows with two monitors: check the README's multi-monitor claim, or narrow it

The README's feature list says maximize "respects the taskbar/dock and multi-monitor setups"
(`README.md:24-25`). On Windows the work-area half is appframe's own code — `Frame.maximizedBounds`
kept on the work area of the monitor the window is on, refreshed on every move
(`appframe/src/desktopMain/kotlin/io/github/youndie/appframe/WorkArea.kt:34-59`) — and both pull
requests that built it say "Not verified: multiple monitors" (`youndie/appframe#60`,
`youndie/appframe#61`: "dragging a maximized window onto another monitor"). `WorkAreaTest.kt` checks
the rectangle arithmetic only. Research §1.2, Consequence 2, H3.

- **The check**, Windows 11 with two monitors, ideally with different scaling and the taskbar on
  only one: maximize on each monitor (button, double click); drag a maximized window from one monitor
  to the other (it restores under the pointer, #61) and maximize it there; move a floating window
  across and maximize. Record bounds against each monitor's work area, as #60 did (`GetWindowRect`,
  `IsZoomed`, or AWT's `bounds`).
- **Then** fix what is wrong, or narrow the README sentence to what was checked. Can share a session
  with [B-02](B-02-windows-snap.md).
- Not this item: macOS and Linux multi-monitor (the system window manager maximizes there,
  `WorkArea.kt:13-16`).

- AC: the item's Findings record the result of each move above with monitor sizes and scaling; the
  README's multi-monitor claim is either backed by them or narrowed.
- Anchors: `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/WorkArea.kt`,
  `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/TitleBarDrag.kt`, `README.md`.
