package ru.workinprogress.appframe

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App(modifier: Modifier = Modifier) {
	Column(modifier) {
		Text(
			"Hello World",
			modifier = Modifier.padding(16.dp).fillMaxWidth(),
			textAlign = androidx.compose.ui.text.style.TextAlign.Center
		)
	}
}