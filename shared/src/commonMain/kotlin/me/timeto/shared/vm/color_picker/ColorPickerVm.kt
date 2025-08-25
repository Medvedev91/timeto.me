package me.timeto.shared.vm.color_picker

import kotlinx.coroutines.flow.*
import me.timeto.shared.ColorRgba
import me.timeto.shared.Palette
import me.timeto.shared.vm.Vm

class ColorPickerVm(
    examplesUi: ColorPickerExamplesUi,
) : Vm<ColorPickerVm.State>() {

    companion object {

        fun prepCustomColorRgbaText(colorRgba: ColorRgba): String {
            val (r, g, b) = listOf(colorRgba.r, colorRgba.g, colorRgba.b)
            return "RGB: $r,$g,$b / #${r.toHex()}${g.toHex()}${b.toHex()}".uppercase()
        }
    }

    data class State(
        val colorRgba: ColorRgba,
    ) {

        val doneText = "Done"

        val colorGroups: List<List<ColorItem>> = palettes
            // todo dark?
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

private val palettes: List<Palette> = listOf(
    Palette.red,
    Palette.orange,
    Palette.yellow,
    Palette.green,
    Palette.mint,
    Palette.teal,
    Palette.cyan,
    Palette.blue,
    Palette.indigo,
    Palette.purple,
    Palette.pink,
    Palette.brown,
    // Custom gray
    Palette(
        "Gray",
        ColorRgba(142, 142, 147),
        ColorRgba(142, 142, 147),
        ColorRgba(174, 174, 178),
        ColorRgba(72, 72, 74),
    ),
)
