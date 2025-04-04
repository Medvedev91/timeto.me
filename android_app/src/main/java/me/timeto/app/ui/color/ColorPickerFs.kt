package me.timeto.app.ui.color

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.ColorRgba
import me.timeto.shared.ui.color.ColorPickerExamplesData
import me.timeto.shared.ui.color.ColorPickerVm

@Composable
fun ColorPickerFs(
    title: String,
    initExamplesData: ColorPickerExamplesData,
    onPick: (ColorRgba) -> Unit,
) {

    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ColorPickerVm(
            examplesData = initExamplesData,
        )
    }

    Screen {

        val scrollState = rememberScrollState()

        Header(
            title = title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = state.saveText,
                isEnabled = true,
                onClick = {
                    onPick(state.colorRgba)
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            )
        )
    }
}
