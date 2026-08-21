package ru.workinprogress.appframe

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import java.awt.Window

/**
 * The bar drawn above [AppFrame]'s content: title, optional [actions] and the window controls.
 *
 * Public so it can be embedded in a hand-rolled [androidx.compose.ui.window.Window] as well.
 *
 * [icon] is drawn only next to a start-aligned title, i.e. the Windows layout — macOS and GNOME
 * don't put an app icon in the title bar, so it is ignored by those styles.
 *
 * A [menuBar] is laid out after the icon, the way a single-row title bar with menus is arranged
 * everywhere from VS Code to a GNOME header bar. It also takes over where the title goes: with
 * menus in the row the title is centered on what they leave over and drawn a step back, whatever
 * [TitleBarStyle.titleAlignment] says. A start-aligned title would otherwise sit right against the
 * last menu in the same size and weight, and read as one more menu.
 */
@Composable
public fun TitleBar(
    state: WindowState,
    onCloseRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "",
    icon: Painter? = null,
    style: TitleBarStyle = TitleBarStyle.forHost(),
    color: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    menuBar: (@Composable MenuBarScope.() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val fullscreen = rememberFullscreenFallback()

    Surface(color = color, contentColor = contentColor, modifier = modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(style.height)
                .doubleClickToToggleMaximized(
                    state = state,
                    action = style.maximizeAction,
                    window = LocalAppFrameWindow.current,
                    fullscreen = fullscreen,
                ),
        ) {
            if (style.titleAlignment == Alignment.CenterHorizontally && menuBar == null) {
                // Centered on the window, not on the space left over by the controls, so pad both
                // sides by the width the controls reserve.
                TitleText(
                    title = title,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(horizontal = style.controlsWidth),
                )
            }

            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                if (style.controlsAlignment == Alignment.Start) {
                    Spacer(Modifier.width(style.controlsPadding))
                    WindowControls(state, style, fullscreen, onCloseRequest)
                }

                val startAligned = style.titleAlignment == Alignment.Start

                if (startAligned) {
                    Spacer(Modifier.width(LeadingInset))
                    if (icon != null) {
                        Image(
                            painter = icon,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }

                if (menuBar != null) {
                    if (!startAligned) Spacer(Modifier.width(LeadingInset))
                    AppMenuBar(content = menuBar)
                    MenuBarSpacer()
                }

                when {
                    // Menus override [TitleBarStyle.titleAlignment]. A start-aligned title lands
                    // right after the last menu, in the same size and weight the menu labels use,
                    // and reads as one more of them — so with menus the title is pushed into the
                    // middle of whatever they leave over, and stepped back the way a title bar
                    // draws a window title that is not the row's main content. VS Code does both.
                    menuBar != null ->
                        TitleText(
                            title = title,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                            alpha = SecondaryTitleAlpha,
                        )

                    // The title itself soaks up the free space: a separate weighted spacer would
                    // split it with the (unfilled) title slot and leave a gap before the controls.
                    startAligned ->
                        TitleText(title = title, textAlign = TextAlign.Start, modifier = Modifier.weight(1f))

                    else -> Spacer(Modifier.weight(1f))
                }

                actions()

                if (style.controlsAlignment == Alignment.End) {
                    WindowControls(state, style, fullscreen, onCloseRequest)
                    Spacer(Modifier.width(style.controlsPadding))
                }
            }
        }
    }
}

/** Gap between the window edge (or app icon) and a start-aligned title. */
private val LeadingInset = 12.dp

/** How far the title steps back from the menu labels when it shares the row with them. */
private const val SecondaryTitleAlpha = 0.7f

@Composable
private fun TitleText(
    title: String,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    Text(
        text = title,
        modifier = modifier,
        color = LocalContentColor.current.copy(alpha = alpha),
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = LocalTextStyle.current.merge(MaterialTheme.typography.labelLarge),
    )
}

/**
 * A double click anywhere on the bar toggles the window, matching every desktop platform.
 *
 * Written by hand rather than with `combinedClickable` because the bar sits inside a
 * [androidx.compose.foundation.window.WindowDraggableArea], which already claims drag gestures.
 */
private fun Modifier.doubleClickToToggleMaximized(
    state: WindowState,
    action: MaximizeAction,
    window: Window?,
    fullscreen: FullscreenFallback,
): Modifier =
    pointerInput(state, action, window, fullscreen) {
        awaitEachGesture {
            awaitFirstDown()
            val firstUp = waitForUpOrCancellation() ?: return@awaitEachGesture
            awaitSecondDown(firstUp) ?: return@awaitEachGesture
            state.toggleZoom(action, window, fullscreen)
        }
    }

private suspend fun AwaitPointerEventScope.awaitSecondDown(firstUp: PointerInputChange): PointerInputChange? =
    withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
        val minUptime = firstUp.uptimeMillis + viewConfiguration.doubleTapMinTimeMillis
        var change: PointerInputChange
        do {
            change = awaitFirstDown()
        } while (change.uptimeMillis < minUptime)
        change
    }
