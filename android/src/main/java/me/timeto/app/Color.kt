package me.timeto.app

import androidx.compose.ui.graphics.Color
import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider

fun Color.toGlanceColorProvider(): ColorProvider {
    return ColorProvider(day = this, night = this)
}
