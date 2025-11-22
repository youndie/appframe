package ru.workinprogress.appframe

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.FullscreenExit
import androidx.compose.material.icons.sharp.Minimize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Toolkit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppFrame(
    onCloseRequest: () -> Unit,
    state: WindowState = rememberWindowState(size = DpSize(1024.dp, 720.dp)),
    title: String = "AppName",
    content: @Composable () -> Unit,
) {
    val screen = Toolkit.getDefaultToolkit().screenSize
    val fullScreenSize = DpSize(screen.width.dp, screen.height.dp)

    var savedSize by remember { mutableStateOf(state.size) }
    var savedPos by remember { mutableStateOf(state.position) }

    Window(
        state = state,
        onCloseRequest = onCloseRequest,
        resizable = true,
        undecorated = true,
        title = title,
    ) {
        Column {
            WindowDraggableArea {
                TopBar(
                    title = title,
                    state = state,
                    savedSize = savedSize,
                    savedPos = savedPos,
                    fullScreenSize = fullScreenSize,
                    onSave = { s, p ->
                        savedSize = s
                        savedPos = p
                    },
                    onClose = onCloseRequest,
                )
            }
            content()
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    state: WindowState,
    savedSize: DpSize,
    savedPos: WindowPosition,
    fullScreenSize: DpSize,
    onSave: (DpSize, WindowPosition) -> Unit,
    onClose: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.doubleClickToToggleMaximize(state, onSave),
    ) {
        Box(Modifier.height(32.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Sharp.Close, null, Modifier.size(24.dp))
                }

                if (state.placement != WindowPlacement.Fullscreen) {
                    IconButton({ state.isMinimized = true }) {
                        Icon(Icons.Sharp.Minimize, null, Modifier.size(24.dp))
                    }
                }

                IconButton({
                    if (state.placement == WindowPlacement.Fullscreen) {
                        state.placement = WindowPlacement.Floating
                        scope.launch {
                            delay(10)
                            state.size = savedSize
                            state.position = savedPos
                        }
                    } else {
                        onSave(state.size, state.position)
                        state.size = fullScreenSize
                        scope.launch {
                            delay(10)
                            state.placement = WindowPlacement.Fullscreen
                        }
                    }
                }) {
                    Icon(Icons.Sharp.FullscreenExit, null, Modifier.size(24.dp))
                }
            }

            Text(
                title,
                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun Modifier.doubleClickToToggleMaximize(
    state: WindowState,
    onSave: (DpSize, WindowPosition) -> Unit,
): Modifier =
    pointerInput(Unit) {
        awaitEachGesture {
            val down1 = awaitFirstDown()
            val up1 = waitForUpOrCancellation() ?: return@awaitEachGesture
            val down2 = awaitSecondDown(up1) ?: return@awaitEachGesture

            if (state.placement == WindowPlacement.Maximized) {
                state.placement = WindowPlacement.Floating
            } else {
                onSave(state.size, state.position)
                state.placement = WindowPlacement.Maximized
            }
        }
    }

private suspend fun AwaitPointerEventScope.awaitSecondDown(firstUp: PointerInputChange): PointerInputChange? =
    withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
        val minUp = firstUp.uptimeMillis + viewConfiguration.doubleTapMinTimeMillis
        var cand: PointerInputChange
        do {
            cand = awaitFirstDown()
        } while (cand.uptimeMillis < minUp)
        cand
    }
