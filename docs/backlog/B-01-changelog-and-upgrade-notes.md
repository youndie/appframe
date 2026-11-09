---
id: B-01
title: "Keep a CHANGELOG, and say in it how to move from 0.2.0 to 0.3.0 — a source and a binary break"
status: done
priority: P1
size: S
stage: stage-first-contact
---

# B-01 — Keep a CHANGELOG, and say in it how to move from 0.2.0 to 0.3.0 — a source and a binary break

The repository has no CHANGELOG and no upgrade note; what changed in a release lives only in its
GitHub release text. 0.3.0 broke `AppFrame` twice, and the repository records neither:

- **Source.** `youndie/appframe#63` put `resizeBorder: Dp = 8.dp` right after `resizable`
  (`appframe/src/desktopMain/kotlin/io/github/youndie/appframe/AppFrame.kt:95-97`). In 0.2.0
  `alwaysOnTop: Boolean` sat there, so a call that passes `alwaysOnTop` or anything after it by
  position no longer compiles. The v0.3.0 release text says this.
- **Binary.** Because `Dp` is a value class, the JVM method was renamed: 0.2.0 has
  `AppFrameKt.AppFrame(...)`, 0.3.0 only `AppFrameKt.AppFrame-AlbwjTQ(...)` (`javap` on both jars,
  [research §1.4](../research/research-architecture.md)). A library compiled against 0.2.0 that calls
  `AppFrame` fails at runtime with `NoSuchMethodError` under 0.3.0, named arguments or not. The
  release text does not say this.

- **The decision and its reason.** A `CHANGELOG.md` at the root in the Keep a Changelog shape, with
  entries for 0.1.x → 0.2.0 (the coordinate and package move, #40 and #46) and 0.2.0 → 0.3.0, and an
  "Upgrading" part per breaking release with the before/after call. A stranger deciding whether to
  bump reads the repository, not the release page; and a dependent library's author needs the binary
  half, which only the bytecode shows.
- **The rule, written where a contributor reads it**: a pull request that changes the public
  surface adds its line under `Unreleased`; the release pull request (as #64 did for the README)
  moves `Unreleased` under the version. The README's Installation section links the file.
- Not this item: an ABI gate in `check` (a binary-compatibility dump would have flagged #63 in
  review) — a separate item if the owner wants one; changing `AppFrame`'s parameter order back.

- AC: `CHANGELOG.md` exists; its 0.3.0 entry names the positional break with a 0.2.0 call that no
  longer compiles and its named-argument fix, and states that 0.3.0 is not binary-compatible with
  0.2.0 for code compiled against it; the contributor rule is written down; the README links the file.
- Anchors: `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/AppFrame.kt`, `README.md`,
  `gradle.properties` (the version head).

## Findings

2026-11-09, closed by the pull request that adds `CHANGELOG.md`.

- **Binary, re-checked.** `javap -public` over every class of `appframe-desktop-0.2.0.jar` and
  `appframe-desktop-0.3.0.jar` from Maven Central, diffed: the only public method that disappeared is
  `AppFrameKt.AppFrame(...)`, replaced by `AppFrameKt.AppFrame-AlbwjTQ(...)` (a `float` for the `Dp`
  after the second `boolean`). The classes 0.3.0 adds (`Grab`, `TitleBarDragKt`, `WorkAreaKt`) are
  Kotlin `internal`, public only in bytecode. A first dump with all classes in one `javap` call came
  out empty and "diffed clean" — the per-class run is the one that counts.
- **Source, compiled.** A scratch file in `:app` (not committed) with the 0.2.0 positional call
  `AppFrame(::exitApplication, rememberWindowState(), true, "My App", null, true, true) { }` failed
  `:app:compileKotlinDesktop` on the Linux box with `Argument type mismatch: actual type is
  'Boolean', but 'Dp' was expected`; with `alwaysOnTop = true` named it compiled. That the same call
  compiles against 0.2.0 is read from the 0.2.0 signature (two `boolean`s after the `Painter`), not
  compiled.
- **Not checked**: a run of a 0.2.0-compiled caller against 0.3.0 showing the `NoSuchMethodError`
  itself; it follows from the missing method.
- **The 0.0.x / 0.1.x table** is from the reposilite `maven-metadata.xml` of both groups and the
  deploy runs of #40 (run 20, the first `io.github.youndie` build) and #46 (run 23, the first with
  the new packages). #30's menus are in builds from 0.0.17 (its deploy run).
