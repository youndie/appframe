package io.github.youndie.appframe

import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/** A button rendered in the title bar. */
public enum class WindowControl {
    Minimize,
    Maximize,
    Close,
}

/** What the middle ("zoom") button does. */
public enum class MaximizeAction {
    /** Fill the work area of the current screen, keeping the taskbar/dock visible. */
    Maximize,

    /**
     * Cover the whole screen, taskbar/dock and menu bar included.
     *
     * On macOS this is not the native fullscreen the green button normally performs — AppKit will
     * not grant that to an undecorated window, so [AppFrame] covers the screen through AWT
     * instead: no separate Space, no slide animation, but the window really does fill the screen.
     * See `Fullscreen.kt`.
     */
    Fullscreen,
}

/** How the window controls are drawn. */
public enum class WindowControlsAppearance {
    /** Rectangular, full-height buttons with a hover fill — Windows / most Linux DEs. */
    Bars,

    /** Round, coloured "traffic light" buttons that reveal their glyph on hover — macOS. */
    TrafficLights,
}

/**
 * Everything about how [AppFrame]'s title bar looks and where its buttons live.
 *
 * Use [forHost] for the native look of the current OS, or build one explicitly to force a
 * particular platform's layout (handy for screenshots and demos).
 */
@Immutable
public data class TitleBarStyle(
    val height: Dp,
    /** Which edge of the title bar the window controls are pinned to. */
    val controlsAlignment: Alignment.Horizontal,
    /** Buttons in visual order, left to right. */
    val controlsOrder: List<WindowControl>,
    val controlsAppearance: WindowControlsAppearance,
    /** Where the window title is drawn. Only [Alignment.Start] and [Alignment.CenterHorizontally]. */
    val titleAlignment: Alignment.Horizontal,
    val buttonSize: DpSize,
    val buttonSpacing: Dp,
    /** Free space between the controls and the window edge. */
    val controlsPadding: Dp,
    val glyphSize: Dp,
    /** What the middle button and a double click on the title bar do. */
    val maximizeAction: MaximizeAction,
    /**
     * Radius of the window's own corners.
     *
     * Anything above zero makes [AppFrame] request a transparent window and clip itself to a
     * rounded rectangle, which is what macOS windows look like. Leave it at zero on desktops that
     * round windows themselves, or where a compositor may not be running.
     */
    val cornerRadius: Dp = 0.dp,
) {
    internal val controlsWidth: Dp
        get() =
            buttonSize.width * controlsOrder.size +
                buttonSpacing * (controlsOrder.size - 1).coerceAtLeast(0) +
                controlsPadding

    public companion object {
        /** Windows 11 / most Linux desktops: controls on the right, title on the left. */
        public val Windows: TitleBarStyle =
            TitleBarStyle(
                height = 32.dp,
                controlsAlignment = Alignment.End,
                controlsOrder = listOf(WindowControl.Minimize, WindowControl.Maximize, WindowControl.Close),
                controlsAppearance = WindowControlsAppearance.Bars,
                titleAlignment = Alignment.Start,
                buttonSize = DpSize(46.dp, 32.dp),
                buttonSpacing = 0.dp,
                controlsPadding = 0.dp,
                glyphSize = 10.dp,
                maximizeAction = MaximizeAction.Maximize,
            )

        /**
         * GNOME/KDE-ish: controls on the right, but round and with a centered title.
         *
         * Only a default — [forHost] asks the desktop itself where the buttons belong, see
         * [withGtkButtonLayout].
         */
        public val Linux: TitleBarStyle =
            Windows.copy(
                height = 38.dp,
                titleAlignment = Alignment.CenterHorizontally,
                buttonSize = DpSize(28.dp, 28.dp),
                buttonSpacing = 8.dp,
                controlsPadding = 8.dp,
            )

        /**
         * macOS: traffic lights on the left, centered title.
         *
         * The green button zooms rather than going fullscreen. macOS' own green button does go
         * fullscreen, but that is a decorated window's privilege: AppKit silently ignores
         * `toggleFullScreen:` on the borderless window an undecorated [AppFrame] is, and the
         * window would keep its size while the title bar redrew as if it had not. Zooming — what
         * an option-click on the green button does — is the closest behaviour that is real.
         *
         * `MacOs.copy(maximizeAction = MaximizeAction.Fullscreen)` still gets fullscreen, through
         * the workaround [MaximizeAction.Fullscreen] describes.
         */
        public val MacOs: TitleBarStyle =
            TitleBarStyle(
                height = 28.dp,
                controlsAlignment = Alignment.Start,
                controlsOrder = listOf(WindowControl.Close, WindowControl.Minimize, WindowControl.Maximize),
                controlsAppearance = WindowControlsAppearance.TrafficLights,
                titleAlignment = Alignment.CenterHorizontally,
                buttonSize = DpSize(12.dp, 12.dp),
                buttonSpacing = 8.dp,
                controlsPadding = 12.dp,
                glyphSize = 6.dp,
                maximizeAction = MaximizeAction.Maximize,
                cornerRadius = 10.dp,
            )

        /** The style matching [os], defaulting to the OS this app is running on. */
        public fun forHost(os: HostOs = HostOs.current): TitleBarStyle =
            when (os) {
                HostOs.MacOs -> MacOs
                HostOs.Linux -> Linux.withGtkButtonLayout(gtkButtonLayout)
                HostOs.Windows, HostOs.Unknown -> Windows
            }
    }
}

/**
 * Applies a desktop-provided button layout: which side the controls live on, and which of them the
 * desktop wants at all — GNOME, for instance, ships with Close only.
 *
 * Returns the receiver unchanged when [layout] is null or has no window controls in it.
 */
internal fun TitleBarStyle.withGtkButtonLayout(layout: DecorationLayout?): TitleBarStyle {
    if (layout == null) return this
    // Desktops that put the controls on the left keep them there alone; prefer whichever side the
    // desktop actually filled.
    val onLeft = layout.leading.isNotEmpty() && layout.trailing.isEmpty()
    val controls = if (onLeft) layout.leading else layout.trailing
    if (controls.isEmpty()) return this

    return copy(
        controlsAlignment = if (onLeft) Alignment.Start else Alignment.End,
        controlsOrder = controls,
    )
}
