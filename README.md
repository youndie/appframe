# AppFrame

[![kotlin](https://img.shields.io/badge/Kotlin-2.4.10-blue?logo=kotlin&logoColor=white)](https://kotlinlang.org)
![Static Badge](https://img.shields.io/badge/Desktop-blue)
[![Maven Central](https://reposilite.kotlin.website/api/badge/latest/releases/ru/workinprogress/appframe-desktop?name=snapshots&color=40c14a&prefix=v)](https://reposilite.kotlin.website/#/releases/ru/workinprogress/appframe-desktop)
[![license](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

A customizable window frame library for [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)
desktop applications. Provides a modern, native-looking window with a Compose-drawn title bar and
standard window controls.

![Screenshot](/Screenshot.png?raw=true "Example App Screenshot")

## Features

* **Platform-aware controls** — traffic lights on the left on macOS, minimize/maximize/close on the
  right on Windows, picked automatically from the host OS
* On Linux there is no single convention, so the desktop is asked directly: the GTK button layout
  (`org.gnome.desktop.wm.preferences button-layout`) decides both the side and which buttons exist —
  right on GNOME/KDE/XFCE (GNOME ships with Close only), left on elementary OS and the old Unity
  layout. Falls back to the right-hand layout if the setting can't be read.
* Windows 11 button metrics including the red close-button hover; macOS traffic lights dim when the
  window loses focus and reveal their glyphs on hover
* Correct maximize via `WindowPlacement.Maximized` — respects the taskbar/dock and multi-monitor
  setups; on macOS the green button zooms, and `MaximizeAction.Fullscreen` covers the whole screen
  even though AppKit will not give an undecorated window its native fullscreen ([Zoom and
  fullscreen](#zoom-and-fullscreen))
* Rounded window corners where the platform expects them (macOS), squared off automatically while
  maximized or fullscreen
* Double-click the title bar to toggle maximize
* Custom `actions` slot in the title bar
* Material menus **inside** the title bar — submenus, checkable items and shortcuts that work while
  the menu is closed, instead of a Swing `JMenuBar` strip that ignores your theme ([Menus](#menus))
* Fully theme-aware — Material 3 colors, works inside any `MaterialTheme`
* No icon dependencies — the glyphs are drawn with `Canvas`
* Drop-in replacement for your `Window` block

## Installation

Add the repository to your `settings.gradle.kts`

```kotlin
maven("https://reposilite.kotlin.website/releases")
```

And the dependency:

```kotlin
desktopMain.dependencies {
    implementation("ru.workinprogress:appframe-desktop:0.0.{version}")
}
```

## Usage

```kotlin
fun main() = application {
    MaterialTheme {
        AppFrame(onCloseRequest = ::exitApplication, title = "My App") {
            App(modifier = Modifier.fillMaxSize())
        }
    }
}
```

### Choosing a layout explicitly

`AppFrame` uses `TitleBarStyle.forHost()` by default. Any of the built-in styles — or a `copy()` of
one — can be passed in, which is handy for demos and screenshots:

```kotlin
AppFrame(
    onCloseRequest = ::exitApplication,
    style = TitleBarStyle.Windows, // .MacOs, .Linux, or forHost(HostOs.Linux)
    actions = {
        IconButton(onClick = ::openSettings) { /* … */ }
    },
) { /* content */ }
```

Every visual aspect is a field on `TitleBarStyle`: bar height, which side the controls sit on, their
order, appearance (`Bars` or `TrafficLights`), button and glyph size, spacing, title alignment,
window corner radius, and whether the middle button maximizes or goes fullscreen.

One caveat when swapping styles at runtime: `cornerRadius > 0` makes `AppFrame` ask for a
transparent window, and transparency cannot change after a window is shown. Only the style used on
the first composition decides whether the window can be rounded; later styles animate the radius
inside that decision (and are ignored if the window started opaque).

The title bar can also be used on its own inside a hand-rolled `Window` via the `TitleBar`
composable, and `WindowState.toggleMaximized()` / `isMaximized` / `isFullscreen` are public helpers.
Provide `LocalAppFrameWindow` around it so its zoom button can reach the window.

### Menus

`menuBar` draws Material menus in the title bar, laid out after the app icon — the arrangement of
every single-row title bar with menus, from VS Code to a GNOME header bar:

```kotlin
AppFrame(
    onCloseRequest = ::exitApplication,
    menuBar = {
        Menu("File") {
            Item("New", shortcut = MenuShortcut.primary(Key.N)) { newDocument() }
            Menu("Open Recent") {
                recent.forEach { Item(it.name) { open(it) } }
            }
            Separator()
            Item("Save", shortcut = MenuShortcut.primary(Key.S), enabled = dirty) { save() }
        }
        Menu("View") {
            CheckboxItem("Word wrap", checked = wrap) { wrap = it }
            Separator()
            themes.forEach { RadioButtonItem(it, selected = it == theme) { theme = it } }
        }
    },
) { /* content */ }
```

The DSL mirrors Compose's own `MenuBar` — `Menu`, `Item`, `CheckboxItem`, `RadioButtonItem`,
`Separator`, and `Menu` again for a submenu. Sliding along the bar switches between open menus, and
hovering an item opens its submenu, the way a desktop menu bar behaves.

Menus also take over where the title goes: with a menu bar in the row the title is centered on the
space they leave over and drawn a step back, whatever `TitleBarStyle.titleAlignment` says. A
start-aligned title — the Windows layout — would otherwise sit right against the last menu in the
same size and weight, and read as one more menu.

Shortcuts are `MenuShortcut`, not Compose's `KeyShortcut`, which keeps its key and modifiers
internal — there would be no way to draw `⌘N` next to the item or to match a key event against it.
`MenuShortcut.primary(Key.N)` is Command on macOS and Ctrl everywhere else. They fire whether or not
their menu is open, including from inside a closed submenu, and a disabled item — or an item in a
disabled menu — does not fire at all.

The bar itself is `AppMenuBar`, public so it can go into a hand-rolled title bar too.

#### Still want the Swing menu bar?

Some apps want the real thing — on macOS, the system menu bar at the top of the screen. `AppFrame`'s
content is an `AppFrameScope`, which is both a `ColumnScope` and Compose's `FrameWindowScope`, so
`MenuBar { … }` works inside it as usual:

```kotlin
AppFrame(onCloseRequest = ::exitApplication) {
    MenuBar {
        Menu("File") { Item("New", onClick = ::newDocument) }
    }
    App(Modifier.weight(1f))
}
```

### Zoom and fullscreen

`TitleBarStyle.maximizeAction` decides what the middle button and a double click do:

| | `MaximizeAction.Maximize` | `MaximizeAction.Fullscreen` |
|---|---|---|
| Windows, Linux | `WindowPlacement.Maximized` | `WindowPlacement.Fullscreen` |
| macOS | `WindowPlacement.Maximized` | whole screen, via AWT — see below |

macOS is the awkward one. Its green button normally goes fullscreen, but that is a decorated
window's privilege: AppKit ignores `toggleFullScreen:` on the borderless window an undecorated
`AppFrame` is. Nothing throws, and `WindowState.placement` reports `Fullscreen` regardless, so a
title bar that trusts the placement redraws as if the window had resized — while the window sits
there at the same size. That is why `TitleBarStyle.MacOs` zooms instead: filling the work area is
what an option-click on the green button does, and it is real.

Asking for `MaximizeAction.Fullscreen` anyway still works — `AppFrame` hands the window to AWT's own
fullscreen, which covers the screen with the menu bar and Dock out of the way. There is no separate
Space and no slide animation; it is the only fullscreen a borderless window can have.

```kotlin
AppFrame(
    onCloseRequest = ::exitApplication,
    style = TitleBarStyle.MacOs.copy(maximizeAction = MaximizeAction.Fullscreen),
) { /* content */ }
```

## Screenshot tests

The title bar is covered by [viddik](https://github.com/youndie/viddik) screenshot tests — one
parameterized fixture in `appframe/src/desktopTest/.../TitleBarScreenshots.kt` produces a golden per
platform layout (Windows, Windows maximized, macOS focused/unfocused, GNOME, left-side Linux, and
Windows/macOS with menus), each in light and dark.

The wiring comes from viddik's Gradle plugin (`id("ru.workinprogress.viddik")` in
`appframe/build.gradle.kts`), and verification is part of `check` — `./gradlew build` runs it:

```shell
./gradlew :appframe:viddikRecord                           # (re-)record, then look at the PNGs
./gradlew :appframe:viddikVerify                           # verify all 18
./gradlew :appframe:viddikVerify --component "Linux GNOME" # just one layout, light and dark
./gradlew :appframe:viddikShowroom                         # browse the fixtures in a window
```

**Goldens are recorded wherever you work, and committed.** The fixtures build their theme with
viddik's `viddikTypography()`, which draws in a bundled Roboto instead of whatever font the host
happens to have — so the capture no longer depends on the machine it runs on. Recorded on macOS and
on `ubuntu-latest` and compared byte for byte, all eighteen PNGs come out identical, which is why
CI verifies on all three runners rather than on the one that recorded them.

> This is what viddik's bundled font buys a project that has none of its own. A project that ships
> its own font should keep using it and run the bytes through `normalizeVerticalMetrics()` instead —
> substituting Roboto into a golden of a UI that doesn't use Roboto is worse than useless.

## Running the sample

```shell
./gradlew :app:run
```

The sample switches between the platform layouts at runtime, and its **View** menu flips the zoom
button between maximizing and fullscreen.
