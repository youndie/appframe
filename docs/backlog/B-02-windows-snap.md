---
id: B-02
title: "Windows 11: find out whether Snap Layouts and Aero Snap work on an AppFrame window, and act on the answer"
status: question
priority: P1
size: M
stage: stage-native-behaviour
---

# B-02 — Windows 11: find out whether Snap Layouts and Aero Snap work on an AppFrame window, and act on the answer

**Hypothesis, not observed.** A Windows 11 user's first gestures with any window are hovering the
maximize button for the Snap Layouts flyout and dragging the window to a screen edge to snap it.
Reading the code says an `AppFrame` window gets neither:

- the maximize button is a Compose `clickable` (`appframe/src/desktopMain/kotlin/io/github/youndie/appframe/WindowControls.kt:58-122`),
  and nothing in `appframe/src` answers `WM_NCHITTEST`. Microsoft's guide for custom title bars
  ("Support snap layouts for desktop apps on Windows 11", learn.microsoft.com, read 2026-11-09) asks
  a Win32 app to return `HTMAXBUTTON` for the maximize button for the flyout to appear;
- the window is moved by appframe itself, `setLocation` per pointer event
  (`appframe/src/desktopMain/kotlin/io/github/youndie/appframe/TitleBarDrag.kt:66-90`, chosen in
  `youndie/appframe#61`), not by the system's move loop, which is where edge snapping lives.

Research [§1.3, Consequence 4, H1](../research/research-architecture.md).

- **First the answer, then the decision.** On a Windows 11 host, with the demo (`./gradlew :app:run`):
  hover the maximize button; drag the window by its bar to the left, right and top edges; press
  Win+Left/Right/Up. Record per gesture: works / does not. Win+arrow is in the list because it is a
  system shortcut that does not depend on the bar, and separates "snapping is off for this window"
  from "our drag bypasses it".
- **If "no" — make it work, or write down why not.** Routes worth reading before choosing (none
  verified): a window procedure hooked through JNA that answers `WM_NCHITTEST` with `HTCAPTION` for
  the bar and `HTMAXBUTTON` for the button, which would hand both the move and the flyout to the
  system and might retire `TitleBarDrag.kt`; the JetBrains Runtime's custom title bar API, which
  would cover only apps running on JBR. A refusal is a valid outcome if its reason is written —
  for example, a new native dependency the library does not want.
- Not this item: macOS window tiling, Linux edge tiling ([B-03](B-03-linux-wayland-gnome.md)),
  multiple monitors ([B-05](B-05-multi-monitor-on-windows.md)).

- AC: the item's Findings record, for Windows 11 with the version and scaling, the result of each
  gesture above on an `AppFrame` window; for every "no", either a merged change after which it
  works, checked the same way, or a written refusal with its reason, and the README says which.
- Anchors: `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/TitleBarDrag.kt`,
  `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/WindowControls.kt`,
  `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/AppFrame.kt`.

## Findings

2026-11-09, at `4a0fc01`: no `WM_NCHITTEST`, `HTMAXBUTTON`, `HTCAPTION`, window-procedure hook or
JNA dependency anywhere in `appframe/src`, `appframe/build.gradle.kts` or the version catalog — the
reading the hypothesis rests on still holds. Nothing beyond that was checked: the item's first step
is a person at a Windows 11 desktop, and the agent loop working this backlog has no interactive
Windows session. It did not try the gestures and does not record a result for any of them.

## Question

**Who runs the Windows 11 check, and when?** The gestures (hover the maximize button; drag the bar
to the left, right and top edges; Win+Left/Right/Up) need someone at the desktop with `./gradlew
:app:run`; the result per gesture, with the Windows build and display scaling, goes into Findings
above. [B-05](B-05-multi-monitor-on-windows.md) (two monitors) and the Windows half of
[B-04](B-04-document-the-resize-border.md) can share that sitting.

The choices, for the owner:

1. **The owner runs it** and writes the results here (or in a comment the loop copies in); the item
   goes back to `open` and the loop takes the decision half — make it work, or a written refusal.
2. **Skip the observation** and decide from the code reading: start on the `WM_NCHITTEST` route as
   if the answer were "no", knowing that a merged change still needs the same hand check before the
   item can close.
3. **Drop it** for now, with the reason written here.
