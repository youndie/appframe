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
  setups; the macOS green button goes fullscreen instead, as it should
* Rounded window corners where the platform expects them (macOS), squared off automatically while
  maximized or fullscreen
* Double-click the title bar to toggle maximize
* Custom `actions` slot in the title bar
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

## Screenshot tests

The title bar is covered by [viddik](https://github.com/youndie/viddik) screenshot tests — one
parameterized fixture in `appframe/src/desktopTest/.../TitleBarScreenshots.kt` produces a golden per
platform layout (Windows, Windows maximized, macOS focused/unfocused, GNOME, left-side Linux), each
in light and dark.

They live in their own Gradle task rather than in `check`, because goldens are host-specific:

```shell
./gradlew :appframe:screenshotTest                # verify
VIDDIK_RECORD_MODE=true ./gradlew :appframe:screenshotTest --rerun   # record
./gradlew check -Pviddik.verify                   # wire verification into `check` (CI does this)
```

Committed goldens are recorded by the **Record screenshot goldens** workflow, on the same runner
image that verifies them in CI — see `appframe/src/desktopTest/snapshots/README.md`. Until that
workflow has been run once, the CI screenshot step fails with `No golden snapshot for …`, which is
the signal to record.

> Prefer verifying locally instead? viddik's `ViddikConsistentRendering` bundles a font and pins
> rasterization so goldens become portable across OSes, at the cost of visibly rougher text — set
> the `viddik.consistentRendering` system property on the test task and build the fixture theme with
> `viddikTypography()`.

## Running the sample

```shell
./gradlew :app:run
```

The sample switches between the platform layouts at runtime.
