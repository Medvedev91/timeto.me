package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.VStack
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.shared.vm.ActivityFormSheetVm
import me.timeto.shared.vm.GoalsFormVm

@Composable
fun GoalsFormFs(
    _layer: WrapperView.Layer,
    _initGoalFormsVmUi: List<ActivityFormSheetVm.GoalFormUi>,
    _onSelected: (List<ActivityFormSheetVm.GoalFormUi>) -> Unit,
) {

    val (vm, state) = rememberVm(_initGoalFormsVmUi) {
        GoalsFormVm(_initGoalFormsVmUi)
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
                _onSelected(state.goalFormsUi)
                _layer.close()
            },
        )

        SpacerW1()
    }
}
