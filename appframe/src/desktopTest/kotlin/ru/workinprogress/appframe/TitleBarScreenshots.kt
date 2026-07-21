package ru.workinprogress.appframe

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import ru.workinprogress.viddik.LocalViddikDarkTheme
import ru.workinprogress.viddik.annotations.ViddikPreviewLabel
import ru.workinprogress.viddik.annotations.ViddikScreenshot

/**
 * Screenshot fixtures for the title bar.
 *
 * [AppFrame] itself can't be captured — it *is* a window — so these render [TitleBar] directly,
 * which is exactly the part whose layout differs per platform.
 */
data class TitleBarFixture(
    val label: String,
    val style: TitleBarStyle,
    val placement: WindowPlacement = WindowPlacement.Floating,
    val windowFocused: Boolean = true,
) : ViddikPreviewLabel {
    override val previewLabel: String get() = label
}

/**
 * The macOS traffic lights dim when the window loses focus, and the capture harness' own window is
 * focused or not depending on what else the machine is doing — left to the real [LocalWindowInfo]
 * that fixture flaked between runs (1.33% of pixels, i.e. exactly the three dots). Focus is a state
 * worth having a golden for, so it is pinned per fixture instead.
 */
private class FixedWindowInfo(override val isWindowFocused: Boolean) : WindowInfo

class TitleBarFixtures : PreviewParameterProvider<TitleBarFixture> {
    override val values: Sequence<TitleBarFixture> =
        sequenceOf(
            TitleBarFixture("Windows", TitleBarStyle.Windows),
            TitleBarFixture("Windows maximized", TitleBarStyle.Windows, WindowPlacement.Maximized),
            TitleBarFixture("macOS", TitleBarStyle.MacOs),
            TitleBarFixture("macOS unfocused", TitleBarStyle.MacOs, windowFocused = false),
            TitleBarFixture("Linux", TitleBarStyle.Linux),
            // GNOME ships with Close only, and elementary OS keeps its buttons on the left.
            TitleBarFixture(
                "Linux GNOME",
                TitleBarStyle.Linux.withGtkButtonLayout(parseGtkButtonLayout("'appmenu:close'")),
            ),
            TitleBarFixture(
                "Linux left side",
                TitleBarStyle.Linux.withGtkButtonLayout(parseGtkButtonLayout("'close,minimize,maximize:'")),
            ),
        )
}

@ViddikScreenshot(name = "TitleBar", group = "AppFrame", width = 720, height = 64, darkVariant = true)
@Composable
fun TitleBarPreview(
    @PreviewParameter(TitleBarFixtures::class) fixture: TitleBarFixture,
) {
    ScreenshotTheme {
        CompositionLocalProvider(LocalWindowInfo provides FixedWindowInfo(fixture.windowFocused)) {
            Box(Modifier.fillMaxWidth()) {
                TitleBar(
                    state = WindowState(placement = fixture.placement),
                    onCloseRequest = {},
                    title = "AppFrame",
                    style = fixture.style,
                )
            }
        }
    }
}

@Composable
private fun ScreenshotTheme(content: @Composable () -> Unit) {
    val dark = LocalViddikDarkTheme.current
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme(), content = content)
}
