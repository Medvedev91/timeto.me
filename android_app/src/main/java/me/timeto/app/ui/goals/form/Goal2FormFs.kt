package me.timeto.app.ui.goals.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.timeto.app.toColor
import me.timeto.app.ui.HStack
import me.timeto.app.ui.Screen
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.checklists.ChecklistsPickerFs
import me.timeto.app.ui.color_picker.ColorPickerFs
import me.timeto.app.ui.form.FormHeader
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.FormSwitch
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.button.FormButtonArrowView
import me.timeto.app.ui.form.button.FormButtonView
import me.timeto.app.ui.form.padding.FormPaddingHeaderSection
import me.timeto.app.ui.form.padding.FormPaddingSectionHeader
import me.timeto.app.ui.form.padding.FormPaddingSectionSection
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.navigation.picker.NavigationPickerItem
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.shortcuts.ShortcutsPickerFs
import me.timeto.app.ui.timer.TimerSheet
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.vm.goals.form.Goal2FormVm

@Composable
fun Goal2FormFs(
    goalDb: Goal2Db?,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        Goal2FormVm(
            initGoalDb = goalDb,
        )
    }

    Screen(
        modifier = Modifier
            .imePadding(),
    ) {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = state.doneText,
                isEnabled = true,
                onClick = {
                    vm.save(
                        dialogsManager = navigationFs,
                        onSuccess = {
                            navigationLayer.close()
                        },
                    )
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

                FormInput(
                    initText = state.name,
                    placeholder = state.namePlaceholder,
                    onChange = { newName ->
                        vm.setName(newName)
                    },
                    isFirst = true,
                    isLast = false,
                    isAutoFocus = goalDb == null,
                    imeAction = ImeAction.Done,
                )

                FormButton(
                    title = state.secondsTitle,
                    isFirst = false,
                    isLast = true,
                    note = state.secondsNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            TimerSheet(
                                title = state.secondsTitle,
                                doneTitle = "Done",
                                initSeconds = state.seconds,
                                onDone = { newSeconds ->
                                    vm.setSeconds(newSeconds = newSeconds)
                                },
                            )
                        }
                    },
                )

                FormPaddingSectionSection()

                FormButton(
                    title = "Checklists",
                    isFirst = true,
                    isLast = false,
                    note = state.checklistsNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            ChecklistsPickerFs(
                                initChecklistsDb = state.checklistsDb,
                                onDone = { newChecklistsDb ->
                                    vm.setChecklistsDb(newChecklistsDb)
                                },
                            )
                        }
                    },
                )

                FormButton(
                    title = "Shortcuts",
                    isFirst = false,
                    isLast = true,
                    note = state.shortcutsNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            ShortcutsPickerFs(
                                initShortcutsDb = state.shortcutsDb,
                                onDone = { newShortcutsDb ->
                                    vm.setShortcutsDb(newShortcutsDb)
                                },
                            )
                        }
                    },
                )

                FormPaddingSectionSection()

                FormButton(
                    title = state.periodTitle,
                    isFirst = true,
                    isLast = true,
                    note = state.periodNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            GoalFormPeriodFs(
                                initGoalDbPeriod = state.period,
                                onDone = { newPeriod ->
                                    vm.setPeriod(newPeriod = newPeriod)
                                },
                            )
                        }
                    },
                )

                FormPaddingSectionHeader()

                FormHeader(state.timerHeader)

                FormPaddingHeaderSection()

                val timer: Int = state.timer
                val isTimerRestOfBar: MutableState<Boolean> =
                    remember(timer) { mutableStateOf(timer == 0) }

                FormSwitch(
                    title = state.timerTitleRest,
                    isEnabled = isTimerRestOfBar.value,
                    isFirst = true,
                    isLast = isTimerRestOfBar.value,
                    onChange = { newValue ->
                        vm.setTimer(if (newValue) 0 else (45 * 60))
                    },
                )

                if (!isTimerRestOfBar.value) {
                    FormButton(
                        title = state.timerTitleTimer,
                        isFirst = false,
                        isLast = true,
                        note = state.timerNote,
                        withArrow = true,
                        onClick = {
                            navigationFs.push {
                                TimerSheet(
                                    title = state.timerHeader,
                                    doneTitle = "Done",
                                    initSeconds = state.timer,
                                    onDone = { seconds ->
                                        vm.setTimer(newTimer = seconds)
                                    },
                                )
                            }
                        },
                    )
                }

                FormPaddingSectionSection()

                FormButton(
                    title = state.parentGoalTitle,
                    isFirst = true,
                    isLast = true,
                    note = state.parentGoalUi?.title ?: "None",
                    withArrow = true,
                    onClick = {
                        navigationFs.picker(
                            title = state.parentGoalTitle,
                            items = buildGoalsPickerItems(
                                goalsUi = state.parentGoalsUi,
                                selectedGoalUi = state.parentGoalUi,
                            ),
                            onDone = { newGoal ->
                                vm.setParentGoalUi(newGoal.item)
                            },
                        )
                    },
                )

                FormPaddingSectionSection()

                FormButtonView(
                    title = state.colorTitle,
                    titleColor = null,
                    isFirst = true,
                    isLast = true,
                    modifier = Modifier,
                    rightView = {
                        HStack(
                            verticalAlignment = CenterVertically,
                        ) {
                            ZStack(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(28.dp)
                                    .clip(roundedShape)
                                    .background(state.colorRgba.toColor()),
                            )
                            FormButtonArrowView()
                        }
                    },
                    onClick = {
                        navigationFs.push {
                            ColorPickerFs(
                                title = state.colorPickerTitle,
                                examplesUi = state.buildColorPickerExamplesUi(),
                                onDone = { newColorRgba ->
                                    vm.setColorRgba(newColorRgba = newColorRgba)
                                },
                            )
                        }
                    },
                    onLongClick = null,
                )

                FormPaddingSectionSection()

                FormButton(
                    title = state.pomodoroTitle,
                    isFirst = true,
                    isLast = false,
                    note = state.pomodoroNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.picker(
                            title = state.pomodoroTitle,
                            items = buildPomodoroPickerItems(
                                pomodoroItemUi = state.pomodoroItemsUi,
                                selectedPomodoroTimer = state.pomodoroTimer,
                            ),
                            onDone = { newPomodoroItemUi ->
                                vm.setPomodoroTimer(newPomodoroItemUi.item.timer)
                            },
                        )
                    },
                )

                FormSwitch(
                    title = state.keepScreenOnTitle,
                    isEnabled = state.keepScreenOn,
                    isFirst = false,
                    isLast = true,
                    onChange = { newKeepScreenOn ->
                        vm.setKeepScreenOn(newKeepScreenOn = newKeepScreenOn)
                    },
                )
            }
        }
    }
}

private fun buildGoalsPickerItems(
    goalsUi: List<Goal2FormVm.GoalUi>,
    selectedGoalUi: Goal2FormVm.GoalUi?,
): List<NavigationPickerItem<Goal2FormVm.GoalUi?>> {
    val list = mutableListOf<NavigationPickerItem<Goal2FormVm.GoalUi?>>()
    list.add(
        NavigationPickerItem(
            title = "None",
            isSelected = selectedGoalUi == null,
            item = null,
        )
    )
    goalsUi.forEach { goalUi ->
        list.add(
            NavigationPickerItem(
                title = goalUi.title,
                isSelected = selectedGoalUi?.goalDb?.id == goalUi.goalDb.id,
                item = goalUi,
            )
        )
    }
    return list
}

private fun buildPomodoroPickerItems(
    pomodoroItemUi: List<Goal2FormVm.PomodoroItemUi>,
    selectedPomodoroTimer: Int,
): List<NavigationPickerItem<Goal2FormVm.PomodoroItemUi>> {
    val list = mutableListOf<NavigationPickerItem<Goal2FormVm.PomodoroItemUi>>()
    pomodoroItemUi.forEach { pomodoroItemUi ->
        list.add(
            NavigationPickerItem(
                title = pomodoroItemUi.title,
                isSelected = selectedPomodoroTimer == pomodoroItemUi.timer,
                item = pomodoroItemUi,
            )
        )
    }
    return list
}
