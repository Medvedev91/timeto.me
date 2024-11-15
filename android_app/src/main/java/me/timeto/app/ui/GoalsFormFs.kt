package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.*
import me.timeto.shared.models.GoalFormUi
import me.timeto.shared.vm.GoalsFormVm

@Composable
fun GoalsFormFs(
    _layer: WrapperView.Layer,
    _initGoalFormsUi: List<GoalFormUi>,
    _onSelected: (List<GoalFormUi>) -> Unit,
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

        VStack(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(state = scrollState),
        ) {

            MyListView__PaddingFirst()

            val goalFormsUi = state.goalFormsUi
            state.goalFormsUi.forEachIndexed { idx, formUi ->

                MyListView__ItemView(
                    isFirst = idx == 0,
                    isLast = goalFormsUi.size -1 == idx,
                    bgColor = c.fg,
                    withTopDivider = idx > 0,
                ) {

                    MyListView__Item__Button(
                        text = formUi.period.note(),
                        rightView = {
                            MyListView__Item__Button__RightText(
                                text = formUi.durationString,
                            )
                        },
                    ) {
                        Fs.show { layer_ ->
                            GoalFormFs(
                                _layer = layer_,
                                _initGoalFormUi = formUi,
                                _onSelect = { newFormUi ->
                                    vm.upGoalFormUi(idx = idx, goalFormUi = newFormUi)
                                },
                            )
                        }
                    }
                }
            }
        }

        Fs__BottomBar {

            Fs__BottomBar__PlusButton(
                text = state.newGoalButtonText,
                modifier = Modifier
                    .padding(start = H_PADDING_HALF)
                    .padding(vertical = 8.dp),
                onClick = {
                    Fs.show { goalFormLayer ->
                        GoalFormFs(
                            _layer = goalFormLayer,
                            _initGoalFormUi = null,
                            _onSelect = { newGoalFormUi ->
                                vm.addGoalFormUi(newGoalFormUi)
                            },
                        )
                    }
                },
            )
        }
    }
}
