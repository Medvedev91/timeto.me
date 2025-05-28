package me.timeto.shared.ui.color_picker

import kotlinx.coroutines.flow.*
import me.timeto.shared.AppleColors
import me.timeto.shared.ColorRgba
import me.timeto.shared.vm.__Vm

class ColorPickerVm(
    examplesUi: ColorPickerExamplesUi,
) : __Vm<ColorPickerVm.State>() {

    companion object {

        fun prepCustomColorRgbaText(colorRgba: ColorRgba):String {
            val (r, g, b) = listOf(colorRgba.r, colorRgba.g, colorRgba.b)
            return "RGB: $r,$g,$b / #${r.toHex()}${g.toHex()}${b.toHex()}".uppercase()
        }
    }

    data class State(
        val colorRgba: ColorRgba,
    ) {

        val saveText = "Done"

        val colorGroups: List<List<ColorItem>> = AppleColors.Palettes.all
            .map { listOf(it.aLight, it.light, it.aDark) }
            .flatten()
            .map {
                ColorItem(
                    colorRgba = it,
                    isSelected = it == colorRgba,
                )
            }
            .chunked(3)
    }

    override val state = MutableStateFlow(
        State(
            colorRgba = examplesUi.mainExampleUi.colorRgba,
        )
    )

    fun setColorRgba(colorRgba: ColorRgba) {
        state.update { it.copy(colorRgba = colorRgba) }
    }

    ///

    class ColorItem(
        val colorRgba: ColorRgba,
        val isSelected: Boolean,
    )
}

///

private fun Int.toHex(): String =
    toString(16).padStart(2, '0')
