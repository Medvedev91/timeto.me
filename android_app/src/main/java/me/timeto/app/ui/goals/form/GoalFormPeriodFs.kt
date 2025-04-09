package me.timeto.app.ui.goals.form

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.form.plain.FormPlainButtonSelection
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.GoalDb
import me.timeto.shared.ui.goals.form.GoalFormPeriodVm

@Composable
fun GoalFormPeriodFs(
    initGoalDbPeriod: GoalDb.Period?,
    onDone: (GoalDb.Period) -> Unit,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        GoalFormPeriodVm(
            initGoalDbPeriod = initGoalDbPeriod,
        )
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = state.doneText,
                isEnabled = true,
                onClick = {
                    val period: GoalDb.Period = state.buildPeriodOrNull(
                        dialogsManager = navigationFs,
                    ) ?: return@HeaderActionButton
                    onDone(period)
                    navigationLayer.close()
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            )
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f),
            state = scrollState,
        ) {

            item {

                FormPaddingTop()

                vm.daysOfWeek.forEachIndexed { idx, dayOfWeek ->
                    FormPlainButtonSelection(
                        title = dayOfWeek.title,
                        isSelected = dayOfWeek.id in state.selectedDaysOfWeek,
                        isFirst = idx == 0,
                        modifier = Modifier,
                        onClick = {
                            vm.toggleDayOfWeek(dayOfWeek = dayOfWeek)
                        },
                    )
                }
            }
        }
    }
}
