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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
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
    appThemeApplier: @Composable (@Composable () -> Unit) -> Unit = { it() },
    content: @Composable () -> Unit,
) {
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val fullScreenSize = DpSize(screenSize.width.dp, screenSize.height.dp)

    var savedSize by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
    var savedPosition: WindowPosition by remember { mutableStateOf(WindowPosition(0.dp, 0.dp)) }
    val coroutineScope = rememberCoroutineScope()

    Window(
        state = state,
        onCloseRequest = onCloseRequest,
        resizable = true,
        undecorated = true,
        title = title,
    ) {
        Column {
            WindowDraggableArea {
                appThemeApplier {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        // Handle custom double-tap gestures since WindowDraggableArea captures pointer events for dragging
                        modifier = Modifier.pointerInput(Unit) {
                            awaitEachGesture {
                                val firstDown = awaitFirstDown()
                                val firstUp = waitForUpOrCancellation() ?: return@awaitEachGesture
                                val secondDown = awaitSecondDown(firstUp) ?: return@awaitEachGesture

                                if (state.placement == WindowPlacement.Maximized) {
                                    state.placement = WindowPlacement.Floating
                                    coroutineScope.launch {
                                        delay(10)
                                        state.size = savedSize
                                        state.position = savedPosition
                                    }
                                } else {
                                    savedSize = state.size
                                    savedPosition = state.position
                                    coroutineScope.launch {
                                        delay(10)
                                        state.placement = WindowPlacement.Maximized
                                    }
                                }
                            }
                        }
                    ) {
                        Box(modifier = Modifier.height(32.dp)) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                IconButton({
                                    onCloseRequest()
                                }) {
                                    Icon(
                                        Icons.Sharp.Close,
                                        modifier = Modifier.size(24.dp),
                                        contentDescription = "close"
                                    )
                                }

                                if (state.placement == WindowPlacement.Fullscreen) {
                                    IconButton({
                                        state.placement = WindowPlacement.Floating
                                        state.position = savedPosition

                                        coroutineScope.launch {
                                            delay(10)
                                            state.size = savedSize
                                        }
                                    }) {
                                        Icon(
                                            Icons.Sharp.FullscreenExit,
                                            modifier = Modifier.size(24.dp),
                                            contentDescription = "fullscreen"
                                        )
                                    }
                                } else {
                                    IconButton({
                                        state.isMinimized = true
                                    }) {
                                        Icon(
                                            Icons.Sharp.Minimize,
                                            modifier = Modifier.size(24.dp),
                                            contentDescription = "minimize"
                                        )
                                    }

                                    IconButton({
                                        savedSize = state.size
                                        savedPosition = state.position

                                        state.size = fullScreenSize

                                        coroutineScope.launch {
                                            delay(10)
                                            state.placement = WindowPlacement.Fullscreen
                                        }
                                    }) {
                                        Icon(
                                            Icons.Sharp.FullscreenExit,
                                            modifier = Modifier.size(24.dp),
                                            contentDescription = "fullscreen"
                                        )
                                    }
                                }
                            }
                            Text(
                                title,
                                modifier = Modifier.fillMaxWidth().align(androidx.compose.ui.Alignment.Center),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
            content()
        }
    }
}

//Copy from CMP
private suspend fun AwaitPointerEventScope.awaitSecondDown(
    firstUp: PointerInputChange,
): PointerInputChange? = withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
    val minUptime = firstUp.uptimeMillis + viewConfiguration.doubleTapMinTimeMillis
    var change: PointerInputChange
    do {
        change = awaitFirstDown()
    } while (change.uptimeMillis < minUptime)
    change
}
