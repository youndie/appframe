package ru.workinprogress.appframe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay

/**
 * Declares the menus of an [AppFrame] title bar.
 *
 * Mirrors [androidx.compose.ui.window.MenuBarScope] so an existing `MenuBar { … }` block ports over
 * unchanged apart from its shortcut type — but the menus are Compose, drawn inside the title bar
 * with the app's own Material theme, rather than a Swing `JMenuBar` strip below it.
 */
public interface MenuBarScope {
    /** A top-level menu. Its [content] is composed when the menu is opened. */
    @Composable
    public fun Menu(
        text: String,
        enabled: Boolean = true,
        content: @Composable MenuScope.() -> Unit,
    )
}

/** Declares the items of a single menu. */
public interface MenuScope {
    /** A plain item. [shortcut] fires it whether or not the menu is open. */
    @Composable
    public fun Item(
        text: String,
        shortcut: MenuShortcut? = null,
        enabled: Boolean = true,
        onClick: () -> Unit,
    )

    /** An item carrying a tick. Clicking it reports the flipped [checked]. */
    @Composable
    public fun CheckboxItem(
        text: String,
        checked: Boolean,
        shortcut: MenuShortcut? = null,
        enabled: Boolean = true,
        onCheckedChange: (Boolean) -> Unit,
    )

    /** One option of a group; the [selected] one carries a dot. */
    @Composable
    public fun RadioButtonItem(
        text: String,
        selected: Boolean,
        shortcut: MenuShortcut? = null,
        enabled: Boolean = true,
        onClick: () -> Unit,
    )

    /** A rule between two groups of items. */
    @Composable
    public fun Separator()

    /** A nested menu, opened by hovering its item. */
    @Composable
    public fun Menu(
        text: String,
        enabled: Boolean = true,
        content: @Composable MenuScope.() -> Unit,
    )
}

/**
 * The row of menu buttons drawn in the title bar.
 *
 * Public so it can be placed in a hand-rolled title bar; [AppFrame] and [TitleBar] take a
 * `menuBar` lambda and call this themselves.
 */
@Composable
public fun AppMenuBar(
    modifier: Modifier = Modifier,
    content: @Composable MenuBarScope.() -> Unit,
) {
    val bar = remember { MenuBarState() }
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        content(BarScope(bar))
    }
}

/**
 * Registers every shortcut in [content] without drawing anything.
 *
 * Composed alongside the visible bar: a closed menu has no items in the composition, so this second
 * pass is the only thing that knows `⌘S` exists before the File menu is ever opened.
 */
@Composable
internal fun MenuShortcuts(
    registry: MenuShortcutRegistry,
    content: @Composable MenuBarScope.() -> Unit,
) {
    content(CollectingBarScope(registry))
}

// -- state ------------------------------------------------------------------------------------

/**
 * Which top-level menu is open.
 *
 * Indices are hand-assigned as the menus first compose, which is what lets a menu know whether the
 * open one is itself without the caller having to name it.
 */
private class MenuBarState {
    var openIndex by mutableIntStateOf(NONE)
    private var assigned = 0

    val isOpen: Boolean get() = openIndex != NONE

    fun nextIndex(): Int = assigned++

    fun close() {
        openIndex = NONE
    }

    companion object {
        const val NONE: Int = -1
    }
}

/** Which submenu of one open menu is itself open — the same trick, one level down. */
private class MenuState {
    var openIndex by mutableIntStateOf(MenuBarState.NONE)
    private var assigned = 0

    fun nextIndex(): Int = assigned++

    fun closeSubmenus() {
        openIndex = MenuBarState.NONE
    }
}

// -- rendering --------------------------------------------------------------------------------

private class BarScope(
    private val bar: MenuBarState,
) : MenuBarScope {
    @Composable
    override fun Menu(
        text: String,
        enabled: Boolean,
        content: @Composable MenuScope.() -> Unit,
    ) {
        val index = remember { bar.nextIndex() }
        val expanded = bar.openIndex == index
        val interaction = remember { MutableInteractionSource() }
        val hovered by interaction.collectIsHoveredAsState()

        // Once any menu is open, sliding along the bar switches to the one under the pointer —
        // every desktop menu bar behaves this way, and clicking each title in turn does not.
        LaunchedEffect(hovered, bar.isOpen) {
            if (hovered && enabled && bar.isOpen) bar.openIndex = index
        }

        Box {
            MenuBarButton(
                text = text,
                enabled = enabled,
                open = expanded,
                interaction = interaction,
                onClick = { bar.openIndex = if (expanded) MenuBarState.NONE else index },
            )
            DropdownMenu(expanded = expanded, onDismissRequest = bar::close) {
                val menu = remember { MenuState() }
                content(ItemScope(menu = menu, dismissAll = bar::close))
            }
        }
    }
}

@Composable
private fun MenuBarButton(
    text: String,
    enabled: Boolean,
    open: Boolean,
    interaction: MutableInteractionSource,
    onClick: () -> Unit,
) {
    val hovered by interaction.collectIsHoveredAsState()
    val background =
        when {
            !enabled -> Color.Transparent
            open -> LocalContentColor.current.copy(alpha = 0.16f)
            hovered -> LocalContentColor.current.copy(alpha = 0.08f)
            else -> Color.Transparent
        }

    Box(
        modifier =
            Modifier
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick,
                ).background(background, RoundedCornerShape(MenuButtonCorner))
                .padding(horizontal = MenuButtonPadding, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = LocalContentColor.current.copy(alpha = if (enabled) 1f else 0.38f),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

private class ItemScope(
    private val menu: MenuState,
    private val dismissAll: () -> Unit,
) : MenuScope {
    @Composable
    override fun Item(
        text: String,
        shortcut: MenuShortcut?,
        enabled: Boolean,
        onClick: () -> Unit,
    ) {
        MenuItemRow(text = text, shortcut = shortcut, enabled = enabled, menu = menu) {
            dismissAll()
            onClick()
        }
    }

    @Composable
    override fun CheckboxItem(
        text: String,
        checked: Boolean,
        shortcut: MenuShortcut?,
        enabled: Boolean,
        onCheckedChange: (Boolean) -> Unit,
    ) {
        MenuItemRow(
            text = text,
            shortcut = shortcut,
            enabled = enabled,
            menu = menu,
            mark = if (checked) MenuGlyph.Check else null,
        ) {
            dismissAll()
            onCheckedChange(!checked)
        }
    }

    @Composable
    override fun RadioButtonItem(
        text: String,
        selected: Boolean,
        shortcut: MenuShortcut?,
        enabled: Boolean,
        onClick: () -> Unit,
    ) {
        MenuItemRow(
            text = text,
            shortcut = shortcut,
            enabled = enabled,
            menu = menu,
            mark = if (selected) MenuGlyph.Dot else null,
        ) {
            dismissAll()
            onClick()
        }
    }

    @Composable
    override fun Separator() {
        HorizontalDivider(Modifier.padding(vertical = 4.dp))
    }

    @Composable
    override fun Menu(
        text: String,
        enabled: Boolean,
        content: @Composable MenuScope.() -> Unit,
    ) {
        Submenu(text = text, enabled = enabled, parent = menu, dismissAll = dismissAll, content = content)
    }
}

/**
 * One row of a dropdown.
 *
 * The mark column is reserved whether or not this item has a mark, so the labels of a menu mixing
 * plain and checkable items still line up — as they do in every native menu.
 */
@Composable
private fun MenuItemRow(
    text: String,
    shortcut: MenuShortcut?,
    enabled: Boolean,
    menu: MenuState,
    mark: MenuGlyph? = null,
    trailing: MenuGlyph? = null,
    interaction: MutableInteractionSource = remember { MutableInteractionSource() },
    onClick: () -> Unit,
) {
    val hovered by interaction.collectIsHoveredAsState()

    // Hovering a plain item folds away whichever submenu of this menu was left open.
    LaunchedEffect(hovered) {
        if (hovered && trailing == null) menu.closeSubmenus()
    }

    DropdownMenuItem(
        text = { Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        leadingIcon = {
            Box(Modifier.size(MarkSize), contentAlignment = Alignment.Center) {
                if (mark != null) {
                    MenuItemGlyph(glyph = mark, color = LocalContentColor.current, size = MarkSize)
                }
            }
        },
        trailingIcon =
            when {
                trailing != null -> {
                    {
                        MenuItemGlyph(
                            glyph = trailing,
                            color = LocalContentColor.current,
                            size = MarkSize,
                        )
                    }
                }

                shortcut != null -> {
                    {
                        Text(
                            text = shortcut.label(),
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalContentColor.current.copy(alpha = 0.6f),
                        )
                    }
                }

                else -> {
                    null
                }
            },
    )
}

@Composable
private fun Submenu(
    text: String,
    enabled: Boolean,
    parent: MenuState,
    dismissAll: () -> Unit,
    content: @Composable MenuScope.() -> Unit,
) {
    val index = remember { parent.nextIndex() }
    val open = parent.openIndex == index
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    var size by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    LaunchedEffect(hovered, enabled) {
        if (hovered && enabled) {
            delay(SUBMENU_HOVER_DELAY_MILLIS)
            parent.openIndex = index
        }
    }

    Box(Modifier.onSizeChanged { size = it }) {
        MenuItemRow(
            text = text,
            shortcut = null,
            enabled = enabled,
            menu = parent,
            trailing = MenuGlyph.Arrow,
            interaction = interaction,
            onClick = { parent.openIndex = if (open) MenuBarState.NONE else index },
        )
        DropdownMenu(
            expanded = open,
            onDismissRequest = parent::closeSubmenus,
            // Anchored to the right edge of its own row rather than below it, which is where a
            // submenu belongs; the popup flips itself when there is no room on that side.
            offset =
                with(density) {
                    DpOffset(x = size.width.toDp(), y = -size.height.toDp())
                },
            // A focusable popup takes focus from the menu that owns it, and that menu reads the
            // loss as a dismissal — opening a submenu would close the whole tree, submenu included.
            // The menu around it is the one holding focus and handling clicks outside.
            properties = PopupProperties(focusable = false),
        ) {
            val nested = remember { MenuState() }
            content(ItemScope(menu = nested, dismissAll = dismissAll))
        }
    }
}

/** Spacer used by [TitleBar] to keep the menus off the window controls. */
@Composable
internal fun RowScope.MenuBarSpacer() {
    Spacer(Modifier.width(MenuButtonPadding))
}

private val MenuButtonPadding = 10.dp
private val MenuButtonCorner = 6.dp
private val MarkSize = 16.dp

/** Long enough that dragging the pointer past an item on the way down does not open it. */
private const val SUBMENU_HOVER_DELAY_MILLIS = 180L

// -- shortcut collection ------------------------------------------------------------------------

private class CollectingBarScope(
    private val registry: MenuShortcutRegistry,
) : MenuBarScope {
    @Composable
    override fun Menu(
        text: String,
        enabled: Boolean,
        content: @Composable MenuScope.() -> Unit,
    ) {
        content(CollectingItemScope(registry, enabled))
    }
}

/**
 * Emits nothing; every item it sees registers its shortcut for as long as it stays in the tree.
 *
 * [reachable] carries the enclosing menus' `enabled` down: a shortcut inside a disabled menu is as
 * dead as the menu is.
 */
private class CollectingItemScope(
    private val registry: MenuShortcutRegistry,
    private val reachable: Boolean,
) : MenuScope {
    @Composable
    override fun Item(
        text: String,
        shortcut: MenuShortcut?,
        enabled: Boolean,
        onClick: () -> Unit,
    ) {
        Register(registry, shortcut, enabled && reachable, onClick)
    }

    @Composable
    override fun CheckboxItem(
        text: String,
        checked: Boolean,
        shortcut: MenuShortcut?,
        enabled: Boolean,
        onCheckedChange: (Boolean) -> Unit,
    ) {
        Register(registry, shortcut, enabled && reachable) { onCheckedChange(!checked) }
    }

    @Composable
    override fun RadioButtonItem(
        text: String,
        selected: Boolean,
        shortcut: MenuShortcut?,
        enabled: Boolean,
        onClick: () -> Unit,
    ) {
        Register(registry, shortcut, enabled && reachable, onClick)
    }

    @Composable
    override fun Separator() {
        // Nothing to bind.
    }

    @Composable
    override fun Menu(
        text: String,
        enabled: Boolean,
        content: @Composable MenuScope.() -> Unit,
    ) {
        content(CollectingItemScope(registry, enabled && reachable))
    }
}

@Composable
private fun Register(
    registry: MenuShortcutRegistry,
    shortcut: MenuShortcut?,
    enabled: Boolean,
    onTrigger: () -> Unit,
) {
    if (shortcut == null) return
    // The item is recomposed with a fresh lambda on every state change; the registration must not
    // be, or a shortcut would unbind and rebind on each keystroke of whatever it acts on.
    val current by rememberUpdatedState(onTrigger)

    DisposableEffect(registry, shortcut, enabled) {
        if (!enabled) return@DisposableEffect onDispose { }

        val entry = MenuShortcutRegistry.Entry(shortcut) { current() }
        registry.register(entry)
        onDispose { registry.unregister(entry) }
    }
}
