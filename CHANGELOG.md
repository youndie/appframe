# Changelog

What changed in each release of `io.github.youndie:appframe`, and what a caller has to do to move
to it. The shape is [Keep a Changelog](https://keepachangelog.com/en/1.1.0/); versions follow
[Semantic Versioning](https://semver.org/) as it applies before 1.0: a minor release may break, and
when it does, its entry has an **Upgrading** part with the call before and after.

**For contributors.** A pull request that changes what a caller sees — a public declaration, a
default, a behaviour of the window, the coordinates or the requirements — adds its line under
[Unreleased](#unreleased) in the same pull request. A change that renames or reorders anything
public says so under **Upgrading**, in source *and* in bytecode: a `Dp` or another value class in a
signature changes the JVM name of the method. The release pull request (the one that moves the
version head in `gradle.properties`) renames `Unreleased` to the version and its date and opens a
new, empty `Unreleased` above it.

## [Unreleased]

_Nothing yet._

## [0.3.0] — 2026-11-04

Requirements unchanged: Java 21, Compose Multiplatform 1.12.

### Added

- `AppFrame(resizeBorder: Dp = 8.dp)` sets the thickness of the resize strip Compose lays over every
  edge of an undecorated resizable window; `0.dp` removes it (#63, closes #62). The default changes
  nothing. Content flush with an edge — a stock `VerticalScrollbar` at the right edge — loses its
  presses to that strip; see `AppFrame`'s KDoc.

### Fixed

- Windows: a double click on the title bar maximizes the window again; it used to undo its own
  maximize (#60).
- Windows: a maximized window keeps to the work area of its monitor and no longer covers the taskbar
  (#60).
- Windows: dragging a maximized window by its title bar restores it and carries it under the
  pointer, and a click with a pixel of jitter no longer moves the window (#61).

### Changed

- The double-click toggle fires on the release of the second click rather than on its press, on
  every platform (#60).
- The build sits on the portfolio's shared conventions (sborka) and Gradle 9.8.0 (#53, #54, #56).
  Nothing a caller receives depends on it.

### Upgrading from 0.2.0

**Source: a call that passes `alwaysOnTop` or anything after it by position no longer compiles.**
`resizeBorder` was inserted right after `resizable`, where `alwaysOnTop` used to be. This call
compiles against 0.2.0:

```kotlin
AppFrame(::exitApplication, rememberWindowState(), true, "My App", null, true, true) { /* … */ }
```

and against 0.3.0 fails with `Argument type mismatch: actual type is 'Boolean', but 'Dp' was
expected`. Name the arguments from `alwaysOnTop` on — the same call, unchanged in meaning:

```kotlin
AppFrame(::exitApplication, rememberWindowState(), true, "My App", null, true, alwaysOnTop = true) { /* … */ }
```

Calls that name their arguments, as the README's do, compile unchanged.

**Binary: 0.3.0 is not binary-compatible with 0.2.0.** `Dp` is a value class, so a function taking
one gets a mangled JVM name: 0.2.0 has `AppFrameKt.AppFrame(...)`, 0.3.0 has only
`AppFrameKt.AppFrame-AlbwjTQ(...)` (`javap` on both jars from Maven Central). Code compiled against
0.2.0 that calls `AppFrame` — a library of yours built on appframe, typically — fails at run time
with `NoSuchMethodError` once 0.3.0 is on the classpath, **however its arguments were written**.
Recompile it against 0.3.0. An application compiled in one build with appframe needs only the source
change above. `AppFrame` is the only public declaration whose JVM signature changed.

## [0.2.0] — 2026-09-30

The first release on Maven Central. Requires Java 21 (stated in the published metadata, so Gradle
refuses the dependency on an older toolchain) and Compose Multiplatform 1.12.

### Added

Listed against nothing released before; the 0.0.17 – 0.1.27 builds already carried these.

- `menuBar`: Material menus inside the title bar — submenus, checkable and radio items, and
  `MenuShortcut` shortcuts that fire while the menu is closed (#30).
- `AppFrame`'s content is an `AppFrameScope`, both a `ColumnScope` and a `FrameWindowScope`, so
  Compose's Swing `MenuBar` works inside it (#30).
- `MaximizeAction.Fullscreen` covers the whole screen on macOS through AWT's fullscreen (#30).

### Changed

- macOS: the green button zooms to the work area instead of asking AppKit for a fullscreen it refuses
  an undecorated window (#30).
- **The coordinate and the packages moved** (#40, #46) — see below.

### Upgrading from the 0.0.x and 0.1.x builds

Those were builds of every push, published to `https://reposilite.kotlin.website/releases`, never
releases; they stay there and keep resolving.

| Builds | Coordinate | Package |
|---|---|---|
| 0.0.8 – 0.0.19 | `ru.workinprogress:appframe-desktop` | `ru.workinprogress.appframe` |
| 0.1.20 – 0.1.22 | `io.github.youndie:appframe-desktop` | `ru.workinprogress.appframe` |
| 0.1.23 – 0.1.27 | `io.github.youndie:appframe-desktop` | `io.github.youndie.appframe` |
| 0.2.0 | `io.github.youndie:appframe` (or `appframe-desktop`), Maven Central | `io.github.youndie.appframe` |

Moving to 0.2.0 is two edits and nothing else:

- the dependency: `io.github.youndie:appframe:0.2.0` from `mavenCentral()` — a build that reads only
  the POM takes `io.github.youndie:appframe-desktop:0.2.0` — and the reposilite repository can go;
- the imports, from a build before 0.1.23: `ru.workinprogress.appframe.*` →
  `io.github.youndie.appframe.*`.

[Unreleased]: https://github.com/youndie/appframe/compare/v0.3.0...HEAD
[0.3.0]: https://github.com/youndie/appframe/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/youndie/appframe/releases/tag/v0.2.0
