package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.H_PADDING
import me.timeto.app.VStack
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.vm.GoalPickerSheetVm

@Composable
fun GoalPickerSheet(
    layer: WrapperView.Layer,
    onPick: (ActivityDb.Goal) -> Unit,
) {

    val (vm, state) = rememberVm { GoalPickerSheetVm() }

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg)
    ) {

        Sheet__HeaderView(
            title = state.headerTitle,
            scrollState = null,
            bgColor = c.sheetBg,
        )

        Column(
            modifier = Modifier
                .weight(1f)
        ) {

            MyListView__Padding__SectionHeader()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
            ) {
                MyListView__ItemView__ButtonView(
                    text = state.durationTitle,
                    withArrow = true,
                    rightView = {
                        MyListView__ItemView__ButtonView__RightText(
                            text = state.durationNote,
                            paddingEnd = 2.dp,
                        )
                    },
                ) {
                    Sheet.show { layer ->
                        TimerPickerSheet(
                            layer = layer,
                            title = state.timerPickerSheetTitle,
                            doneText = "Done",
                            defMinutes = 60, // todo default
                            onPick = { seconds ->
                                vm.upTime(seconds)
                            }
                        )
                    }
                }
            }

            WeekDaysFormView(
                weekDays = state.weekDays,
                size = 36.dp,
                modifier = Modifier
                    .padding(
                        start = H_PADDING,
                        top = 20.dp,
                    ),
                onChange = { newWeekDays ->
                    vm.upWeekDays(newWeekDays)
                }
            )
        }

        Sheet__BottomViewDefault(
            primaryText = state.doneTitle,
            primaryAction = {
                state.buildGoal { goal ->
                    onPick(goal)
                    layer.close()
                }
            },
            secondaryText = "Cancel",
            secondaryAction = {
                layer.close()
            },
        )
    }
}
