package io.github.youndie.appframe

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.WindowState

/**
 * What [AppFrame] lays out inside its window, with the window left out so it can be drawn — and
 * tested — without one: [background] under everything, then a column of [titleBar] and [content].
 *
 * One shape whether there is a [background] or not. A layout that wrapped itself in a [Box] only
 * when it needed one would put the content somewhere else in the composition the moment a
 * background is switched on — a theme with a texture after one without — and the content would
 * lose every bit of state it remembers.
 */
@Composable
internal fun FrameLayout(
    background: (@Composable BoxScope.() -> Unit)?,
    titleBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier) {
        if (background != null) {
            background()
        }
        Column(Modifier.fillMaxSize()) {
            titleBar()
            content()
        }
    }
}

/**
 * [TitleBar] as [AppFrame] draws it.
 *
 * Over a caller's background ([overBackground]) the bar draws no surface of its own, so the
 * background shows through it; the bar still takes every press on it, as a `Surface` does. Without
 * [showTitle] it draws no title — the window keeps one, because [AppFrame] hands its title to the
 * window directly rather than through here.
 */
@Composable
internal fun FrameTitleBar(
    state: WindowState,
    onCloseRequest: () -> Unit,
    title: String,
    showTitle: Boolean,
    icon: Painter?,
    style: TitleBarStyle,
    overBackground: Boolean,
    menuBar: (@Composable MenuBarScope.() -> Unit)?,
    actions: @Composable RowScope.() -> Unit,
) {
    TitleBar(
        state = state,
        onCloseRequest = onCloseRequest,
        title = if (showTitle) title else "",
        icon = icon,
        style = style,
        // `TitleBar`'s own default, spelled again: the colour is chosen here, so a default argument
        // cannot be. `FrameLayoutTest` compares the two, pixel for pixel.
        color = if (overBackground) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant,
        menuBar = menuBar,
        actions = actions,
    )
}
