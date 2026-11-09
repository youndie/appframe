---
id: B-04
title: "Tell README readers about the resize strip and resizeBorder, and check by hand that a thinner border frees an edge scrollbar"
status: open
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
