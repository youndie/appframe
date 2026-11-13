package io.github.youndie.appframe

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * [AppFrame]'s layout, drawn without a window: [FrameLayout] with [FrameTitleBar] in it, which is
 * what `AppFrameContent` puts in the window, minus the drag area around the bar (it needs the
 * window). At density 1, so a dp of the styles is a pixel here.
 */
@OptIn(ExperimentalComposeUiApi::class)
class FrameLayoutTest {
    @Test
    fun `a background shows through the bar and below it`() {
        val frame = Frame(TitleBarStyle.MacOs, withBackground = true, showTitle = false)

        val pixels = frame.render()

        // The middle of the bar, clear of the traffic lights; 0.3.0 painted `surfaceVariant` here.
        assertEquals(Stage, pixels[WIDTH / 2, BAR_MIDDLE_MACOS])
        // Below the bar, where the content draws nothing.
        assertEquals(Stage, pixels[WIDTH / 2, HEIGHT - 10])
        // The bar is drawn over the background, not under it: the close button is still there.
        // The first traffic light: 12 dp wide, after the controls' padding.
        val close = 12 + 6
        assertTrue(pixels[close, BAR_MIDDLE_MACOS] != Stage, "the close button is not drawn")
    }

    @Test
    fun `without its title the bar draws nothing between the controls`() {
        val hidden = Frame(TitleBarStyle.MacOs, withBackground = true, showTitle = false).render()
        val shown = Frame(TitleBarStyle.MacOs, withBackground = true, showTitle = true).render()

        // The macOS title is centered in what the traffic lights leave on either side.
        val titleArea = (100 until WIDTH - 100).flatMap { x -> (0 until 28).map { y -> x to y } }

        assertTrue(titleArea.all { (x, y) -> hidden[x, y] == Stage }, "a hidden title still drew")
        // The control: the same frame with the title draws it in that area, so the area is right.
        assertTrue(titleArea.any { (x, y) -> shown[x, y] != Stage }, "the title is not in the area")
    }

    @Test
    fun `without a background the frame draws what the 0_3_0 layout drew, pixel for pixel`() {
        val styles =
            listOf(
                TitleBarStyle.Windows,
                TitleBarStyle.MacOs,
                TitleBarStyle.Linux,
                TitleBarStyle.Linux.withGtkButtonLayout(parseGtkButtonLayout("'appmenu:close'")),
            )

        for (style in styles) {
            for (menus in listOf(false, true)) {
                val frame = Frame(style, withBackground = false, menus = menus)

                val now = frame.render()
                val before = frame.render { frame.Layout030() }

                assertSamePixels(before, now, "${style.controlsAppearance}, menus=$menus")
            }
        }
    }

    @Test
    fun `a double click on the bar over a background still toggles the window`() {
        val frame = Frame(TitleBarStyle.Windows, withBackground = true, showTitle = false)

        frame.scene { scene ->
            scene.click(Offset(WIDTH / 2f, 16f), atMillis = 1_000)
            scene.click(Offset(WIDTH / 2f, 16f), atMillis = 1_100)
        }

        assertEquals(WindowPlacement.Maximized, frame.state.placement)
        assertEquals(0, frame.backgroundPresses, "a press on the bar reached the background")
    }

    @Test
    fun `the window controls over a background still work, and keep their presses`() {
        val frame = Frame(TitleBarStyle.Windows, withBackground = true, showTitle = false)

        frame.scene { scene ->
            // The close button: the last 46 px of a Windows bar.
            scene.click(Offset(WIDTH - 23f, 16f), atMillis = 1_000)
        }

        assertTrue(frame.closed, "the close button did not close")
        assertEquals(0, frame.backgroundPresses, "a press on the close button reached the background")
    }

    @Test
    fun `below the bar a press the content leaves alone reaches the background`() {
        // The control for the two tests above: the probe does count presses that get to it.
        val frame = Frame(TitleBarStyle.Windows, withBackground = true)

        frame.scene { scene -> scene.click(Offset(WIDTH / 2f, HEIGHT - 10f), atMillis = 1_000) }

        assertEquals(1, frame.backgroundPresses)
    }

    @Test
    fun `switching the background on keeps what the content remembers`() {
        var withBackground by mutableStateOf(false)
        val remembered = mutableListOf<Any>()
        val scene =
            ImageComposeScene(WIDTH, HEIGHT, Density(1f)) {
                MaterialTheme {
                    FrameLayout(
                        background = if (withBackground) stageBackground {} else null,
                        titleBar = {},
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        val token = remember { Any() }
                        SideEffect { remembered += token }
                    }
                }
            }
        try {
            scene.render()
            withBackground = true
            Snapshot.sendApplyNotifications()
            scene.render()
        } finally {
            scene.close()
        }

        assertTrue(remembered.size >= 2, "the content did not recompose: $remembered")
        assertEquals(1, remembered.distinct().size, "the content lost its state")
    }
}

private const val WIDTH = 720
private const val HEIGHT = 200

/** Half of `TitleBarStyle.MacOs.height`. */
private const val BAR_MIDDLE_MACOS = 14

/** The background's one colour — none of the default light scheme's. */
private val Stage = Color(0xFF2E7D32)

/** What the content draws in the 0.3.0 comparison: a band, so that where it starts is checked. */
private val ContentBand = Color(0xFF1565C0)

/**
 * One frame and what it was asked to do: [state] for the bar's double click, [closed] for its close
 * button, [backgroundPresses] for every press that reached the background.
 */
private class Frame(
    val style: TitleBarStyle,
    val withBackground: Boolean,
    val showTitle: Boolean = true,
    val menus: Boolean = false,
) {
    val state = WindowState(placement = WindowPlacement.Floating)
    var closed = false
    var backgroundPresses = 0

    private val icon = ColorPainter(Color(0xFFE65100))

    /** What `AppFrameContent` lays out. */
    @Composable
    fun Layout() {
        FrameLayout(
            background = if (withBackground) stageBackground { backgroundPresses++ } else null,
            titleBar = {
                FrameTitleBar(
                    state = state,
                    onCloseRequest = { closed = true },
                    title = "AppFrame",
                    showTitle = showTitle,
                    icon = icon,
                    style = style,
                    overBackground = withBackground,
                    menuBar = if (menus) TestMenus else null,
                    actions = { Action() },
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            Content()
        }
    }

    /** What `AppFrameContent` laid out in 0.3.0, copied from it: a column of the bar and the content. */
    @Composable
    fun Layout030() {
        Column(Modifier.fillMaxSize()) {
            TitleBar(
                state = state,
                onCloseRequest = { closed = true },
                title = "AppFrame",
                icon = icon,
                style = style,
                menuBar = if (menus) TestMenus else null,
                actions = { Action() },
            )
            Content()
        }
    }

    @Composable
    private fun Action() {
        Box(Modifier.size(20.dp).background(Color(0xFF6A1B9A)))
    }

    /** Over a background nothing but the text, so the background shows; without one, a band too. */
    @Composable
    private fun ColumnScope.Content() {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (!withBackground) {
                Box(Modifier.fillMaxWidth().size(40.dp).background(ContentBand))
            }
            Text("Content", Modifier.padding(start = 16.dp, top = 48.dp))
        }
    }

    fun render(content: @Composable () -> Unit = { Layout() }): PixelMap {
        val scene = ImageComposeScene(WIDTH, HEIGHT, Density(1f)) { MaterialTheme { content() } }
        return try {
            scene.render().toComposeImageBitmap().toPixelMap()
        } finally {
            scene.close()
        }
    }

    fun scene(block: (ImageComposeScene) -> Unit) {
        val scene = ImageComposeScene(WIDTH, HEIGHT, Density(1f)) { MaterialTheme { Layout() } }
        try {
            scene.render()
            block(scene)
            scene.render()
        } finally {
            scene.close()
        }
    }
}

/** A background of one colour that counts the presses it receives. */
private fun stageBackground(onPress: () -> Unit): @Composable BoxScope.() -> Unit =
    {
        Box(
            Modifier
                .matchParentSize()
                .background(Stage)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        onPress()
                    }
                },
        )
    }

private val TestMenus: @Composable MenuBarScope.() -> Unit = {
    Menu("File") {
        Item("New", shortcut = MenuShortcut.primary(Key.N, os = HostOs.Windows)) {}
    }
    Menu("Help", enabled = false) { Item("About") {} }
}

/** A press and a release 10 ms later, at [atMillis] on the scene's clock. */
@OptIn(ExperimentalComposeUiApi::class)
private fun ImageComposeScene.click(
    at: Offset,
    atMillis: Long,
) {
    sendPointerEvent(
        PointerEventType.Press,
        at,
        timeMillis = atMillis,
        buttons = PointerButtons(isPrimaryPressed = true),
        button = PointerButton.Primary,
    )
    sendPointerEvent(
        PointerEventType.Release,
        at,
        timeMillis = atMillis + 10,
        buttons = PointerButtons(),
        button = PointerButton.Primary,
    )
}

private fun assertSamePixels(
    expected: PixelMap,
    actual: PixelMap,
    what: String,
) {
    assertEquals(expected.width to expected.height, actual.width to actual.height, what)
    var differing = 0
    var first: Pair<Int, Int>? = null
    for (y in 0 until expected.height) {
        for (x in 0 until expected.width) {
            if (expected[x, y] != actual[x, y]) {
                differing++
                if (first == null) first = x to y
            }
        }
    }
    assertEquals(0, differing, "$what: $differing pixels differ, the first at $first")
}
