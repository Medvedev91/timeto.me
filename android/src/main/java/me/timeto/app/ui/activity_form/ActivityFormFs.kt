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
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.FormSwitch
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.button.FormButtonArrowView
import me.timeto.app.ui.form.button.FormButtonView
import me.timeto.app.ui.form.padding.FormPaddingBottom
import me.timeto.app.ui.form.padding.FormPaddingSectionSection
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.goals.form.GoalFormPeriodFs
import me.timeto.app.ui.goals.form.GoalFormTimerHintsFs
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
                    isLast = false,
                    isAutoFocus = activityDb == null,
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
                                hints = state.initActivityDb?.buildTimerHints() ?: emptyList(),
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
                    note = state.timerTypeItemsUi.first { it.id == state.timerTypeId }.title,
                    withArrow = true,
                    onClick = {
                        navigationFs.picker(
                            title = state.timerTypeTitle,
                            items = buildTimerTypesPickerItems(
                                timerTypeItemsUi = state.timerTypeItemsUi,
                                selectedTimerTypeId = state.timerTypeId,
                            ),
                            onDone = { newTimerTypeUi ->
                                vm.setTimerTypeId(newTimerTypeUi.item.id)
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
                            GoalFormTimerHintsFs(
                                initTimerHints = state.timerHints,
                                onDone = { newTimerHints ->
                                    vm.setTimerHints(newTimerHints)
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
    timerTypeItemsUi: List<ActivityFormVm.TimerTypeItemUi>,
    selectedTimerTypeId: ActivityFormVm.TimerTypeItemUi.TimerTypeUiId,
): List<NavigationPickerItem<ActivityFormVm.TimerTypeItemUi>> {
    return timerTypeItemsUi.map { timerTypeItemUi ->
        NavigationPickerItem(
            title = timerTypeItemUi.title,
            isSelected = selectedTimerTypeId == timerTypeItemUi.id,
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
