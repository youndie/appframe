package io.github.youndie.appframe

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState

/**
 * The receiver of [AppFrame]'s content.
 *
 * A [ColumnScope], so `Modifier.weight` works on the content below the title bar, and a
 * [FrameWindowScope], so the AWT `window` is reachable — which is what a Swing
 * [androidx.compose.ui.window.MenuBar], a file dialog or a tray icon needs.
 */
public interface AppFrameScope :
    ColumnScope,
    FrameWindowScope

private class AppFrameScopeImpl(
    column: ColumnScope,
    frame: FrameWindowScope,
) : AppFrameScope,
    ColumnScope by column,
    FrameWindowScope by frame

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
 * [menuBar] draws Material menus inside the title bar, with the shortcuts they declare live whether
 * or not their menu is open:
 *
 * ```
 * AppFrame(onCloseRequest = ::exitApplication, menuBar = {
 *     Menu("File") {
 *         Item("New", shortcut = MenuShortcut.primary(Key.N)) { new() }
 *     }
 * }) { … }
 * ```
 *
 * [style] may be swapped at runtime, with one caveat: rounded corners need a transparent window,
 * and a window's transparency is fixed the moment it is shown. Only the style present on the first
 * composition decides whether corners can be rounded at all — a style swapped in later animates its
 * [TitleBarStyle.cornerRadius] within that decision, and is ignored if the window started opaque.
 *
 * A [resizable] window has no system border to grab, so Compose lays a strip [resizeBorder] thick
 * over every edge of the content, and a press on it resizes the window. Whatever the content puts
 * flush with an edge loses its presses to that strip: a stock 8 dp `VerticalScrollbar` against the
 * right edge is covered entirely, and its thumb shows a resize cursor and cannot be dragged. Keep
 * such content [resizeBorder] in from the edges, or pass a thinner border — `0.dp` removes the
 * strip, and the window can then no longer be resized by its edges.
 *
 * [background] is drawn under the whole frame, title bar included, inside the window's rounded
 * corners — for a design whose window background runs under the bar: a texture, a gradient, an
 * image. While it is set the bar draws no surface of its own and the background shows through it;
 * the bar's controls, its double click and the drag by it work as before, and the bar still takes
 * every press on it, so none reaches the background. Below the bar the background shows wherever
 * the content draws nothing. The bar's glyphs keep `MaterialTheme.colorScheme.onSurfaceVariant`.
 *
 * [showTitle] `= false` keeps [title] for the system — the Dock, the taskbar, the window switcher —
 * and draws none in the bar. The [icon], where the [style] draws one, stays.
 *
 * ```
 * AppFrame(
 *     onCloseRequest = ::exitApplication,
 *     title = "My App",
 *     showTitle = false,
 *     background = { Stage(Modifier.matchParentSize()) },
 * ) {
 *     App(Modifier.weight(1f)) // draws nothing where the stage should show
 * }
 * ```
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
public fun AppFrame(
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(size = DpSize(1024.dp, 720.dp)),
    visible: Boolean = true,
    title: String = "AppName",
    icon: Painter? = null,
    resizable: Boolean = true,
    resizeBorder: Dp = 8.dp,
    alwaysOnTop: Boolean = false,
    style: TitleBarStyle = TitleBarStyle.forHost(),
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    menuBar: (@Composable MenuBarScope.() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    showTitle: Boolean = true,
    background: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable AppFrameScope.() -> Unit,
) {
    // Rounded corners need the window itself to be transparent — the corners are literally cut out
    // of it. Transparency is fixed once the window is displayable (Compose throws otherwise), so it
    // is latched here: a style swapped in later can animate its radius, but only within a window
    // that already started out transparent.
    val transparent = remember { style.cornerRadius > 0.dp }
    val shortcuts = remember { MenuShortcutRegistry() }

    Window(
        onCloseRequest = onCloseRequest,
        state = state,
        visible = visible,
        title = title,
        icon = icon,
        decoration = WindowDecoration.Undecorated(resizerThickness = resizeBorder),
        transparent = transparent,
        resizable = resizable,
        alwaysOnTop = alwaysOnTop,
        // The caller sees the event first; a menu shortcut only fires on what is left over.
        onPreviewKeyEvent = { event -> onPreviewKeyEvent(event) || shortcuts.handle(event) },
        onKeyEvent = onKeyEvent,
    ) {
        AppFrameContent(
            state = state,
            transparent = transparent,
            onCloseRequest = onCloseRequest,
            title = title,
            showTitle = showTitle,
            icon = icon,
            style = style,
            shortcuts = shortcuts,
            menuBar = menuBar,
            actions = actions,
            background = background,
            content = content,
        )
    }
}

/**
 * 0.3.0's [AppFrame], kept so that code compiled against 0.3.0 still links: a new parameter renames
 * nothing, but it changes the JVM descriptor, and a library built on appframe would otherwise fail
 * with `NoSuchMethodError` — the 0.2.0 → 0.3.0 break over again (CHANGELOG.md). Hidden, so source
 * always resolves to the one above.
 */
@Deprecated("Kept for binary compatibility with 0.3.0", level = DeprecationLevel.HIDDEN)
@Composable
public fun AppFrame(
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(size = DpSize(1024.dp, 720.dp)),
    visible: Boolean = true,
    title: String = "AppName",
    icon: Painter? = null,
    resizable: Boolean = true,
    resizeBorder: Dp = 8.dp,
    alwaysOnTop: Boolean = false,
    style: TitleBarStyle = TitleBarStyle.forHost(),
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    menuBar: (@Composable MenuBarScope.() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable AppFrameScope.() -> Unit,
) {
    AppFrame(
        onCloseRequest = onCloseRequest,
        state = state,
        visible = visible,
        title = title,
        icon = icon,
        resizable = resizable,
        resizeBorder = resizeBorder,
        alwaysOnTop = alwaysOnTop,
        style = style,
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent,
        menuBar = menuBar,
        actions = actions,
        content = content,
    )
}

@Composable
private fun FrameWindowScope.AppFrameContent(
    state: WindowState,
    transparent: Boolean,
    onCloseRequest: () -> Unit,
    title: String,
    showTitle: Boolean,
    icon: Painter?,
    style: TitleBarStyle,
    shortcuts: MenuShortcutRegistry,
    menuBar: (@Composable MenuBarScope.() -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
    background: (@Composable BoxScope.() -> Unit)?,
    content: @Composable AppFrameScope.() -> Unit,
) {
    val fullscreen = remember { FullscreenFallback() }

    // A maximized or fullscreen window is flush with the screen edges, so it must not be rounded;
    // neither can an opaque window be, as its corners have nothing to show through to.
    val floating = state.placement == WindowPlacement.Floating && !fullscreen.active
    val target = if (transparent && floating) style.cornerRadius else 0.dp
    val radius by animateDpAsState(targetValue = target, label = "windowCornerRadius")
    // Clipping forces a graphics layer, so square windows — every platform but macOS — skip it.
    val clip = if (radius > 0.dp) Modifier.clip(RoundedCornerShape(radius)) else Modifier

    // AWT hides the menu bar and Dock for as long as a window owns the screen, and a window
    // disposed while owning it leaves them hidden.
    DisposableEffect(window) {
        onDispose(window::releaseScreen)
    }

    // Windows maximizes a window without a caption over the taskbar; see `WorkArea.kt`.
    DisposableEffect(window) {
        val release = window.keepMaximizedBoundsOnWorkArea()
        onDispose(release)
    }

    if (menuBar != null) {
        MenuShortcuts(shortcuts, menuBar)
    }

    // The background is inside the clip, so a rounded window cuts it at the corners like the rest.
    FrameLayout(
        background = background,
        modifier = Modifier.fillMaxSize().then(clip),
        titleBar = {
            CompositionLocalProvider(
                LocalAppFrameWindow provides window,
                LocalFullscreenFallback provides fullscreen,
            ) {
                TitleBarDragArea {
                    FrameTitleBar(
                        state = state,
                        onCloseRequest = onCloseRequest,
                        title = title,
                        showTitle = showTitle,
                        icon = icon,
                        style = style,
                        overBackground = background != null,
                        menuBar = menuBar,
                        actions = actions,
                    )
                }
            }
        },
    ) {
        val scope = remember(this, this@AppFrameContent) { AppFrameScopeImpl(this, this@AppFrameContent) }
        scope.content()
    }
}
