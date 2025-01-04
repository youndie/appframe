package ru.workinprogress.appframe

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application

@OptIn(ExperimentalFoundationApi::class)
fun main() = application {
	AppFrame(::exitApplication, appThemeApplier = {
		MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surfaceVariant = Color.Cyan)) {
			it()
		}
	}) {
		App(modifier = Modifier.fillMaxSize().padding(top = 32.dp))
	}
}