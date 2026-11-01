package io.github.youndie.appframe

import java.awt.Frame
import java.awt.Insets
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent

/**
 * True when maximizing this window would cover the taskbar unless it is told not to.
 *
 * Windows maximizes a window without a caption — which is what `undecorated = true` produces — to
 * the whole monitor rather than to its work area, so a maximized [AppFrame] would hide the taskbar
 * underneath it. macOS and the Linux window managers keep to the work area by themselves.
 */
internal fun needsWorkAreaBounds(
    os: HostOs,
    undecorated: Boolean,
): Boolean = os == HostOs.Windows && undecorated

/** The part of a screen with [screen] bounds that its taskbars and docks leave free. */
internal fun workAreaOf(
    screen: Rectangle,
    insets: Insets,
): Rectangle =
    Rectangle(
        screen.x + insets.left,
        screen.y + insets.top,
        screen.width - insets.left - insets.right,
        screen.height - insets.top - insets.bottom,
    )

/**
 * Keeps [Frame.getMaximizedBounds] on the work area of the screen the window is on, where
 * [needsWorkAreaBounds] says it has to be, and returns what undoes it.
 *
 * Refreshed on every move, so a window dragged to another monitor maximizes onto that monitor's
 * work area, and whatever route maximizes it — the zoom button, a double click, the placement set
 * by the application — finds the bounds already in place.
 */
internal fun Frame.keepMaximizedBoundsOnWorkArea(os: HostOs = HostOs.current): () -> Unit {
    if (!needsWorkAreaBounds(os, isUndecorated)) return {}

    fun update() {
        val configuration = graphicsConfiguration ?: return
        maximizedBounds = workAreaOf(configuration.bounds, Toolkit.getDefaultToolkit().getScreenInsets(configuration))
    }

    val listener =
        object : ComponentAdapter() {
            override fun componentMoved(e: ComponentEvent) = update()

            override fun componentShown(e: ComponentEvent) = update()
        }
    update()
    addComponentListener(listener)
    return { removeComponentListener(listener) }
}
