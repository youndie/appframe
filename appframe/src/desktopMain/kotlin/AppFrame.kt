package ru.workinprogress.appframe

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange

/**
 * # AppFrame Component Documentation
 *
 * The `AppFrame` composable creates a customizable application window with theming support, window state management, and gesture-based operations like minimizing, maximizing, and toggling fullscreen.
 *
 * ## Parameters
 *
 * - **onCloseRequest**: A lambda invoked when the close button is pressed or the close gesture is performed.
 * - **title**: The title of the application window. Defaults to `"AppName"`.
 * - **appTheme**: A composable lambda to provide a custom theme for the application content. Defaults to a pass-through lambda.
 * - **state**: The window's state, which includes size, position, and placement. Defaults to a window size of `1024x720`.
 * - **content**: A composable lambda for the main content of the application window.
 *
 * ## Features
 *
 * 1. **Customizable Window State**: The `state` parameter allows the caller to manage the size, position, and placement of the window.
 * 2. **Draggable Area**: The `WindowDraggableArea` enables dragging of the window through the header.
 * 3. **Gestures**:
 *	- Double-tap gestures allow toggling between maximized and floating window placements.
 *	- Buttons for minimizing, toggling fullscreen, and closing the window.
 * 4. **Theming**: A fully customizable theme can be applied around the app's content through the `appTheme` parameter.
 *
 * ## Usage
 *
 * ```kotlin
 * @Composable
 * fun MyApplication() {
 *	 AppFrame(
 *		 onCloseRequest = { exitApplication() },
 *		 title = "MyApp",
 *		 appTheme = { content -> MyAppTheme { content() } },
 *		 state = rememberWindowState(size = DpSize(1280.dp, 800.dp))
 *	 ) {
 *		 Text("Welcome to My Application!")
 *	 }
 * }
 * ```
 *
 * ## Notes
 *
 * - The window must be closed explicitly using the `onCloseRequest` lambda.
 * - The `AppFrame` is compatible with Compose's `Window` API and adheres to the foundation gestures API for custom interaction handling.
 */

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

