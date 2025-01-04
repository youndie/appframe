package ru.workinprogress.appframe

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.awt.Toolkit

/**
 * Creates an application window with customizable theming, window state, and content. Supports operations
 * like minimizing, maximizing, fullscreen toggling, and close handling through gestures or buttons.
 *
 * @param onCloseRequest A lambda that is invoked when the close button is pressed or the close gesture is performed.
 * @param title The title of the application window. Defaults to "AppName".
 * @param appTheme A composable lambda to provide a custom theme for the application content.
 * @param state The window's state, including size, position, and placement. Defaults to a window size of 1024x720.
 * @param content The main content of the application window, provided as a composable lambda.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppFrame(
	onCloseRequest: () -> Unit,
	title: String = "AppName",
	appTheme: @Composable (@Composable () -> Unit) -> Unit = { it() },
	state: WindowState = rememberWindowState(size = DpSize(1024.dp, 720.dp)),
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
		WindowDraggableArea {
			appTheme {
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
								Icon(Icons.Sharp.Close, modifier = Modifier.size(24.dp), contentDescription = "close")
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
