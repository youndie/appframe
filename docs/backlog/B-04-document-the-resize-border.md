---
id: B-04
title: "Tell README readers about the resize strip and resizeBorder, and check by hand that a thinner border frees an edge scrollbar"
status: question
priority: P2
size: S
stage: stage-first-contact
---

# B-04 — Tell README readers about the resize strip and resizeBorder, and check by hand that a thinner border frees an edge scrollbar

`youndie/appframe#62`: an `AppFrame` window carries Compose's 8 dp resize strip over every edge of
its content, and a stock 8 dp `VerticalScrollbar` flush with the right edge cannot be dragged —
seen by hand on macOS. `youndie/appframe#63` added `AppFrame(resizeBorder = …)` and kept the 8 dp
default (research D1); the explanation is in `AppFrame`'s KDoc
(`appframe/src/desktopMain/kotlin/io/github/youndie/appframe/AppFrame.kt:80-85`) and in the v0.3.0
release text. Two gaps remain:

- the README, which is where a newcomer reads, does not mention the strip or the parameter
  (`README.md`, Features and Usage);
- the fix itself was never seen working: #63 says "Not checked by hand that a thinner border frees
  the scrollbar", and #62 says "Windows not checked yet".

- **The decision and its reason.** A short README paragraph under Usage — what the strip is, what it
  covers, `resizeBorder` and `0.dp` — because the scrollbar under it is the first bug a list-heavy app
  hits. Then the hand check, macOS and Windows, with a flush-right `VerticalScrollbar` (the demo has
  none: `app/src/commonMain/kotlin/io/github/youndie/appframe/App.kt` — add one to the demo, or use a
  scratch app): default 8 dp, `4.dp`, `0.dp`.
- Not this item: changing the default (the owner kept it in #63); insetting content.

- AC: the README describes the strip and `resizeBorder`; the item's Findings record, on macOS and on
  Windows, whether the thumb drags at 8, 4 and 0 dp and whether the window still resizes from that
  edge at 4 dp.
- Anchors: `README.md`, `appframe/src/desktopMain/kotlin/io/github/youndie/appframe/AppFrame.kt`,
  `app/src/commonMain/kotlin/io/github/youndie/appframe/App.kt`.

## Findings

2026-11-09.

- **The README half is done**: Usage has "The resize strip at the edges" — what the strip is, that a
  flush 8 dp `VerticalScrollbar` sits wholly under it, `resizeBorder`, and `0.dp`.
- **The demo can show the case now**, so the hand check needs no scratch app: View ▸ Edge scrollbar
  swaps the content for a 120-row column with a stock `VerticalScrollbar` flush right
  (`app/src/desktopMain/kotlin/EdgeScrollbarPane.kt`), and View ▸ Resize border picks 8, 4 or 0 dp
  (`app/src/desktopMain/kotlin/Main.kt`). Compiled and `ktlintCheck`-clean; on macOS it was started
  (`./gradlew :app:run`, JetBrains Runtime 25) but not looked at.
- **Not checked: the hand check, on either platform.** On macOS the loop asked for control of the
  demo's window to drag the thumb and was refused, so no gesture was made; on Windows it has no
  interactive session. No result is recorded for any of 8, 4 or 0 dp.

## Question

**Who drags the thumb, and when?** About a minute per platform with the demo: View ▸ Edge
scrollbar, then for each of 8, 4 and 0 dp under View ▸ Resize border — does the thumb drag, and at
4 dp does the window still resize from the right edge (the outer 4 dp)? Record the platform, its
version and the display scaling in Findings above.

The choices, for the owner:

1. **The owner runs it** on macOS and on Windows (the Windows half fits the B-02 / B-05 sitting),
   and the item closes on the results.
2. **The owner lets the loop drive the demo window on the Mac** — the macOS half then gets done by
   the loop; Windows still needs a person.
3. **Close on the README alone**, recording the hand check as not done.
