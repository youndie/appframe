package io.github.youndie.appframe

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.window.application

private val Platforms = listOf("This platform", "Windows", "macOS", "Linux")

private fun styleOf(platform: String): TitleBarStyle =
    when (platform) {
        "Windows" -> TitleBarStyle.Windows
        "macOS" -> TitleBarStyle.MacOs
        "Linux" -> TitleBarStyle.Linux
        else -> TitleBarStyle.forHost()
    }

fun main() =
    application {
        val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()

        MaterialTheme(colorScheme = colorScheme) {
            var platform by remember { mutableStateOf(Platforms.first()) }
            var fullscreen by remember { mutableStateOf(false) }
            var lastCommand by remember { mutableStateOf("—") }

            val style =
                styleOf(platform).copy(
                    maximizeAction = if (fullscreen) MaximizeAction.Fullscreen else MaximizeAction.Maximize,
                )

            AppFrame(
                onCloseRequest = ::exitApplication,
                title = "AppFrame",
                style = style,
                menuBar = {
                    Menu("File") {
                        Item("New", shortcut = MenuShortcut.primary(Key.N)) { lastCommand = "File ▸ New" }
                        Item("Open…", shortcut = MenuShortcut.primary(Key.O)) { lastCommand = "File ▸ Open" }
                        Menu("Open Recent") {
                            Item("AppFrame.kt") { lastCommand = "Recent ▸ AppFrame.kt" }
                            Item("TitleBar.kt") { lastCommand = "Recent ▸ TitleBar.kt" }
                        }
                        Separator()
                        Item("Save", shortcut = MenuShortcut.primary(Key.S), enabled = false) { }
                        Separator()
                        Item("Quit", shortcut = MenuShortcut.primary(Key.Q), onClick = ::exitApplication)
                    }
                    Menu("View") {
                        CheckboxItem(
                            text = "Fullscreen zoom button",
                            checked = fullscreen,
                            shortcut = MenuShortcut.primary(Key.F, shift = true),
                            onCheckedChange = { fullscreen = it },
                        )
                        Separator()
                        Platforms.forEach { candidate ->
                            RadioButtonItem(
                                text = candidate,
                                selected = candidate == platform,
                                onClick = { platform = candidate },
                            )
                        }
                    }
                },
            ) {
                App(
                    platforms = Platforms,
                    selected = platform,
                    onSelect = { platform = it },
                    lastCommand = lastCommand,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
