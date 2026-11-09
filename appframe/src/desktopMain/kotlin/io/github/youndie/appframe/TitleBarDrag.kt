package io.github.youndie.appframe

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.FrameWindowScope
import java.awt.Frame
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Rectangle
import java.awt.Window
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * What [AppFrame] puts around its title bar so the window can be dragged by it.
 *
 * Compose's [WindowDraggableArea] everywhere but Windows. There it moves the window on any motion
 * with the button held — a pixel of a hand's jitter during a click included — and a maximized
 * window it moves stays maximized: the size of the screen, off the screen's edges, with the restore
 * glyph still on the bar and a double click that restores rather than maximizes. A Windows title
 * bar does neither: it waits for the pointer to travel past a threshold, and a maximized window is
 * restored as the drag begins and carried under the pointer at the same relative position. This
 * does the same. macOS moves a zoomed window as it is.
 *
 * On Linux what [WindowDraggableArea] does depends on the runtime, not on the desktop. On the
 * JetBrains Runtime it hands the move to the window manager (`JBR.isWindowMoveSupported()`, then
 * `WindowMove.startMovingTogetherWithMouse`), which knows the rule itself. On any other JDK —
 * Temurin included — it moves the window by hand from the first press: `Window.setLocation` from
 * `MouseInfo` on every mouse drag the window receives, with no threshold and no restore of a
 * maximized window. Neither path has been tried on a Wayland session.
 */
@Composable
internal fun FrameWindowScope.TitleBarDragArea(content: @Composable () -> Unit) {
    if (HostOs.current != HostOs.Windows) {
        WindowDraggableArea(content = content)
        return
    }

    val window = window
    Box(
        modifier =
            Modifier.pointerInput(window) {
                awaitEachGesture {
                    awaitFirstDown()
                    dragWindow(window)
                }
            },
        propagateMinConstraints = true,
    ) {
        content()
    }
}

/**
 * How far, in screen pixels, the pointer travels before a press on the bar becomes a drag —
 * Windows' own `SM_CXDRAG` at its default.
 */
internal const val DRAG_THRESHOLD: Int = 4

/**
 * Follows the pointer from a press until its release.
 *
 * Positions are read off the screen, not off the event: the window moves under the pointer, so a
 * position relative to it says nothing about where the window should go.
 */
private suspend fun AwaitPointerEventScope.dragWindow(window: Window) {
    val start = pointerOnScreen() ?: return
    val grab = Grab.of(start, window.bounds)
    var moving = false

    while (true) {
        val event = awaitPointerEvent()
        if (event.changes.none { it.pressed }) return
        val pointer = pointerOnScreen() ?: continue

        if (!moving) {
            if (!grab.isDrag(start, pointer)) continue
            moving = true
            // Restored here rather than through `WindowState.placement`, which Compose applies a
            // frame later — the moves in between would carry the window still maximized.
            if (window is Frame && window.extendedState and Frame.MAXIMIZED_BOTH != 0) {
                window.extendedState = window.extendedState and Frame.MAXIMIZED_BOTH.inv()
            }
        }

        // Read every time: the width is the maximized one until the restore has landed.
        val location = grab.windowLocation(pointer, window.width)
        window.setLocation(location.x, location.y)
    }
}

private fun pointerOnScreen(): Point? = MouseInfo.getPointerInfo()?.location

/**
 * Where on the window a drag took hold of it: across, as a share of the width, and down, in pixels.
 *
 * A share rather than an offset because the window can change width under the pointer — a
 * maximized one is restored as its drag begins — and the pointer should stay over the same part of
 * the bar, the way it does on a Windows title bar.
 */
internal class Grab(
    private val across: Double,
    private val down: Int,
) {
    fun isDrag(
        start: Point,
        pointer: Point,
    ): Boolean = abs(pointer.x - start.x) > DRAG_THRESHOLD || abs(pointer.y - start.y) > DRAG_THRESHOLD

    fun windowLocation(
        pointer: Point,
        width: Int,
    ): Point = Point(pointer.x - (across * width).roundToInt(), pointer.y - down)

    companion object {
        fun of(
            pointer: Point,
            window: Rectangle,
        ): Grab =
            Grab(
                across = if (window.width > 0) (pointer.x - window.x).toDouble() / window.width else 0.0,
                down = pointer.y - window.y,
            )
    }
}
