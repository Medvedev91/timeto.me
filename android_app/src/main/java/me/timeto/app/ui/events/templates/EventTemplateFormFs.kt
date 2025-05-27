package me.timeto.app.ui.events.templates

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
import me.timeto.app.ui.activities.ActivityPickerFs
import me.timeto.app.ui.checklists.ChecklistsPickerFs
import me.timeto.app.ui.daytime_picker.DaytimePickerSheet
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.button.FormButton
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
import me.timeto.shared.db.EventTemplateDb
import me.timeto.shared.ui.events.templates.EventTemplateFormVm

@Composable
fun EventTemplateFormFs(
    initEventTemplateDb: EventTemplateDb?,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        EventTemplateFormVm(
            initEventTemplateDb = initEventTemplateDb,
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
                    title = state.daytimeTitle,
                    isFirst = true,
                    isLast = true,
                    note = state.daytimeNote,
                    noteColor = if (state.daytimeUi == null) c.red else c.secondaryText,
                    onClick = {
                        navigationFs.push {
                            DaytimePickerSheet(
                                title = state.daytimeTitle,
                                doneText = "Done",
                                daytimeUi = state.daytimeUiPicker,
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
                    title = state.activityTitle,
                    isFirst = true,
                    isLast = false,
                    note = state.activityNote,
                    noteColor = if (state.activityDb == null) c.red else c.secondaryText,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            ActivityPickerFs(
                                initActivityDb = state.activityDb,
                                onDone = { newActivityDb ->
                                    vm.setActivity(newActivityDb)
                                },
                            )
                        }
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
                                initSeconds = state.timerSecondsPicker,
                                onDone = { newTimerSeconds ->
                                    vm.setTimer(seconds = newTimerSeconds)
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
                                }
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

                val eventTemplateDb: EventTemplateDb? = vm.initEventTemplateDb
                if (eventTemplateDb != null) {
                    FormPaddingSectionSection()
                    FormButton(
                        title = state.deleteText,
                        titleColor = c.red,
                        isFirst = true,
                        isLast = true,
                        onClick = {
                            vm.delete(
                                eventTemplateDb = eventTemplateDb,
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
