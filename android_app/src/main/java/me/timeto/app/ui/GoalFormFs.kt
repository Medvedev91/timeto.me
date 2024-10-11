package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

        VStack(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
        ) {

            MyListView__PaddingFirst()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
                bgColor = c.fg,
            ) {
                MyListView__ItemView__TextInputView(
                    placeholder = state.notePlaceholder,
                    text = state.note,
                    onTextChanged = { newNote ->
                        vm.setNote(newNote)
                    },
                )
            }
        }
    }
}
