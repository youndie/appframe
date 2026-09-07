package io.github.youndie.appframe

import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import java.awt.Window

/** True while the window fills the screen's work area. */
public val WindowState.isMaximized: Boolean
    get() = placement == WindowPlacement.Maximized

/**
 * True while the window is in true fullscreen.
 *
 * Reads [WindowState] alone, which is all there is to read on Windows and Linux. On macOS an
 * undecorated window reaches fullscreen by a route the placement cannot describe — see
 * `Fullscreen.kt` — and [AppFrame]'s own title bar consults that as well.
 */
public val WindowState.isFullscreen: Boolean
    get() = placement == WindowPlacement.Fullscreen

/**
 * Toggles between [WindowPlacement.Floating] and the placement [action] asks for.
 *
 * The previous floating bounds are restored by the platform window manager, so — unlike resizing
 * the window to the screen size by hand — this keeps multi-monitor and taskbar/dock insets correct.
 *
 * This is the placement-only toggle: [MaximizeAction.Fullscreen] does nothing visible to an
 * undecorated window on macOS. [AppFrame]'s zoom button goes through the internal toggle that
 * also carries the window, which works around it.
 */
public fun WindowState.toggleMaximized(action: MaximizeAction = MaximizeAction.Maximize) {
    placement =
        if (placement == WindowPlacement.Floating) {
            when (action) {
                MaximizeAction.Maximize -> WindowPlacement.Maximized
                MaximizeAction.Fullscreen -> WindowPlacement.Fullscreen
            }
        } else {
            WindowPlacement.Floating
        }
}

/**
 * The zoom button's toggle: [toggleMaximized] plus the macOS fullscreen workaround.
 *
 * Driven straight from the click rather than from an effect watching [WindowState.placement]. The
 * placement is not a reliable signal for this: Compose overwrites whatever is written there with
 * what the window reports, and AWT's fullscreen is not something it reports — a watcher would see
 * the value flip back within the same frame, or see nothing at all, since `snapshotFlow` conflates.
 */
internal fun WindowState.toggleZoom(
    action: MaximizeAction,
    window: Window?,
    fullscreen: FullscreenFallback,
) {
    if (window == null || action != MaximizeAction.Fullscreen || !window.needsFullscreenFallback()) {
        toggleMaximized(action)
        return
    }

    if (fullscreen.active) fullscreen.exit(window) else fullscreen.enter(window)
}
