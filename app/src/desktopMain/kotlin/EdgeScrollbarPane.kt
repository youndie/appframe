package io.github.youndie.appframe

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A long column with a stock `VerticalScrollbar` flush against the window's right edge — the layout
 * that loses its thumb to the resize strip (`youndie/appframe#62`). Switch View ▸ Resize border
 * between 8, 4 and 0 dp and try to drag the thumb, then to resize the window from that edge.
 */
@Composable
fun EdgeScrollbarPane(
    resizeBorder: Dp,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Surface(modifier, color = MaterialTheme.colorScheme.surface) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(24.dp)) {
                Text(
                    "Resize border: ${resizeBorder.value.toInt()} dp. Drag the scrollbar at the right edge.",
                    style = MaterialTheme.typography.titleMedium,
                )
                repeat(ROWS) { row ->
                    Text("Row ${row + 1}", style = MaterialTheme.typography.bodyLarge)
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scroll),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            )
        }
    }
}

private const val ROWS = 120
