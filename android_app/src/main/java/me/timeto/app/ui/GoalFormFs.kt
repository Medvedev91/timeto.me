package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.*
import me.timeto.shared.vm.ActivityFormSheetVm
import me.timeto.shared.vm.GoalFormVm

@Composable
fun GoalFormFs(
    _layer: WrapperView.Layer,
    _initGoalFormUi: ActivityFormSheetVm.GoalFormUi?,
    _onSelect: (ActivityFormSheetVm.GoalFormUi) -> Unit,
) {

    val (vm, state) = rememberVm(_initGoalFormUi) {
        GoalFormVm(_initGoalFormUi)
    }

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.bg),
    ) {

        val scrollState = rememberScrollState()

        Fs__HeaderAction(
            title = state.headerTitle,
            actionText = state.headerDoneText,
            onCancel = { _layer.close() },
            scrollState = scrollState,
            onDone = {
                vm.buildFormUi { formUi ->
                    _onSelect(formUi)
                    _layer.close()
                }
            },
        )

        SpacerW1()
    }
}
