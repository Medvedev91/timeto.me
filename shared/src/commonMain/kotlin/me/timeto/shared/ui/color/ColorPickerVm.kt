package me.timeto.shared.ui.color

import kotlinx.coroutines.flow.*
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
}
