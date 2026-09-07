package io.github.youndie.appframe

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The line/square/cross drawn inside a window control.
 *
 * Drawn by hand rather than pulled from `material-icons-extended`: that icon pack weighs tens of
 * megabytes, while native title bar glyphs are plain geometry.
 */
internal enum class Glyph {
    Minimize,
    Maximize,
    Restore,
    Close,

    /** The arrows macOS shows on the green button. */
    Expand,

    /** Leave fullscreen. */
    Collapse,
}

@Composable
internal fun WindowControlGlyph(
    glyph: Glyph,
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 1.dp,
) {
    Canvas(modifier.size(size)) {
        val width = strokeWidth.toPx()
        val box = Rect(Offset.Zero, this.size).deflate(width / 2f)

        when (glyph) {
            Glyph.Minimize -> {
                line(box.centerLeft, box.centerRight, color, width)
            }

            Glyph.Maximize -> {
                drawRect(color, box.topLeft, box.size, style = Stroke(width, cap = StrokeCap.Square))
            }

            Glyph.Restore -> {
                drawRestore(box, color, width)
            }

            Glyph.Close -> {
                line(box.topLeft, box.bottomRight, color, width)
                line(box.topRight, box.bottomLeft, color, width)
            }

            Glyph.Expand -> {
                drawArrows(box, color, width, outward = true)
            }

            Glyph.Collapse -> {
                drawArrows(box, color, width, outward = false)
            }
        }
    }
}

private fun DrawScope.line(
    start: Offset,
    end: Offset,
    color: Color,
    width: Float,
) {
    drawLine(color, start, end, strokeWidth = width, cap = StrokeCap.Square)
}

/** A window overlapping the corner of the one behind it. */
private fun DrawScope.drawRestore(
    box: Rect,
    color: Color,
    width: Float,
) {
    val inset = box.width * 0.25f
    drawRect(
        color = color,
        topLeft = Offset(box.left, box.top + inset),
        size = Size(box.width - inset, box.height - inset),
        style = Stroke(width, cap = StrokeCap.Square),
    )
    line(Offset(box.left + inset, box.top + inset), Offset(box.left + inset, box.top), color, width)
    line(Offset(box.left + inset, box.top), Offset(box.right, box.top), color, width)
    line(Offset(box.right, box.top), Offset(box.right, box.bottom - inset), color, width)
}

/** Two triangles pointing out of (or into) opposite corners. */
private fun DrawScope.drawArrows(
    box: Rect,
    color: Color,
    width: Float,
    outward: Boolean,
) {
    val side = box.width * 0.55f
    if (outward) {
        line(box.topLeft, Offset(box.left + side, box.top), color, width)
        line(box.topLeft, Offset(box.left, box.top + side), color, width)
        line(box.bottomRight, Offset(box.right - side, box.bottom), color, width)
        line(box.bottomRight, Offset(box.right, box.bottom - side), color, width)
    } else {
        line(box.topRight, Offset(box.right - side, box.top), color, width)
        line(box.topRight, Offset(box.right, box.top + side), color, width)
        line(box.bottomLeft, Offset(box.left + side, box.bottom), color, width)
        line(box.bottomLeft, Offset(box.left, box.bottom - side), color, width)
    }
}
