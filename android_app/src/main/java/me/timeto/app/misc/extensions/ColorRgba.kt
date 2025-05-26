package me.timeto.app.misc.extensions

import androidx.compose.ui.graphics.Color
import me.timeto.shared.ColorRgba

fun ColorRgba.toColor() =
    Color(r, g, b, a)
