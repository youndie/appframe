package io.github.youndie.appframe

import androidx.compose.ui.Alignment
import kotlin.test.Test
import kotlin.test.assertEquals

class HostOsTest {
    @Test
    fun `recognizes os names reported by the jvm`() {
        assertEquals(HostOs.Windows, hostOsOf("Windows 11"))
        assertEquals(HostOs.Windows, hostOsOf("Windows Server 2022"))
        assertEquals(HostOs.MacOs, hostOsOf("Mac OS X"))
        assertEquals(HostOs.MacOs, hostOsOf("Darwin"))
        assertEquals(HostOs.Linux, hostOsOf("Linux"))
        assertEquals(HostOs.Linux, hostOsOf("FreeBSD"))
        assertEquals(HostOs.Unknown, hostOsOf(null))
        assertEquals(HostOs.Unknown, hostOsOf("Plan 9"))
    }

    @Test
    fun `windows and linux put the controls on the right, macos on the left`() {
        assertEquals(Alignment.End, TitleBarStyle.forHost(HostOs.Windows).controlsAlignment)
        assertEquals(Alignment.End, TitleBarStyle.forHost(HostOs.Linux).controlsAlignment)
        assertEquals(Alignment.End, TitleBarStyle.forHost(HostOs.Unknown).controlsAlignment)
        assertEquals(Alignment.Start, TitleBarStyle.forHost(HostOs.MacOs).controlsAlignment)
    }

    @Test
    fun `close button comes last on windows and first on macos`() {
        assertEquals(WindowControl.Close, TitleBarStyle.forHost(HostOs.Windows).controlsOrder.last())
        assertEquals(WindowControl.Close, TitleBarStyle.forHost(HostOs.MacOs).controlsOrder.first())
    }

    @Test
    fun `parses gtk button layouts`() {
        // GNOME's default: everything but Close is hidden.
        assertEquals(
            DecorationLayout(leading = emptyList(), trailing = listOf(WindowControl.Close)),
            parseGtkButtonLayout("'appmenu:close'\n"),
        )
        assertEquals(
            DecorationLayout(
                leading = emptyList(),
                trailing = listOf(WindowControl.Minimize, WindowControl.Maximize, WindowControl.Close),
            ),
            parseGtkButtonLayout("'appmenu:minimize,maximize,close'"),
        )
        // elementary OS keeps Close on the left.
        assertEquals(
            DecorationLayout(leading = listOf(WindowControl.Close), trailing = listOf(WindowControl.Maximize)),
            parseGtkButtonLayout("'close:maximize'"),
        )
        assertEquals(null, parseGtkButtonLayout("nonsense"))
    }

    @Test
    fun `gtk layout decides which side the linux controls sit on`() {
        val base = TitleBarStyle.Linux

        val unity =
            base.withGtkButtonLayout(
                parseGtkButtonLayout("'close,minimize,maximize:'"),
            )
        assertEquals(Alignment.Start, unity.controlsAlignment)
        assertEquals(WindowControl.Close, unity.controlsOrder.first())

        val gnome = base.withGtkButtonLayout(parseGtkButtonLayout("'appmenu:close'"))
        assertEquals(Alignment.End, gnome.controlsAlignment)
        assertEquals(listOf(WindowControl.Close), gnome.controlsOrder)

        // Nothing detected, or nothing usable in it: keep the default.
        assertEquals(base, base.withGtkButtonLayout(null))
        assertEquals(base, base.withGtkButtonLayout(parseGtkButtonLayout("'appmenu:'")))
    }
}
