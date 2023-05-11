package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.ColorRgba

class ColorPickerSheetVM(
    selectedColor: ColorRgba,
) : __VM<ColorPickerSheetVM.State>() {

    data class State(
        val r: Float,
        val g: Float,
        val b: Float,
    ) {
        val headerTitle = "Color"
        val doneTitle = "Done"

        val rgbText = "RGB: ${r.toInt()},${g.toInt()},${b.toInt()}"
        val colorGroups = mdColors.chunked(6)

        fun getSelectedColor() = ColorRgba(r.toInt(), g.toInt(), b.toInt())
    }

    override val state = MutableStateFlow(
        State(
            r = selectedColor.r.toFloat(),
            g = selectedColor.g.toFloat(),
            b = selectedColor.b.toFloat(),
        )
    )

    fun upR(r: Float): Unit = state.update { it.copy(r = r) }
    fun upG(g: Float): Unit = state.update { it.copy(g = g) }
    fun upB(b: Float): Unit = state.update { it.copy(b = b) }

    fun upColorRgba(colorRgba: ColorRgba) {
        state.update {
            it.copy(
                r = colorRgba.r.toFloat(),
                g = colorRgba.g.toFloat(),
                b = colorRgba.b.toFloat(),
            )
        }
    }
}

private val mdColors = listOf(
    listOf(ColorRgba(185,246,202),ColorRgba(105,240,174),ColorRgba(0,200,83),),
    listOf(ColorRgba(204,255,144),ColorRgba(178,255,89),ColorRgba(100,221,23),),
    listOf(ColorRgba(255,255,141),ColorRgba(255,255,0),ColorRgba(255,214,0),),
    listOf(ColorRgba(255,209,128),ColorRgba(255,171,64),ColorRgba(255,109,0),),
    listOf(ColorRgba(255,138,128),ColorRgba(255,82,82),ColorRgba(213,0,0),),
    listOf(ColorRgba(188,170,164),ColorRgba(141,110,99),ColorRgba(93,64,55),),
    listOf(ColorRgba(132,255,255),ColorRgba(24,255,255),ColorRgba(0,184,212),),
    listOf(ColorRgba(128,216,255),ColorRgba(64,196,255),ColorRgba(0,145,234),),
    listOf(ColorRgba(140,158,255),ColorRgba(83,109,254),ColorRgba(48,79,254),),
    listOf(ColorRgba(179,136,255),ColorRgba(124,77,255),ColorRgba(98,0,234),),
    listOf(ColorRgba(234,128,252),ColorRgba(224,64,251),ColorRgba(170,0,255),),
    listOf(ColorRgba(176,190,197),ColorRgba(96,125,139),ColorRgba(55,71,79),),
)
