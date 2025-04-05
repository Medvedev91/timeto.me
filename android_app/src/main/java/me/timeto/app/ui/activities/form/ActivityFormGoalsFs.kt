package me.timeto.app.ui.activities.form

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.rememberVm
import me.timeto.app.ui.goals.form.GoalFormFs
import me.timeto.app.ui.Screen
import me.timeto.app.ui.footer.Footer
import me.timeto.app.ui.footer.FooterAddButton
import me.timeto.app.ui.form.FormButton
import me.timeto.app.ui.form.FormPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.ui.activities.form.ActivityFormGoalsVm
import me.timeto.shared.ui.goals.form.GoalFormData
import me.timeto.shared.ui.goals.form.GoalFormStrategy

@Composable
fun ActivityFormGoalsFs(
    initGoalFormsData: List<GoalFormData>,
    onDone: (List<GoalFormData>) -> Unit,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ActivityFormGoalsVm(
            initGoalFormsData = initGoalFormsData,
        )
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = "Goals",
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = "Done",
                isEnabled = true,
                onClick = {
                    onDone(state.goalFormsData)
                    navigationLayer.close()
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            ),
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f),
            state = scrollState,
        ) {

            item {
                FormPaddingTop()
            }

            val goalFormsData = state.goalFormsData
            goalFormsData.forEachIndexed { idx, goalFormData ->
                item {
                    FormButton(
                        title = goalFormData.formListTitle,
                        isFirst = idx == 0,
                        isLast = goalFormsData.last() == goalFormData,
                        note = goalFormData.formListNote,
                        withArrow = true,
                        onClick = {
                            navigationFs.push {
                                GoalFormFs(
                                    strategy = GoalFormStrategy.EditFormData(
                                        initGoalFormData = goalFormData,
                                        onDone = { newGoalFormData ->
                                            vm.updateGoalFormData(
                                                idx = idx,
                                                new = newGoalFormData
                                            )
                                        },
                                        onDelete = {
                                            vm.deleteGoalFormData(idx = idx)
                                        },
                                    ),
                                )
                            }
                        },
                    )
                }
            }
        }

        Footer(
            scrollState = scrollState,
            contentModifier = Modifier,
            content = {
                FooterAddButton(
                    text = state.newGoalTitle,
                    onClick = {
                        navigationFs.push {
                            GoalFormFs(
                                strategy = vm.newGoalStrategy,
                            )
                        }
                    },
                )
            },
        )
    }
}
