package ru.workinprogress.appframe

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.application

fun main() =
    application {
        MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surfaceVariant = Color.Cyan)) {
            AppFrame(onCloseRequest = ::exitApplication) {
                App(modifier = Modifier.fillMaxSize())
            }
        }
    }
