package io.github.youndie.appframe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import java.awt.Frame
import java.awt.Window

/**
 * The AWT window a title bar belongs to, or `null` when there is nothing to reach.
 *
 * [AppFrame] provides it around its [TitleBar], which is what lets the zoom button work around the
 * macOS fullscreen limitation described below. A hand-rolled title bar can provide it too:
 *
 * ```
 * Window(onCloseRequest = ::exitApplication, undecorated = true) {
 *     CompositionLocalProvider(LocalAppFrameWindow provides window) {
 *         TitleBar(state = state, onCloseRequest = ::exitApplication)
 *     }
 * }
 * ```
 */
public val LocalAppFrameWindow: ProvidableCompositionLocal<Window?> = staticCompositionLocalOf { null }

/**
 * True when this window cannot reach fullscreen the way Compose asks for it.
 *
 * `WindowPlacement.Fullscreen` ends up in `[NSWindow toggleFullScreen:]`, and AppKit refuses that
 * for a borderless window — which is exactly what `undecorated = true` produces. Nothing throws and
 * nothing is logged: the window keeps its size while [WindowState.placement] reports `Fullscreen`
 * anyway, so the title bar redraws as if it had worked. That is the bug this file exists for.
 *
 * Every other desktop resizes an undecorated window fine — X11 sets `_NET_WM_STATE_FULLSCREEN`,
 * Windows moves the window to the monitor bounds — so the workaround is kept to the one platform
 * that needs it.
 */
internal fun Window.needsFullscreenFallback(os: HostOs = HostOs.current): Boolean =
    needsFullscreenFallback(os, undecorated = this is Frame && isUndecorated)

/** The rule itself, so it can be checked without a window on the screen. */
internal fun needsFullscreenFallback(
    os: HostOs,
    undecorated: Boolean,
): Boolean = os == HostOs.MacOs && undecorated

/**
 * Fullscreen for the windows [needsFullscreenFallback] describes, held outside [WindowState].
 *
 * The workaround hands the window to AWT's own fullscreen, which covers the whole screen — menu bar
 * and Dock included — but is not what AWT reports back as fullscreen. Compose keeps
 * [WindowState.placement] in sync with that report and overwrites anything else written there, so
 * the state has to live here instead: [active] is what the title bar and the window corners read.
 *
 * Not the macOS native fullscreen either way: no separate Space and no slide animation. It is the
 * only fullscreen a borderless window can get, and unlike the native one it does resize.
 */
@Stable
internal class FullscreenFallback {
    var active: Boolean by mutableStateOf(false)
        private set

    fun enter(window: Window) {
        val device = window.graphicsConfiguration?.device ?: return
        if (!device.isFullScreenSupported) return

        device.fullScreenWindow = window
        active = true
    }

    /**
     * The screen is released before anything else touches the placement: AWT leaves a window that
     * is still in fullscreen reporting fullscreen afterwards, and Compose would sync that stale
     * report straight back into [WindowState.placement].
     */
    fun exit(window: Window) {
        window.releaseScreen()
        active = false
    }
}

/** Created by [AppFrame]; `null` in a title bar that nobody provided one to. */
internal val LocalFullscreenFallback: ProvidableCompositionLocal<FullscreenFallback?> =
    staticCompositionLocalOf { null }

/** The fallback in scope, or a private one so a standalone [TitleBar] still behaves. */
@Composable
internal fun rememberFullscreenFallback(): FullscreenFallback {
    val provided = LocalFullscreenFallback.current
    val own = remember { FullscreenFallback() }
    return provided ?: own
}

/** Hands the screen back if this window is holding it. */
internal fun Window.releaseScreen() {
    val device = graphicsConfiguration?.device ?: return
    if (device.fullScreenWindow === this) {
        device.fullScreenWindow = null
    }
}

/** Whether the window is covering the screen, by either route. */
internal fun WindowState.isFullscreenWith(fallback: FullscreenFallback): Boolean =
    fallback.active || placement == WindowPlacement.Fullscreen
