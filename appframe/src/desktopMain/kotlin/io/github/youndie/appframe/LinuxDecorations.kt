package io.github.youndie.appframe

import java.util.concurrent.TimeUnit

/**
 * Which controls a desktop wants on each side of the title bar.
 *
 * Unlike Windows and macOS, Linux has no single convention: GNOME, KDE, XFCE and Cinnamon put the
 * controls on the right (GNOME shows only Close by default), while elementary OS and the old Unity
 * layout put them on the left. The desktop publishes its choice as a GTK "button layout", which is
 * what [detectGtkButtonLayout] reads.
 */
internal data class DecorationLayout(
    val leading: List<WindowControl>,
    val trailing: List<WindowControl>,
)

/**
 * The current desktop's layout, or `null` if it could not be determined (non-Linux, no `gsettings`,
 * unparseable value). Queried at most once.
 */
internal val gtkButtonLayout: DecorationLayout? by lazy {
    if (HostOs.current == HostOs.Linux) detectGtkButtonLayout() else null
}

private fun detectGtkButtonLayout(): DecorationLayout? =
    runCatching {
        val process =
            ProcessBuilder("gsettings", "get", "org.gnome.desktop.wm.preferences", "button-layout")
                .redirectErrorStream(true)
                .start()

        if (!process.waitFor(1, TimeUnit.SECONDS)) {
            process.destroy()
            return@runCatching null
        }
        if (process.exitValue() != 0) return@runCatching null

        parseGtkButtonLayout(process.inputStream.bufferedReader().readText())
    }.getOrNull()

/**
 * Parses a GTK button layout such as `'appmenu:minimize,maximize,close'` — sides separated by `:`,
 * controls by `,`. Anything that is not a window control (`appmenu`, `menu`, `icon`, `spacer`) is
 * dropped; `gsettings` wraps its output in quotes.
 */
internal fun parseGtkButtonLayout(raw: String): DecorationLayout? {
    val value = raw.trim().trim('\'', '"')
    if (':' !in value) return null

    val (leading, trailing) = value.split(':', limit = 2)
    return DecorationLayout(leading.toControls(), trailing.toControls())
}

private fun String.toControls(): List<WindowControl> =
    split(',').mapNotNull { token ->
        when (token.trim().lowercase()) {
            "minimize" -> WindowControl.Minimize
            "maximize" -> WindowControl.Maximize
            "close" -> WindowControl.Close
            else -> null
        }
    }
