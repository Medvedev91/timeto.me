package me.timeto.app.ui.activity_form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.timeto.app.toColor
import me.timeto.app.ui.HStack
import me.timeto.app.ui.Screen
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.checklists.ChecklistsPickerFs
import me.timeto.app.ui.color_picker.ColorPickerFs
import me.timeto.app.ui.daytime_picker.DaytimePickerSheet
import me.timeto.app.ui.form.FormHeader
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.FormSwitch
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.button.FormButtonArrowView
import me.timeto.app.ui.form.button.FormButtonView
import me.timeto.app.ui.form.padding.FormPaddingBottom
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
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.vm.activity_form.ActivityFormVm

@Composable
fun ActivityFormFs(
    activityDb: ActivityDb?,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ActivityFormVm(
            initActivityDb = activityDb,
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
                isEnabled = state.isDoneEnabled,
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
                    isLast = true,
                    isAutoFocus = activityDb == null,
                    imeAction = ImeAction.Done,
                )

                FormPaddingSectionHeader()

                FormHeader(state.goalHeader)

                val isChecklistGoalType: Boolean =
                    (state.goalTypeUi == ActivityFormVm.GoalTypeUi.Checklist)

                FormButton(
                    title = state.goalTypeTitle,
                    isFirst = true,
                    isLast = state.goalTypeUi == null,
                    note = state.goalTypeNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.picker(
                            title = state.goalTypeTitle,
                            items = buildGoalTypesPickerItems(
                                goalTypesUi = state.goalTypesUi,
                                selectedGoalTypeUi = state.goalTypeUi,
                            ),
                            onDone = { pickerItem ->
                                vm.setGoalType(pickerItem.item)
                            },
                        )
                    },
                )

                if (state.goalTypeUi == ActivityFormVm.GoalTypeUi.Timer) {
                    FormButton(
                        title = state.goalTimerTitle,
                        isFirst = false,
                        isLast = true,
                        note = state.goalTimerNote,
                        withArrow = true,
                        onClick = {
                            navigationFs.push {
                                TimerSheet(
                                    title = state.goalTimerTitle,
                                    doneTitle = "Done",
                                    initSeconds = state.goalTimerSeconds,
                                    hints = state.initActivityDb?.buildTimerHints() ?: emptyList(),
                                    onDone = { newSeconds ->
                                        vm.setGoalTimer(seconds = newSeconds)
                                    },
                                )
                            }
                        },
                    )
                }

                if (state.goalTypeUi == ActivityFormVm.GoalTypeUi.Counter) {
                    FormButton(
                        title = state.goalCounterTitle,
                        isFirst = false,
                        isLast = true,
                        note = state.goalCounterNote,
                        withArrow = true,
                        onClick = {
                            navigationFs.picker(
                                title = state.goalCounterTitle,
                                items = buildGoalCounterPickerItems(
                                    goalCountItemsUi = state.goalCountItemsUi,
                                    selectedGoalCount = state.goalCounterCount,
                                ),
                                onDone = { pickerItem ->
                                    vm.setGoalCounter(pickerItem.item.count)
                                },
                            )
                        },
                    )
                }

                if (!isChecklistGoalType) {
                    FormPaddingSectionSection()
                }

                FormButton(
                    title = "Checklist",
                    isFirst = !isChecklistGoalType,
                    isLast = true,
                    note = state.checklistsNote,
                    noteColor = if (isChecklistGoalType && state.checklistsDb.isEmpty()) c.red else null,
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

                FormPaddingSectionSection()

                FormButton(
                    title = state.periodTitle,
                    isFirst = true,
                    isLast = true,
                    note = state.periodNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            ActivityFormPeriodFs(
                                initActivityDbPeriod = state.period,
                                onDone = { newPeriod ->
                                    vm.setPeriod(newPeriod = newPeriod)
                                },
                            )
                        }
                    },
                )

                FormPaddingSectionSection()

                FormButton(
                    title = state.timerTypeTitle,
                    isFirst = true,
                    isLast = !state.showFixedTimerPicker && !state.showDaytimeTimerPicker,
                    note = state.timerTypeUi.title,
                    withArrow = true,
                    onClick = {
                        navigationFs.picker(
                            title = state.timerTypeTitle,
                            items = buildTimerTypesPickerItems(
                                timerTypeItemsUi = state.timerTypesUi,
                                selectedTimerTypeUi = state.timerTypeUi,
                            ),
                            onDone = { pickerItem ->
                                vm.setTimerType(pickerItem.item)
                            },
                        )
                    },
                )

                if (state.showFixedTimerPicker) {
                    FormButton(
                        title = state.fixedTimerTitle,
                        isFirst = false,
                        isLast = true,
                        note = state.fixedTimerNote,
                        withArrow = true,
                        onClick = {
                            navigationFs.push {
                                TimerSheet(
                                    title = state.fixedTimerTitle,
                                    doneTitle = "Done",
                                    initSeconds = state.fixedTimer,
                                    hints = emptyList(),
                                    onDone = { seconds ->
                                        vm.setFixedTimer(newFixedTimer = seconds)
                                    },
                                )
                            }
                        },
                    )
                }

                if (state.showDaytimeTimerPicker) {
                    FormButton(
                        title = state.daytimeTimerTitle,
                        isFirst = false,
                        isLast = true,
                        note = state.daytimeTimerNote,
                        withArrow = true,
                        onClick = {
                            navigationFs.push {
                                DaytimePickerSheet(
                                    title = state.daytimeTimerTitle,
                                    doneText = "Done",
                                    daytimeUi = state.timerDaytimeUi,
                                    withRemove = false,
                                    onDone = { daytimePickerUi ->
                                        vm.setDaytimeTimer(daytimePickerUi)
                                    },
                                    onRemove = {},
                                )
                            }
                        },
                    )
                }

                FormPaddingSectionSection()

                FormButton(
                    title = state.parentActivityTitle,
                    isFirst = true,
                    isLast = true,
                    note = state.parentActivityUi?.title ?: "None",
                    withArrow = true,
                    onClick = {
                        navigationFs.picker(
                            title = state.parentActivityTitle,
                            items = buildActivitiesPickerItems(
                                activitiesUi = state.parentActivitiesUi,
                                selectedActivityUi = state.parentActivityUi,
                            ),
                            onDone = { newActivity ->
                                vm.setParentActivityUi(newActivity.item)
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
                                pomodoroItemsUi = state.pomodoroItemsUi,
                                selectedPomodoroTimer = state.pomodoroTimer,
                            ),
                            onDone = { newPomodoroItemUi ->
                                vm.setPomodoroTimer(newPomodoroItemUi.item.timer)
                            },
                        )
                    },
                )

                FormButton(
                    title = "Timer Hints",
                    isFirst = false,
                    isLast = false,
                    note = state.timerHintsNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            ActivityFormTimerHintsFs(
                                initTimerHints = state.timerHints,
                                onDone = { newTimerHints ->
                                    vm.setTimerHints(newTimerHints)
                                },
                            )
                        }
                    },
                )

                FormButton(
                    title = "Shortcuts",
                    isFirst = false,
                    isLast = false,
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

                FormSwitch(
                    title = state.keepScreenOnTitle,
                    isEnabled = state.keepScreenOn,
                    isFirst = false,
                    isLast = true,
                    onChange = { newKeepScreenOn ->
                        vm.setKeepScreenOn(newKeepScreenOn = newKeepScreenOn)
                    },
                )

                if (activityDb != null) {
                    FormPaddingSectionSection()
                    FormButton(
                        title = "Delete Activity",
                        titleColor = c.red,
                        isFirst = true,
                        isLast = true,
                        onClick = {
                            vm.delete(
                                activityDb = activityDb,
                                dialogsManager = navigationFs,
                                onSuccess = {
                                    navigationLayer.close()
                                },
                            )
                        },
                    )
                }

                FormPaddingBottom(
                    withNavigation = true,
                )
            }
        }
    }
}

private fun buildGoalTypesPickerItems(
    goalTypesUi: List<ActivityFormVm.GoalTypeUi>,
    selectedGoalTypeUi: ActivityFormVm.GoalTypeUi?,
): List<NavigationPickerItem<ActivityFormVm.GoalTypeUi?>> {
    val list = mutableListOf<NavigationPickerItem<ActivityFormVm.GoalTypeUi?>>()
    list.add(
        NavigationPickerItem(
            title = "None",
            isSelected = selectedGoalTypeUi == null,
            item = null,
        )
    )
    goalTypesUi.forEach { goalTypeUi ->
        list.add(
            NavigationPickerItem(
                title = goalTypeUi.title,
                isSelected = selectedGoalTypeUi == goalTypeUi,
                item = goalTypeUi,
            )
        )
    }
    return list
}

private fun buildGoalCounterPickerItems(
    goalCountItemsUi: List<ActivityFormVm.GoalCountUi>,
    selectedGoalCount: Int,
): List<NavigationPickerItem<ActivityFormVm.GoalCountUi>> {
    return goalCountItemsUi.map { goalCountUi ->
        NavigationPickerItem(
            title = goalCountUi.title,
            isSelected = selectedGoalCount == goalCountUi.count,
            item = goalCountUi,
        )
    }
}

private fun buildActivitiesPickerItems(
    activitiesUi: List<ActivityFormVm.ActivityUi>,
    selectedActivityUi: ActivityFormVm.ActivityUi?,
): List<NavigationPickerItem<ActivityFormVm.ActivityUi?>> {
    val list = mutableListOf<NavigationPickerItem<ActivityFormVm.ActivityUi?>>()
    list.add(
        NavigationPickerItem(
            title = "None",
            isSelected = selectedActivityUi == null,
            item = null,
        )
    )
    activitiesUi.forEach { activityUi ->
        list.add(
            NavigationPickerItem(
                title = activityUi.title,
                isSelected = selectedActivityUi?.activityDb?.id == activityUi.activityDb.id,
                item = activityUi,
            )
        )
    }
    return list
}

private fun buildTimerTypesPickerItems(
    timerTypeItemsUi: List<ActivityFormVm.TimerTypeUi>,
    selectedTimerTypeUi: ActivityFormVm.TimerTypeUi,
): List<NavigationPickerItem<ActivityFormVm.TimerTypeUi>> {
    return timerTypeItemsUi.map { timerTypeItemUi ->
        NavigationPickerItem(
            title = timerTypeItemUi.title,
            isSelected = selectedTimerTypeUi == timerTypeItemUi,
            item = timerTypeItemUi,
        )
    }
}

private fun buildPomodoroPickerItems(
    pomodoroItemsUi: List<ActivityFormVm.PomodoroItemUi>,
    selectedPomodoroTimer: Int,
): List<NavigationPickerItem<ActivityFormVm.PomodoroItemUi>> {
    return pomodoroItemsUi.map { pomodoroItemUi ->
        NavigationPickerItem(
            title = pomodoroItemUi.title,
            isSelected = selectedPomodoroTimer == pomodoroItemUi.timer,
            item = pomodoroItemUi,
        )
    }
}
