package me.timeto.app.ui.goals.form

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
import me.timeto.app.ui.emoji.EmojiPickerFs
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.button.FormButtonEmoji
import me.timeto.app.ui.form.padding.FormPaddingBottom
import me.timeto.app.ui.form.padding.FormPaddingSectionSection
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.shortcuts.ShortcutsPickerFs
import me.timeto.app.ui.timer.TimerSheet
import me.timeto.shared.vm.goals.form.GoalFormData
import me.timeto.shared.vm.goals.form.GoalFormStrategy
import me.timeto.shared.vm.goals.form.GoalFormVm

@Composable
fun GoalFormFs(
    strategy: GoalFormStrategy,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        GoalFormVm(
            strategy = strategy,
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
                text = "Done",
                isEnabled = true,
                onClick = {
                    when (strategy) {
                        is GoalFormStrategy.NewFormData -> {
                            val formData: GoalFormData = state.buildFormDataOrNull(
                                dialogsManager = navigationFs,
                                goalDb = null,
                            ) ?: return@HeaderActionButton
                            strategy.onDone(formData)
                            navigationLayer.close()
                        }
                        is GoalFormStrategy.EditFormData -> {
                            val formData: GoalFormData = state.buildFormDataOrNull(
                                dialogsManager = navigationFs,
                                goalDb = strategy.initGoalFormData.goalDb,
                            ) ?: return@HeaderActionButton
                            strategy.onDone(formData)
                            navigationLayer.close()
                        }
                        is GoalFormStrategy.NewGoal -> {
                            vm.addGoal(
                                activityDb = strategy.activityDb,
                                dialogsManager = navigationFs,
                                onCreate = { newGoalDb ->
                                    strategy.onCreate(newGoalDb)
                                    navigationLayer.close()
                                },
                            )
                        }
                        is GoalFormStrategy.EditGoal -> {
                            vm.saveGoal(
                                goalDb = strategy.goalDb,
                                dialogsManager = navigationFs,
                                onSuccess = {
                                    navigationLayer.close()
                                },
                            )
                        }
                    }
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
                    initText = state.note,
                    placeholder = state.notePlaceholder,
                    onChange = { newNote ->
                        vm.setNote(newNote)
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
                    noteColor = if (state.period == null) c.red else null,
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
                    title = state.timerHeader,
                    isFirst = true,
                    isLast = false,
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

                FormButtonEmoji(
                    title = state.finishedTextTitle,
                    emoji = state.finishedText,
                    isFirst = false,
                    isLast = true,
                    onClick = {
                        navigationFs.push {
                            EmojiPickerFs(
                                onDone = { emoji ->
                                    vm.setFinishedText(emoji)
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
                                }
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
                                }
                            )
                        }
                    },
                )

                if (strategy is GoalFormStrategy.EditFormData) {
                    DeleteButton {
                        strategy.onDelete()
                        navigationLayer.close()
                    }
                } else if (strategy is GoalFormStrategy.EditGoal) {
                    DeleteButton {
                        vm.deleteGoal(strategy.goalDb)
                        navigationLayer.close()
                    }
                }

                FormPaddingBottom(
                    withNavigation = true,
                )
            }
        }
    }
}

@Composable
private fun DeleteButton(
    onClick: () -> Unit,
) {
    FormPaddingSectionSection()
    FormButton(
        title = "Delete Goal",
        titleColor = c.red,
        isFirst = true,
        isLast = true,
        onClick = {
            onClick()
        },
    )
}
