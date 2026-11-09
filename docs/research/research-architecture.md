---
id: research-architecture
title: appframe — what the library is, and what has been checked on which desktop
type: research
status: active
date: 2026-11-09
---

# Research: appframe

appframe is a window frame for Compose Multiplatform desktop applications: `AppFrame` opens an
undecorated `Window` and draws the title bar in Compose — platform-shaped controls, Material menus
inside the bar, rounded corners on macOS — so that the frame follows the app's theme instead of the
operating system's. It is a drop-in replacement for a `Window { … }` block. Everything a native frame
gives a window for free (moving, resizing, snapping, maximizing to the work area) has to be rebuilt
or handed back to the system, and that is where the open questions of this document are.

This document records what was read in the code and in the published artefacts, which behaviours
were checked by hand on which desktop and in which pull request, and what is still a hypothesis,
with the backlog item that answers it. It is amended, not rewritten.

---

## 1. Verified facts

### 1.1 Modules, target, coordinates

Read at `youndie/appframe@3aa929298f25867c71e727f3d1708ed209fe8725`.

| Fact | Where verified |
|---|---|
| Two modules: `:appframe` (the library) and `:app` (the demo, published nowhere) | `settings.gradle.kts`, `app/build.gradle.kts` |
| One Kotlin target, `jvm("desktop")`; the library exposes Compose `runtime`, `foundation`, `ui`, `material3` and `desktop.common` as `api` | `appframe/build.gradle.kts` |
| Published as `io.github.youndie:appframe` (KMP root) and `io.github.youndie:appframe-desktop`; 0.3.0 is the current release, the head is `0.3.1` | `gradle.properties`, `README.md` (Installation) |
| Bytecode floor Java 21 (`sborka.jvmFloor=21`); compiled against Compose Multiplatform 1.12.1 from the shared catalog | `gradle.properties`, `gradle/libs.versions.toml`, `youndie/appframe#62` (cites `ui-desktop-1.12.1.jar`) |
| CI builds and runs `check` on `ubuntu-latest`, `windows-latest` and `macos-latest`; `check` includes ktlint, unit tests and 18 viddik screenshot goldens of the title bar | `.github/workflows/build.yml`, `appframe/build.gradle.kts` (`viddik { verifyOnCheck = true }`) |
| Unit tests cover pure rules only — host detection, the GTK button layout parser, the fullscreen-fallback rule, the work-area rectangle, the drag threshold, menu shortcuts. No test opens a window and moves or resizes it | `appframe/src/desktopTest/kotlin/io/github/youndie/appframe/` |
| There is no CHANGELOG and no upgrade note in the repository; what changed in a release is written only in its GitHub release text | repository tree; `gh release view v0.3.0` |

**Consequence 1.** CI proves that the bar *draws* the same on all three runners. Nothing in CI
proves that a window *behaves*: every statement below about moving, resizing or snapping is either
a hand check with a pull request behind it, or a hypothesis.

### 1.2 What was checked by hand, where

| Behaviour | Desktop | Where recorded |
|---|---|---|
| Zoom button zooms; `MaximizeAction.Fullscreen` covers the screen through AWT fullscreen, three round trips each with `java.awt.Robot` | macOS | `youndie/appframe#30` |
| Double click on the bar maximizes and restores; a maximized window keeps to the work area (taskbar visible) | Windows 11, 150 %, **one monitor** | `youndie/appframe#60` |
| Dragging a maximized window restores it under the pointer; 2 px of jitter does not move it | Windows 11, 150 %, **one monitor** | `youndie/appframe#61` |
| An 8 dp scrollbar flush with the right edge cannot be dragged — the resize strip covers it | macOS | `youndie/appframe#62` |
| A thinner `resizeBorder` frees that scrollbar | — **not checked** | `youndie/appframe#63` ("Not checked by hand") |
| Anything on Linux beyond the screenshot goldens | — **not checked** | no pull request records one |
| More than one monitor | — **not checked** | `youndie/appframe#60`, `youndie/appframe#61` ("Not verified: multiple monitors") |

**Consequence 2.** The README's feature line "respects the taskbar/dock and multi-monitor setups"
(`README.md:24-25`) is ahead of the evidence on its second half: the multi-monitor path
(`WorkArea.kt`, refreshed on every move) is covered by a unit test of the rectangle only
(`WorkAreaTest.kt`). [B-05](../backlog/B-05-multi-monitor-on-windows.md) checks it.

### 1.3 How a window is moved and resized, per platform

| Fact | Where verified |
|---|---|
| On Windows, appframe moves the window itself: `setLocation` from `MouseInfo` on every pointer event after a 4 px threshold | `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/TitleBarDrag.kt:66-90` |
| Elsewhere it uses Compose's `WindowDraggableArea` | `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/TitleBarDrag.kt:33-37` |
| `WindowDraggableArea` hands the move to the window manager **only on the JetBrains Runtime**: it asks `JBR.isWindowMoveSupported()` and then uses `JbrMoveHandler` (`com.jetbrains.WindowMove.startMovingTogetherWithMouse`); on any other JDK it uses `StandardMoveHandler`, which calls `Window.setLocation` from `MouseInfo` | `foundation-desktop-1.12.1.jar!/androidx/compose/foundation/window/WindowDraggableArea_desktopKt.class`, `foundation-desktop-1.12.1.jar!/androidx/compose/foundation/window/JbrMoveHandler.class`, `foundation-desktop-1.12.1.jar!/androidx/compose/foundation/window/StandardMoveHandler.class` (read with `javap -c`) |
| The resize strip resizes by hand on every platform: `UndecoratedWindowResizer` reads `MouseInfo` and calls `Window.setSize` / `Window.setLocation` | `ui-desktop-1.12.1.jar!/androidx/compose/ui/window/UndecoratedWindowResizer.class` (read with `javap -c`) |
| The window controls are Compose `clickable` + `hoverable` boxes; nothing answers the system's hit test for a caption or a maximize button | `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/WindowControls.kt:58-122`; no `WM_`, JNA or JBR call anywhere in `appframe/src` |

**Consequence 3.** The KDoc's "on Linux Compose hands the move to the window manager"
(`TitleBarDrag.kt:29-30`) is true on the JetBrains Runtime and false on Temurin or any other JDK,
which the README's "Java 21" requirement admits. Whether the hand-rolled move and resize work on a
Wayland session is therefore two questions, one per runtime — [B-03](../backlog/B-03-linux-wayland-gnome.md).

**Consequence 4.** On Windows neither the move nor the maximize button goes through the system, and
Windows 11's Snap Layouts flyout and Aero Snap are system behaviours of exactly those two —
[B-02](../backlog/B-02-windows-snap.md). This is a reading of the code, not an observation.

### 1.4 0.3.0 changed `AppFrame`'s signature, in source and in bytecode

| Fact | Where verified |
|---|---|
| 0.2.0: `resizable: Boolean` is followed by `alwaysOnTop: Boolean` | `youndie/appframe@v0.2.0!/appframe/src/desktopMain/kotlin/io/github/youndie/appframe/AppFrame.kt` |
| 0.3.0: `resizeBorder: Dp = 8.dp` sits between them | `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/AppFrame.kt:95-97`, `youndie/appframe#63` |
| The JVM method changed name: 0.2.0 has `AppFrameKt.AppFrame(...)`, 0.3.0 has only `AppFrameKt.AppFrame-AlbwjTQ(...)` — `Dp` is a value class, so the name is mangled | `appframe-desktop-0.2.0.jar!/io/github/youndie/appframe/AppFrameKt.class`, `appframe-desktop-0.3.0.jar!/io/github/youndie/appframe/AppFrameKt.class` (read with `javap`) |
| The v0.3.0 release text calls the change "source-incompatible for positional callers"; it does not mention bytecode | GitHub release `v0.3.0` |

**Consequence 5.** 0.3.0 is binary-incompatible as well: a library compiled against 0.2.0 that calls
`AppFrame` fails with `NoSuchMethodError` when an application puts 0.3.0 on its classpath, even if
every argument is named. No repository document says so — [B-01](../backlog/B-01-changelog-and-upgrade-notes.md).

---

## 2. Decisions

### D1. The default resize border stays Compose's 8 dp

`youndie/appframe#62` listed three options: a thinner strip, insetting the content, documenting the
8 dp. `youndie/appframe#63` took the parameter and kept the default, so that nothing changes for an
existing caller; the KDoc on `AppFrame` (`AppFrame.kt:80-85`) carries the workaround. Rejected:
insetting the content, which would move every existing layout by 8 dp. The price: a newcomer who
reads only the README does not learn about the strip — [B-04](../backlog/B-04-document-the-resize-border.md).

### D2. On macOS the green button zooms rather than going fullscreen

AppKit refuses `toggleFullScreen:` for a borderless window while `WindowState.placement` reports
`Fullscreen` anyway (`youndie/appframe#30`, measured bounds in the PR). `TitleBarStyle.MacOs` zooms;
`MaximizeAction.Fullscreen` uses AWT fullscreen, without a separate Space
(`appframe/src/desktopMain/kotlin/io/github/youndie/appframe/Fullscreen.kt`).

### D3. On Windows appframe drags the window itself

`WindowDraggableArea` moved a maximized window without restoring it and moved on a pixel of jitter
(`youndie/appframe#61`). The replacement waits for `SM_CXDRAG`'s default 4 px and restores before it
moves (`TitleBarDrag.kt`). Consequence 4 is the price this may carry.

---

## 3. Hypotheses, each with the item that answers it

| # | Hypothesis | Answered by |
|---|---|---|
| H1 | On Windows 11, hovering the maximize button shows no Snap Layouts flyout, and dragging the window to a screen edge does not snap it | [B-02](../backlog/B-02-windows-snap.md) |
| H2 | On a GNOME Wayland session (XWayland, the default AWT toolkit), move and resize work on the JetBrains Runtime; on Temurin, where both are `setLocation`/`setSize`, some of them do not | [B-03](../backlog/B-03-linux-wayland-gnome.md) |
| H3 | A maximized window on a second monitor keeps to that monitor's work area, as `WorkArea.kt` intends | [B-05](../backlog/B-05-multi-monitor-on-windows.md) |
| H4 | A `resizeBorder` thinner than the scrollbar frees its thumb, on macOS and Windows | [B-04](../backlog/B-04-document-the-resize-border.md) |

### Not checked, and deliberately without an item

macOS beyond D2 — whether the green button's hover menu of window tiles appears, whether a double
click on the bar follows the system's "double-click a window's title bar" setting (appframe always
toggles: `TitleBar.kt:181-207`). No issue, no consumer report and no hand check points at either;
an item is opened when one does.
