package me.timeto.shared.ui.color

import kotlinx.coroutines.flow.*
import me.timeto.shared.AppleColors
import me.timeto.shared.ColorRgba
import me.timeto.shared.vm.__Vm

class ColorPickerVm(
    examplesData: ColorPickerExamplesData,
) : __Vm<ColorPickerVm.State>() {

    data class State(
        val colorRgba: ColorRgba,
        val examplesData: ColorPickerExamplesData,
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
            colorRgba = examplesData.mainExample.colorRgba,
            examplesData = examplesData,
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
