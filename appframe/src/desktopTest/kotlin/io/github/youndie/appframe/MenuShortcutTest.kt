package io.github.youndie.appframe

import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Compose's own key-event factory. Marked internal to Compose because apps have no reason to
 * synthesize input — a test that has to check what a shortcut matches does.
 */
@OptIn(InternalComposeUiApi::class)
private fun keyDown(
    key: Key,
    ctrl: Boolean = false,
    meta: Boolean = false,
    alt: Boolean = false,
    shift: Boolean = false,
    type: KeyEventType = KeyEventType.KeyDown,
): KeyEvent =
    KeyEvent(
        key = key,
        type = type,
        isCtrlPressed = ctrl,
        isMetaPressed = meta,
        isAltPressed = alt,
        isShiftPressed = shift,
    )

class MenuShortcutTest {
    @Test
    fun `primary is command on macos and ctrl everywhere else`() {
        assertEquals(
            MenuShortcut(Key.N, meta = true),
            MenuShortcut.primary(Key.N, os = HostOs.MacOs),
        )
        assertEquals(
            MenuShortcut(Key.N, ctrl = true),
            MenuShortcut.primary(Key.N, os = HostOs.Windows),
        )
        assertEquals(
            MenuShortcut(Key.N, ctrl = true),
            MenuShortcut.primary(Key.N, os = HostOs.Linux),
        )
        assertEquals(
            MenuShortcut(Key.N, ctrl = true, shift = true),
            MenuShortcut.primary(Key.N, shift = true, os = HostOs.Unknown),
        )
    }

    @Test
    fun `matches the exact combination`() {
        val save = MenuShortcut(Key.S, meta = true)

        assertTrue(save.matches(keyDown(Key.S, meta = true)))
        assertFalse(save.matches(keyDown(Key.S)))
        assertFalse(save.matches(keyDown(Key.D, meta = true)))
        assertFalse(save.matches(keyDown(Key.S, ctrl = true)))
    }

    @Test
    fun `an extra modifier belongs to somebody else's shortcut`() {
        val save = MenuShortcut(Key.S, meta = true)

        assertFalse(save.matches(keyDown(Key.S, meta = true, shift = true)))
        assertTrue(MenuShortcut(Key.S, meta = true, shift = true).matches(keyDown(Key.S, meta = true, shift = true)))
    }

    @Test
    fun `fires on key down only`() {
        val save = MenuShortcut(Key.S, meta = true)

        assertFalse(save.matches(keyDown(Key.S, meta = true, type = KeyEventType.KeyUp)))
        assertFalse(save.matches(keyDown(Key.S, meta = true, type = KeyEventType.Unknown)))
    }

    @Test
    fun `label follows the platform's own notation`() {
        val shortcut = MenuShortcut(Key.N, meta = true, shift = true)

        assertEquals("⇧⌘N", shortcut.label(HostOs.MacOs))
        assertEquals("Meta+Shift+N", shortcut.label(HostOs.Windows))
        assertEquals("Ctrl+Alt+N", MenuShortcut(Key.N, ctrl = true, alt = true).label(HostOs.Linux))
        assertEquals("⌃⌥N", MenuShortcut(Key.N, ctrl = true, alt = true).label(HostOs.MacOs))
    }
}

class MenuShortcutRegistryTest {
    @Test
    fun `runs the item bound to the event`() {
        val registry = MenuShortcutRegistry()
        var fired = 0
        registry.register(MenuShortcutRegistry.Entry(MenuShortcut(Key.N, meta = true)) { fired++ })

        assertTrue(registry.handle(keyDown(Key.N, meta = true)))
        assertEquals(1, fired)
    }

    @Test
    fun `leaves an unbound event alone`() {
        val registry = MenuShortcutRegistry()
        registry.register(MenuShortcutRegistry.Entry(MenuShortcut(Key.N, meta = true)) { })

        assertFalse(registry.handle(keyDown(Key.M, meta = true)))
    }

    @Test
    fun `an unregistered item stops firing`() {
        val registry = MenuShortcutRegistry()
        var fired = 0
        val entry = MenuShortcutRegistry.Entry(MenuShortcut(Key.N, meta = true)) { fired++ }

        registry.register(entry)
        registry.unregister(entry)

        assertFalse(registry.handle(keyDown(Key.N, meta = true)))
        assertEquals(0, fired)
    }

    @Test
    fun `the last registration of a combination wins`() {
        val registry = MenuShortcutRegistry()
        val fired = mutableListOf<String>()
        registry.register(MenuShortcutRegistry.Entry(MenuShortcut(Key.N, meta = true)) { fired += "menu" })
        registry.register(MenuShortcutRegistry.Entry(MenuShortcut(Key.N, meta = true)) { fired += "submenu" })

        registry.handle(keyDown(Key.N, meta = true))

        assertEquals(listOf("submenu"), fired)
    }
}
