---
id: B-07
title: "Owner: one sitting at Windows 11 — snapping, two monitors and the edge scrollbar on an AppFrame window, checked by hand"
status: question
priority: P1
size: S
stage: stage-native-behaviour
---

# B-07 — Owner: one sitting at Windows 11 — snapping, two monitors and the edge scrollbar on an AppFrame window, checked by hand

**A task for a person at the desktop, not for an agent.** Every Windows observation the backlog is
waiting for, gathered so it can be checked on its own, in one sitting: the agent loop has no
interactive Windows session and does not record a result it did not see (owner's decision,
2026-11-09). [B-02](B-02-windows-snap.md) and [B-05](B-05-multi-monitor-on-windows.md) are
`blocked_by` this item and take the decision half once it is done; the Windows half of
[B-04](B-04-document-the-resize-border.md) is here too.

Setup: Windows 11, ideally two monitors with different scaling and the taskbar on only one;
`./gradlew :app:run` from `main`. Write down first: the Windows build (`winver`), each monitor's
resolution and scaling, where the taskbar is.

1. **Snapping** (for B-02). Hover the maximize button — does the Snap Layouts flyout appear? Drag
   the window by its bar to the left edge, the right edge, the top edge. Press Win+Left, Win+Right,
   Win+Up.
2. **Two monitors** (for B-05). Maximize on each monitor, by the button and by a double click on the
   bar. Drag a maximized window from one monitor to the other (it should restore under the pointer)
   and maximize it there. Move a floating window across and maximize it. For each, does the window
   fill that monitor's work area exactly — not over the taskbar, not short of the edges?
3. **Edge scrollbar** (for B-04). View ▸ Edge scrollbar, then for each of 8, 4 and 0 dp under
   View ▸ Resize border: does the thumb at the right edge drag? At 4 dp, does the window still
   resize from the outer part of the right edge?

| Check | Result (works / does not / works badly — how) |
|---|---|
| 1. Snap Layouts flyout on the maximize button | |
| 1. Drag to left / right / top edge | |
| 1. Win+Left / Win+Right / Win+Up | |
| 2. Maximize on monitor A / B (button, double click) | |
| 2. Maximized window dragged A → B, then maximized | |
| 2. Floating window moved A → B, then maximized | |
| 3. Thumb drags at 8 / 4 / 0 dp | |
| 3. Resize from the right edge at 4 dp | |

A screenshot of anything that "does not" or "works badly" helps; exact bounds (`GetWindowRect`,
`IsZoomed`, as in `youndie/appframe#60`) help B-05 most.

- Not this item: deciding what to do about a "no" — that is B-02's and B-05's; macOS (B-04's other
  half) and Linux ([B-03](B-03-linux-wayland-gnome.md)).

- AC: the table above is filled in with the setup written down, and the item closes; B-02 and B-05
  then become pickable, and B-04's Findings get the Windows rows.
- Anchors: `app/src/desktopMain/kotlin/Main.kt`, `app/src/desktopMain/kotlin/EdgeScrollbarPane.kt`,
  `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/TitleBarDrag.kt`,
  `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/WorkArea.kt`.
