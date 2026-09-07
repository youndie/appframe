package io.github.youndie.appframe

import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FullscreenTest {
    @Test
    fun `only an undecorated window on macos needs the workaround`() {
        assertTrue(needsFullscreenFallback(HostOs.MacOs, undecorated = true))
        assertFalse(needsFullscreenFallback(HostOs.MacOs, undecorated = false))

        // Everywhere else `WindowPlacement.Fullscreen` resizes an undecorated window by itself.
        assertFalse(needsFullscreenFallback(HostOs.Windows, undecorated = true))
        assertFalse(needsFullscreenFallback(HostOs.Linux, undecorated = true))
        assertFalse(needsFullscreenFallback(HostOs.Unknown, undecorated = true))
    }

    @Test
    fun `the title bar counts either route as fullscreen`() {
        val fallback = FullscreenFallback()

        assertFalse(WindowState(placement = WindowPlacement.Floating).isFullscreenWith(fallback))
        assertFalse(WindowState(placement = WindowPlacement.Maximized).isFullscreenWith(fallback))
        assertTrue(WindowState(placement = WindowPlacement.Fullscreen).isFullscreenWith(fallback))
    }

    @Test
    fun `macos zooms rather than pretending to go fullscreen`() {
        // The green button used to ask for a fullscreen macOS refuses an undecorated window, which
        // left it changing the glyph and nothing else.
        assertEquals(MaximizeAction.Maximize, TitleBarStyle.MacOs.maximizeAction)
        assertEquals(MaximizeAction.Maximize, TitleBarStyle.forHost(HostOs.MacOs).maximizeAction)
    }

    @Test
    fun `the placement-only toggle still moves between floating and the requested placement`() {
        val state = WindowState(placement = WindowPlacement.Floating)

        state.toggleMaximized(MaximizeAction.Maximize)
        assertEquals(WindowPlacement.Maximized, state.placement)

        state.toggleMaximized(MaximizeAction.Maximize)
        assertEquals(WindowPlacement.Floating, state.placement)

        state.toggleMaximized(MaximizeAction.Fullscreen)
        assertEquals(WindowPlacement.Fullscreen, state.placement)
    }

    @Test
    fun `without a window the zoom toggle is the placement toggle`() {
        val state = WindowState(placement = WindowPlacement.Floating)
        val fallback = FullscreenFallback()

        state.toggleZoom(MaximizeAction.Fullscreen, window = null, fullscreen = fallback)

        assertEquals(WindowPlacement.Fullscreen, state.placement)
        assertFalse(fallback.active)
    }
}
