package ru.workinprogress.appframe

import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState

/** True while the window fills the screen's work area. */
public val WindowState.isMaximized: Boolean
    get() = placement == WindowPlacement.Maximized

/** True while the window is in true fullscreen. */
public val WindowState.isFullscreen: Boolean
    get() = placement == WindowPlacement.Fullscreen

/**
 * Toggles between [WindowPlacement.Floating] and the placement [action] asks for.
 *
 * The previous floating bounds are restored by the platform window manager, so — unlike resizing
 * the window to the screen size by hand — this keeps multi-monitor and taskbar/dock insets correct.
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
