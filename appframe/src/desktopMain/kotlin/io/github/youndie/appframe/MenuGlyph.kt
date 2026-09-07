package io.github.youndie.appframe

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The tick, dot and arrow a menu item needs.
 *
 * Drawn rather than imported for the same reason [Glyph] is: pulling in `material-icons-extended`
 * for three shapes would cost the library's users tens of megabytes.
 */
internal enum class MenuGlyph {
    /** Marks a checked [MenuScope.CheckboxItem]. */
    Check,

    /** Marks the selected [MenuScope.RadioButtonItem]. */
    Dot,

    /** Points at the submenu an item opens. */
    Arrow,
}

@Composable
internal fun MenuItemGlyph(
    glyph: MenuGlyph,
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 1.5.dp,
) {
    Canvas(modifier.size(size)) {
        val width = strokeWidth.toPx()
        val box = Rect(Offset.Zero, this.size).deflate(width / 2f)

        when (glyph) {
            MenuGlyph.Check -> {
                val corner = Offset(box.left + box.width * 0.4f, box.bottom - box.height * 0.1f)
                drawLine(
                    color,
                    Offset(box.left, box.center.y),
                    corner,
                    strokeWidth = width,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color,
                    corner,
                    Offset(box.right, box.top + box.height * 0.15f),
                    strokeWidth = width,
                    cap = StrokeCap.Round,
                )
            }

            MenuGlyph.Dot -> {
                drawCircle(color, radius = box.width * 0.22f, center = box.center)
            }

            MenuGlyph.Arrow -> {
                val tip = Offset(box.right - box.width * 0.25f, box.center.y)
                val back = box.left + box.width * 0.25f
                drawLine(
                    color,
                    Offset(back, box.top + box.height * 0.15f),
                    tip,
                    strokeWidth = width,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color,
                    tip,
                    Offset(back, box.bottom - box.height * 0.15f),
                    strokeWidth = width,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
