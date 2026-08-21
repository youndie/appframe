package ru.workinprogress.appframe

import androidx.compose.runtime.Immutable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.type

/**
 * A key combination that triggers a menu item, whether or not its menu is open.
 *
 * Deliberately not [androidx.compose.ui.window.KeyShortcut]: that one keeps its key and modifiers
 * `internal`, so a shortcut built from it can neither be matched against a [KeyEvent] nor rendered
 * next to the item.
 *
 * Use [primary] for the modifier the platform expects — Command on macOS, Ctrl everywhere else.
 */
@Immutable
public data class MenuShortcut(
    val key: Key,
    val ctrl: Boolean = false,
    val meta: Boolean = false,
    val alt: Boolean = false,
    val shift: Boolean = false,
) {
    public companion object {
        /**
         * The shortcut a menu would normally use for [key]: `⌘key` on macOS, `Ctrl+key` elsewhere.
         *
         * [os] is resolved once at call time, so a shortcut built at composition follows the host.
         */
        public fun primary(
            key: Key,
            shift: Boolean = false,
            alt: Boolean = false,
            os: HostOs = HostOs.current,
        ): MenuShortcut =
            MenuShortcut(
                key = key,
                ctrl = os != HostOs.MacOs,
                meta = os == HostOs.MacOs,
                alt = alt,
                shift = shift,
            )
    }
}

/**
 * Whether [event] is this exact combination.
 *
 * Matched on key *down* and on every modifier, including the ones the shortcut does not ask for:
 * `⌘S` must not fire on `⇧⌘S`, which is somebody else's shortcut.
 */
internal fun MenuShortcut.matches(event: KeyEvent): Boolean =
    event.type == KeyEventType.KeyDown &&
        event.key == key &&
        event.isCtrlPressed == ctrl &&
        event.isMetaPressed == meta &&
        event.isAltPressed == alt &&
        event.isShiftPressed == shift

/**
 * How the shortcut is written next to the item: `⇧⌘N` on macOS, `Ctrl+Shift+N` elsewhere.
 *
 * The modifier order is the platform's own — macOS prints Control, Option, Shift, Command in that
 * order and no separators, everyone else joins with `+`.
 */
internal fun MenuShortcut.label(os: HostOs = HostOs.current): String {
    val name = keyName(key)
    return if (os == HostOs.MacOs) {
        buildString {
            if (ctrl) append('⌃')
            if (alt) append('⌥')
            if (shift) append('⇧')
            if (meta) append('⌘')
            append(name)
        }
    } else {
        buildString {
            if (ctrl) append("Ctrl+")
            if (meta) append("Meta+")
            if (alt) append("Alt+")
            if (shift) append("Shift+")
            append(name)
        }
    }
}

/** AWT already knows every key's localized name, and [Key] is an AWT key code on desktop. */
private fun keyName(key: Key): String = java.awt.event.KeyEvent.getKeyText(key.nativeKeyCode)

/**
 * The shortcuts of every item currently in the menu tree, open or not.
 *
 * A menu's items only exist in the composition while its dropdown is open, so the bar is composed a
 * second time into a scope that emits nothing and registers here instead — that pass is what makes
 * `⌘S` work without opening the File menu first.
 */
internal class MenuShortcutRegistry {
    private val entries = mutableListOf<Entry>()

    fun register(entry: Entry) {
        entries += entry
    }

    fun unregister(entry: Entry) {
        entries -= entry
    }

    /**
     * Runs the item bound to [event], if any, and reports whether it did.
     *
     * The last registration wins: a submenu registered after the menu that holds it is the more
     * specific binding of the two.
     */
    fun handle(event: KeyEvent): Boolean {
        val entry = entries.lastOrNull { it.shortcut.matches(event) } ?: return false
        entry.onTrigger()
        return true
    }

    class Entry(val shortcut: MenuShortcut, val onTrigger: () -> Unit)
}
