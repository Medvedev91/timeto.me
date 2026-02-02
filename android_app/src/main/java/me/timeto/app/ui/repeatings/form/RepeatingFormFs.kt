package me.timeto.app.ui.repeatings.form

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import me.timeto.app.ui.c
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.checklists.ChecklistsPickerFs
import me.timeto.app.ui.daytime_picker.DaytimePickerSheet
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.FormSwitch
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.padding.FormPaddingBottom
import me.timeto.app.ui.form.padding.FormPaddingSectionSection
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.navigation.picker.NavigationPickerItem
import me.timeto.app.ui.shortcuts.ShortcutsPickerFs
import me.timeto.app.ui.timer.TimerSheet
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.vm.repeatings.form.RepeatingFormVm

@Composable
fun RepeatingFormFs(
    initRepeatingDb: RepeatingDb?,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        RepeatingFormVm(
            initRepeatingDb = initRepeatingDb,
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
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
        ) {

            item {

                FormPaddingTop()

                FormInput(
                    initText = state.text,
                    placeholder = state.textPlaceholder,
                    onChange = { newText ->
                        vm.setText(newText)
                    },
                    isFirst = true,
                    isLast = true,
                    isAutoFocus = false,
                    imeAction = ImeAction.Done,
                )

                FormPaddingSectionSection()

                FormButton(
                    title = state.periodTitle,
                    isFirst = true,
                    isLast = false,
                    note = state.periodNote,
                    noteColor = if (state.period == null) c.red else c.secondaryText,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            RepeatingFormPeriodFs(
                                initPeriod = state.period,
                                onDone = { newPeriod ->
                                    vm.setPeriod(newPeriod)
                                },
                            )
                        }
                    },
                )

                FormButton(
                    title = state.daytimeTitle,
                    isFirst = false,
                    isLast = true,
                    note = state.daytimeNote,
                    noteColor = if (state.daytimeUi == null) c.red else c.secondaryText,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            DaytimePickerSheet(
                                title = state.daytimeTitle,
                                doneText = "Done",
                                daytimeUi = state.daytimePickerUi,
                                withRemove = false,
                                onDone = { newDaytime ->
                                    vm.setDaytime(newDaytime)
                                },
                                onRemove = {},
                            )
                        }
                    },
                )

                FormPaddingSectionSection()

                FormButton(
                    title = state.goalTitle,
                    isFirst = true,
                    isLast = false,
                    note = state.goalNote,
                    noteColor = if (state.goalDb == null) c.red else c.secondaryText,
                    withArrow = true,
                    onClick = {
                        navigationFs.picker(
                            title = state.goalTitle,
                            items = buildGoalsPickerItems(
                                goalsUi = state.goalsUi,
                                selectedGoalDb = state.goalDb,
                            ),
                            onDone = { newGoal ->
                                vm.setGoal(newGoal.item)
                            },
                        )
                    },
                )

                FormButton(
                    title = state.timerTitle,
                    isFirst = false,
                    isLast = true,
                    note = state.timerNote,
                    noteColor = if (state.timerSeconds == null) c.red else c.secondaryText,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            TimerSheet(
                                title = state.timerTitle,
                                doneTitle = "Done",
                                initSeconds = state.timerPickerSeconds,
                                hints = state.goalDb?.buildTimerHints() ?: emptyList(),
                                onDone = { newTimerSeconds ->
                                    vm.setTimerSeconds(newTimerSeconds)
                                },
                            )
                        }
                    },
                )

                FormPaddingSectionSection()

                FormButton(
                    title = state.checklistsTitle,
                    isFirst = true,
                    isLast = false,
                    note = state.checklistsNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            ChecklistsPickerFs(
                                initChecklistsDb = state.checklistsDb,
                                onDone = { newChecklistsDb ->
                                    vm.setChecklists(newChecklistsDb)
                                },
                            )
                        }
                    },
                )

                FormButton(
                    title = state.shortcutsTitle,
                    isFirst = false,
                    isLast = true,
                    note = state.shortcutsNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            ShortcutsPickerFs(
                                initShortcutsDb = state.shortcutsDb,
                                onDone = { newShortcutsDb ->
                                    vm.setShortcuts(newShortcutsDb)
                                }
                            )
                        }
                    },
                )

                FormPaddingSectionSection()

                FormSwitch(
                    title = "Display in Calendar",
                    isEnabled = state.inCalendar,
                    isFirst = true,
                    isLast = false,
                    onChange = { newInCalendar ->
                        vm.setInCalendar(newInCalendar)
                    },
                )

                FormSwitch(
                    title = state.isImportantTitle,
                    isEnabled = state.isImportant,
                    isFirst = false,
                    isLast = true,
                    onChange = { newIsImportant ->
                        vm.setIsImportant(newIsImportant)
                    },
                )

                val repeatingDb: RepeatingDb? = state.initRepeatingDb
                if (repeatingDb != null) {
                    FormPaddingSectionSection()
                    FormButton(
                        title = "Delete Repeating Task",
                        titleColor = c.red,
                        isFirst = true,
                        isLast = true,
                        onClick = {
                            vm.delete(
                                repeatingDb = repeatingDb,
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

private fun buildGoalsPickerItems(
    goalsUi: List<RepeatingFormVm.GoalUi>,
    selectedGoalDb: Goal2Db?,
): List<NavigationPickerItem<Goal2Db>> = goalsUi.map { goalUi ->
    NavigationPickerItem(
        title = goalUi.title,
        isSelected = selectedGoalDb?.id == goalUi.goalDb.id,
        item = goalUi.goalDb,
    )
}
