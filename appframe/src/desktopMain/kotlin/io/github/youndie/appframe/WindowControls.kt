package io.github.youndie.appframe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.WindowState

/**
 * The minimize / maximize / close cluster, laid out in [TitleBarStyle.controlsOrder].
 */
@Composable
internal fun WindowControls(
    state: WindowState,
    style: TitleBarStyle,
    fullscreen: FullscreenFallback,
    onCloseRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val groupInteraction = remember { MutableInteractionSource() }
    val groupHovered by groupInteraction.collectIsHoveredAsState()

    Row(
        modifier = modifier.fillMaxHeight().hoverable(groupInteraction),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(style.buttonSpacing),
    ) {
        style.controlsOrder.forEach { control ->
            WindowControlButton(
                control = control,
                state = state,
                style = style,
                fullscreen = fullscreen,
                groupHovered = groupHovered,
                onCloseRequest = onCloseRequest,
            )
        }
    }
}

@Composable
private fun WindowControlButton(
    control: WindowControl,
    state: WindowState,
    style: TitleBarStyle,
    fullscreen: FullscreenFallback,
    groupHovered: Boolean,
    onCloseRequest: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val window = LocalAppFrameWindow.current
    val isFullscreen = state.isFullscreenWith(fullscreen)
    val enabled = control != WindowControl.Minimize || !isFullscreen

    val onClick: () -> Unit =
        when (control) {
            WindowControl.Minimize -> ({ state.isMinimized = true })
            WindowControl.Maximize -> ({ state.toggleZoom(style.maximizeAction, window, fullscreen) })
            WindowControl.Close -> onCloseRequest
        }

    val glyph =
        when (control) {
            WindowControl.Minimize -> {
                Glyph.Minimize
            }

            WindowControl.Close -> {
                Glyph.Close
            }

            WindowControl.Maximize -> {
                when {
                    style.maximizeAction == MaximizeAction.Fullscreen && isFullscreen -> Glyph.Collapse
                    style.maximizeAction == MaximizeAction.Fullscreen -> Glyph.Expand
                    state.isMaximized -> Glyph.Restore
                    else -> Glyph.Maximize
                }
            }
        }

    val content = @Composable { color: Color, visible: Boolean ->
        if (visible) {
            WindowControlGlyph(glyph = glyph, color = color, size = style.glyphSize)
        }
    }

    val clickable =
        Modifier
            .size(style.buttonSize)
            .hoverable(interaction, enabled = enabled)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )

    when (style.controlsAppearance) {
        WindowControlsAppearance.Bars -> {
            Box(
                modifier = clickable.background(barBackground(control, hovered, enabled)),
                contentAlignment = Alignment.Center,
            ) {
                content(barContentColor(control, hovered, enabled), true)
            }
        }

        WindowControlsAppearance.TrafficLights -> {
            val focused = LocalWindowInfo.current.isWindowFocused
            Box(
                modifier =
                    clickable.background(
                        color = trafficLightColor(control, enabled, focused),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // macOS reveals every glyph as soon as the cluster is hovered, not just one.
                content(TrafficLightGlyph, groupHovered && enabled)
            }
        }
    }
}

@Composable
private fun barBackground(
    control: WindowControl,
    hovered: Boolean,
    enabled: Boolean,
): Color =
    when {
        !hovered || !enabled -> Color.Transparent
        control == WindowControl.Close -> CloseRed
        else -> LocalContentColor.current.copy(alpha = 0.12f)
    }

@Composable
private fun barContentColor(
    control: WindowControl,
    hovered: Boolean,
    enabled: Boolean,
): Color =
    when {
        !enabled -> LocalContentColor.current.copy(alpha = 0.38f)
        hovered && control == WindowControl.Close -> Color.White
        else -> LocalContentColor.current
    }

@Composable
private fun trafficLightColor(
    control: WindowControl,
    enabled: Boolean,
    focused: Boolean,
): Color =
    if (!focused || !enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
    } else {
        when (control) {
            WindowControl.Close -> TrafficLightRed
            WindowControl.Minimize -> TrafficLightYellow
            WindowControl.Maximize -> TrafficLightGreen
        }
    }

/** Windows 11's close-button hover fill. */
private val CloseRed = Color(0xFFC42B1C)
private val TrafficLightRed = Color(0xFFFF5F57)
private val TrafficLightYellow = Color(0xFFFEBC2E)
private val TrafficLightGreen = Color(0xFF28C840)
private val TrafficLightGlyph = Color(0x99000000)
