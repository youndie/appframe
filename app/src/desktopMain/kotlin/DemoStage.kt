package io.github.youndie.appframe

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

/**
 * A patterned window background for View ▸ Background under the title bar: `AppFrame(background =
 * …)` draws it under the whole frame, and the stripes running on under the bar are what show that
 * the bar draws no strip of its own. Try the controls, a double click and a drag on the bar, and a
 * resize from an edge, over it.
 */
@Composable
fun DemoStage(modifier: Modifier = Modifier) {
    val base = MaterialTheme.colorScheme.tertiaryContainer
    val stripe = MaterialTheme.colorScheme.primaryContainer
    Canvas(modifier) {
        drawRect(base)
        val step = 32.dp.toPx()
        var x = -size.height
        while (x < size.width) {
            drawLine(stripe, Offset(x, size.height), Offset(x + size.height, 0f), strokeWidth = step / 2)
            x += step
        }
    }
}
