package me.timeto.app.ui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * cubicTo() adds a cubic bezier segment that curves from the current point
 * to the given point (x3, y3), using the control points (x1, y1) and (x2, y2).
 */
class SquircleShape(
    private val len: Float = 60f,
    private val angleParam: Float = -2f,
    private val angles: List<Boolean> = listOf(true, true, true, true), // top left, top right, ...
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height

        val path = Path()
        path.moveTo(0f, len)
        if (angles[0])
            path.cubicTo(
                x1 = 0f,
                y1 = angleParam,
                x2 = angleParam,
                y2 = 0f,
                x3 = len,
                y3 = 0f,
            )
        else
            path.lineTo(0f, 0f)
        path.lineTo(w - len, 0f)
        if (angles[1])
            path.cubicTo(
                x1 = w - angleParam,
                y1 = 0f,
                x2 = w,
                y2 = angleParam,
                x3 = w,
                y3 = len,
            )
        else
            path.lineTo(w, 0f)
        path.lineTo(w, h - len)
        if (angles[2])
            path.cubicTo(
                x1 = w,
                y1 = h - angleParam,
                x2 = w - angleParam,
                y2 = h,
                x3 = w - len,
                y3 = h,
            )
        else
            path.lineTo(w, h)
        path.lineTo(len, h)
        if (angles[3])
            path.cubicTo(
                x1 = angleParam,
                y1 = h,
                x2 = 0f,
                y2 = h - angleParam,
                x3 = 0f,
                y3 = h - len,
            )
        else
            path.lineTo(0f, h)
        path.close()
        return Outline.Generic(path)
    }
}

class SquircleShapeDp(
    private val lenDp: Dp,
    private val attackRatio: Float = 6f,
    // top left, top right, bottom right, bottom left
    private val angles: List<Boolean> = listOf(true, true, true, true),
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val totalLen: Float = density.density * lenDp.value
        val attackLen: Float = totalLen / attackRatio
        return buildOutline(
            size = size,
            totalLen = totalLen,
            attackLen = attackLen,
            angles = angles,
        )
    }
}

///

/**
 * cubicTo() adds a cubic bezier segment that curves from the current point
 * to the given point (x3, y3), using the control points (x1, y1) and (x2, y2).
 */
private fun buildOutline(
    size: Size,
    totalLen: Float,
    attackLen: Float,
    angles: List<Boolean> = listOf(true, true, true, true), // top left, top right, ...
): Outline {

    val w: Float = size.width
    val h: Float = size.height

    val path = Path()
    path.moveTo(0f, totalLen)
    if (angles[0])
        path.cubicTo(
            x1 = 0f,
            y1 = attackLen,
            x2 = attackLen,
            y2 = 0f,
            x3 = totalLen,
            y3 = 0f,
        )
    else
        path.lineTo(0f, 0f)
    path.lineTo(w - totalLen, 0f)
    if (angles[1])
        path.cubicTo(
            x1 = w - attackLen,
            y1 = 0f,
            x2 = w,
            y2 = attackLen,
            x3 = w,
            y3 = totalLen,
        )
    else
        path.lineTo(w, 0f)
    path.lineTo(w, h - totalLen)
    if (angles[2])
        path.cubicTo(
            x1 = w,
            y1 = h - attackLen,
            x2 = w - attackLen,
            y2 = h,
            x3 = w - totalLen,
            y3 = h,
        )
    else
        path.lineTo(w, h)
    path.lineTo(totalLen, h)
    if (angles[3])
        path.cubicTo(
            x1 = attackLen,
            y1 = h,
            x2 = 0f,
            y2 = h - attackLen,
            x3 = 0f,
            y3 = h - totalLen,
        )
    else
        path.lineTo(0f, h)
    path.close()

    return Outline.Generic(path)
}
