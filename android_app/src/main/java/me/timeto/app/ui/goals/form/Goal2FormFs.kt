package me.timeto.app.ui.goals.form

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import me.timeto.app.ui.Screen
import me.timeto.app.ui.checklists.ChecklistsPickerFs
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.padding.FormPaddingSectionSection
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.rememberVm
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
            }
        }
    }
}
