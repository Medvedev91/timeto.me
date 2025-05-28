package me.timeto.app.misc.extensions

import androidx.compose.ui.graphics.Color
import me.timeto.app.ui.c
import me.timeto.shared.misc.ColorEnum

fun ColorEnum.toColor(): Color = when (this) {

    ColorEnum.white -> c.white
    ColorEnum.black -> c.black

    ColorEnum.red -> c.red
    ColorEnum.orange -> c.orange
    ColorEnum.yellow -> c.yellow
    ColorEnum.green -> c.green
    ColorEnum.mint -> c.mint
    ColorEnum.teal -> c.teal
    ColorEnum.cyan -> c.cyan
    ColorEnum.blue -> c.blue
    ColorEnum.indigo -> c.indigo
    ColorEnum.purple -> c.purple
    ColorEnum.pink -> c.pink
    ColorEnum.brown -> c.brown

    ColorEnum.gray -> c.gray
}
