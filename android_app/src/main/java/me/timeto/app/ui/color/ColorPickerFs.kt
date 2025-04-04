package me.timeto.app.ui.color

import androidx.compose.runtime.Composable
import me.timeto.app.rememberVm
import me.timeto.shared.ColorRgba
import me.timeto.shared.ui.color.ColorPickerExamplesData
import me.timeto.shared.ui.color.ColorPickerVm

@Composable
fun ColorPickerFs(
    title: String,
    initExamplesData: ColorPickerExamplesData,
    onPick: (ColorRgba) -> Unit,
) {

    val (vm, state) = rememberVm {
        ColorPickerVm(
            examplesData = initExamplesData,
        )
    }
}
