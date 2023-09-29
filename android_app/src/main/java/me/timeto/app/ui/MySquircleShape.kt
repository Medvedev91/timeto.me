package me.timeto.app.ui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

// Based on https://github.com/mizoguche/ui-bezier-curved-image-compose/blob/master/app/src/main/java/dev/mizoguche/beziercurvedimageview/MainActivity.kt
class MySquircleShape(
    private val len: Float = 60f, // Длинна закругления
    private val angleParam: Float = -2f, // Острота загругления,
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
