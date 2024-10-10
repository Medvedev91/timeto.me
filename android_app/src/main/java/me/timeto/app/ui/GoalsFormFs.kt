package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.*
import me.timeto.shared.vm.ActivityFormSheetVm
import me.timeto.shared.vm.GoalsFormVm

@Composable
fun GoalsFormFs(
    _layer: WrapperView.Layer,
    _initGoalFormsUi: List<ActivityFormSheetVm.GoalFormUi>,
    _onSelected: (List<ActivityFormSheetVm.GoalFormUi>) -> Unit,
) {

    val (vm, state) = rememberVm(_initGoalFormsUi) {
        GoalsFormVm(_initGoalFormsUi)
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

        Fs__BottomBar {

            Fs__BottomBar__PlusButton(
                text = state.newGoalButtonText,
                modifier = Modifier
                    .padding(start = H_PADDING_HALF)
                    .padding(vertical = 8.dp),
                onClick = {
                },
            )
        }
    }
}
