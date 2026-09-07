package io.github.youndie.appframe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Demo content: pick a platform and watch the title bar above rearrange itself.
 */
@Composable
fun App(
    platforms: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    lastCommand: String,
    modifier: Modifier = Modifier,
) {
    Surface(modifier, color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "AppFrame",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                "Window controls follow the host platform. Try the other layouts:",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                platforms.forEach { platform ->
                    FilterChip(
                        selected = platform == selected,
                        onClick = { onSelect(platform) },
                        label = { Text(platform) },
                    )
                }
            }
            Text(
                "Double-click the title bar to maximize. Menus live in the title bar too.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
            Text(
                "Last menu command: $lastCommand",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}
