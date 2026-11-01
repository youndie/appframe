package io.github.youndie.appframe

import java.awt.Point
import java.awt.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TitleBarDragTest {
    @Test
    fun `a window of the same width keeps the pointer where it took hold`() {
        val grab = Grab.of(Point(348, 60), Rectangle(48, 48, 1200, 760))

        assertEquals(Point(148, 248), grab.windowLocation(Point(448, 260), width = 1200))
    }

    @Test
    fun `a maximized window restored under the pointer keeps the same share of the bar`() {
        // Taken three quarters of the way across a maximized 1707 px window, restored to 1200.
        val grab = Grab.of(Point(1280, 12), Rectangle(0, 0, 1707, 1037))

        val location = grab.windowLocation(Point(1300, 212), width = 1200)

        assertEquals(Point(1300 - 900, 200), location)
    }

    @Test
    fun `a click that wobbles a pixel or two is not a drag`() {
        val start = Point(300, 20)
        val grab = Grab.of(start, Rectangle(0, 0, 1200, 760))

        assertFalse(grab.isDrag(start, Point(302, 21)))
        assertFalse(grab.isDrag(start, Point(300 + DRAG_THRESHOLD, 20 - DRAG_THRESHOLD)))
        assertTrue(grab.isDrag(start, Point(300 + DRAG_THRESHOLD + 1, 20)))
        assertTrue(grab.isDrag(start, Point(300, 20 + DRAG_THRESHOLD + 1)))
    }
}
