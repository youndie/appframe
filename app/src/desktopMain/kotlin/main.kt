package ru.workinprogress.appframe

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

            AppFrame(
                onCloseRequest = ::exitApplication,
                title = "AppFrame",
                style = styleOf(platform),
            ) {
                App(
                    platforms = Platforms,
                    selected = platform,
                    onSelect = { platform = it },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
