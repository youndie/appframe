# Screenshot goldens

Recorded PNGs live here. **They are recorded on CI, not locally** — Skia renders text with whatever
fonts the host has installed, so a golden recorded on macOS never matches the ubuntu-latest runner
that verifies it.

To (re-)record:

1. Actions → **Record screenshot goldens** → *Run workflow*
2. Download the `appframe-goldens` artifact
3. Unzip it over this directory and commit the PNGs

Locally you can still record and verify against your own machine — just don't commit the result:

```shell
VIDDIK_RECORD_MODE=true ./gradlew :appframe:screenshotTest --rerun   # record
./gradlew :appframe:screenshotTest --rerun                          # verify
```

Fixtures live in `../kotlin/ru/workinprogress/appframe/TitleBarScreenshots.kt`.
