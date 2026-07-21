package ru.workinprogress.appframe

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState

/**
 * An undecorated [Window] with a title bar drawn by Compose.
 *
 * The controls follow the host platform's conventions by default — traffic lights on the left on
 * macOS, minimize/maximize/close on the right on Windows and Linux. Pass an explicit [style] to
 * override, e.g. `TitleBarStyle.Windows`.
 *
 * ```
 * fun main() = application {
 *     MaterialTheme {
 *         AppFrame(onCloseRequest = ::exitApplication, title = "My App") {
 *             App(Modifier.fillMaxSize())
 *         }
 *     }
 * }
 * ```
 *
 * [style] may be swapped at runtime, with one caveat: rounded corners need a transparent window,
 * and a window's transparency is fixed the moment it is shown. Only the style present on the first
 * composition decides whether corners can be rounded at all — a style swapped in later animates its
 * [TitleBarStyle.cornerRadius] within that decision, and is ignored if the window started opaque.
 */
@Composable
public fun AppFrame(
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(size = DpSize(1024.dp, 720.dp)),
    visible: Boolean = true,
    title: String = "AppName",
    icon: Painter? = null,
    resizable: Boolean = true,
    alwaysOnTop: Boolean = false,
    style: TitleBarStyle = TitleBarStyle.forHost(),
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    // Rounded corners need the window itself to be transparent — the corners are literally cut out
    // of it. Transparency is fixed once the window is displayable (Compose throws otherwise), so it
    // is latched here: a style swapped in later can animate its radius, but only within a window
    // that already started out transparent.
    val transparent = remember { style.cornerRadius > 0.dp }

    Window(
        onCloseRequest = onCloseRequest,
        state = state,
        visible = visible,
        title = title,
        icon = icon,
        undecorated = true,
        transparent = transparent,
        resizable = resizable,
        alwaysOnTop = alwaysOnTop,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
    ) {
        AppFrameContent(
            state = state,
            transparent = transparent,
            onCloseRequest = onCloseRequest,
            title = title,
            icon = icon,
            style = style,
            actions = actions,
            content = content,
        )
    }
}

@Composable
private fun FrameWindowScope.AppFrameContent(
    state: WindowState,
    transparent: Boolean,
    onCloseRequest: () -> Unit,
    title: String,
    icon: Painter?,
    style: TitleBarStyle,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    // A maximized or fullscreen window is flush with the screen edges, so it must not be rounded;
    // neither can an opaque window be, as its corners have nothing to show through to.
    val target = if (transparent && state.placement == WindowPlacement.Floating) style.cornerRadius else 0.dp
    val radius by animateDpAsState(targetValue = target, label = "windowCornerRadius")
    // Clipping forces a graphics layer, so square windows — every platform but macOS — skip it.
    val clip = if (radius > 0.dp) Modifier.clip(RoundedCornerShape(radius)) else Modifier

    Column(Modifier.fillMaxSize().then(clip)) {
        WindowDraggableArea {
            TitleBar(
                state = state,
                onCloseRequest = onCloseRequest,
                title = title,
                icon = icon,
                style = style,
                actions = actions,
            )
        }
        content()
    }
}
