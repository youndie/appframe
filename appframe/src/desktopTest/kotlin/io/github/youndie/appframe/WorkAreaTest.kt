package io.github.youndie.appframe

import java.awt.Insets
import java.awt.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkAreaTest {
    @Test
    fun `only an undecorated window on windows needs its maximized bounds set`() {
        assertTrue(needsWorkAreaBounds(HostOs.Windows, undecorated = true))
        assertFalse(needsWorkAreaBounds(HostOs.Windows, undecorated = false))

        // Everywhere else the window manager keeps a maximized window off the taskbar and dock.
        assertFalse(needsWorkAreaBounds(HostOs.MacOs, undecorated = true))
        assertFalse(needsWorkAreaBounds(HostOs.Linux, undecorated = true))
        assertFalse(needsWorkAreaBounds(HostOs.Unknown, undecorated = true))
    }

    @Test
    fun `the work area is the screen less its taskbar`() {
        // A 1707x1067 screen with a 30 px taskbar along the bottom — the machine this was found on.
        assertEquals(
            Rectangle(0, 0, 1707, 1037),
            workAreaOf(Rectangle(0, 0, 1707, 1067), Insets(0, 0, 30, 0)),
        )
    }

    @Test
    fun `a second monitor keeps its own origin`() {
        assertEquals(
            Rectangle(1968, 40, 1872, 1040),
            workAreaOf(Rectangle(1920, 0, 1920, 1080), Insets(40, 48, 0, 0)),
        )
    }
}
