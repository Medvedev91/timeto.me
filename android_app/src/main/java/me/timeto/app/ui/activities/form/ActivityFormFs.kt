package me.timeto.app.ui.activities.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack
import me.timeto.app.ZStack
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.app.roundedShape
import me.timeto.app.toColor
import me.timeto.app.ui.Screen
import me.timeto.app.ui.checklists.ChecklistsPickerFs
import me.timeto.app.ui.color.ColorPickerFs
import me.timeto.app.ui.emoji.EmojiPickerFs
import me.timeto.app.ui.form.FormButton
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.FormPaddingBottom
import me.timeto.app.ui.form.FormPaddingSectionSection
import me.timeto.app.ui.form.FormPaddingTop
import me.timeto.app.ui.form.FormSwitch
import me.timeto.app.ui.form.views.FormButtonArrowView
import me.timeto.app.ui.form.views.FormButtonNoteView
import me.timeto.app.ui.form.views.FormButtonView
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.shortcuts.ShortcutsPickerFs
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.ui.activities.form.ActivityFormVm

@Composable
fun ActivityFormFs(
    initActivityDb: ActivityDb?,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ActivityFormVm(
            initActivityDb = initActivityDb,
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
                text = state.saveText,
                isEnabled = true,
                onClick = {
                    vm.save(
                        dialogsManager = navigationFs,
                        onSave = {
                            navigationLayer.close()
                        }
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
                    initText = state.name,
                    placeholder = state.namePlaceholder,
                    onChange = { newName ->
                        vm.setName(newName)
                    },
                    isFirst = true,
                    isLast = true,
                    isAutoFocus = false,
                    imeAction = ImeAction.Done,
                )

                FormPaddingSectionSection()

                FormButtonView(
                    title = state.emojiTitle,
                    titleColor = null,
                    isFirst = true,
                    isLast = false,
                    modifier = Modifier,
                    rightView = {
                        HStack(
                            verticalAlignment = CenterVertically,
                        ) {
                            val emoji: String? = state.emoji
                            if (emoji != null) {
                                Text(
                                    text = emoji,
                                    fontSize = 20.sp,
                                    modifier = Modifier
                                        .padding(end = 9.dp)
                                )
                            } else {
                                FormButtonNoteView(
                                    note = state.emojiNotSelected,
                                    color = c.red,
                                    withArrow = true,
                                )
                            }
                            FormButtonArrowView()
                        }
                    },
                    onClick = {
                        navigationFs.push {
                            EmojiPickerFs(
                                onPick = { newEmoji ->
                                    vm.setEmoji(newEmoji = newEmoji)
                                },
                            )
                        }
                    },
                    onLongClick = null,
                )

                FormButtonView(
                    title = state.colorTitle,
                    titleColor = null,
                    isFirst = false,
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
                                examplesData = state.buildColorPickerExamplesData(),
                                onPick = { newColorRgba ->
                                    vm.setColorRgba(newColorRgba = newColorRgba)
                                },
                            )
                        }
                    },
                    onLongClick = null,
                )

                FormPaddingSectionSection()

                FormSwitch(
                    title = state.keepScreenOnTitle,
                    isEnabled = state.keepScreenOn,
                    isFirst = true,
                    isLast = false,
                    onChange = { newKeepScreenOn ->
                        vm.setKeepScreenOn(newKeepScreenOn = newKeepScreenOn)
                    },
                )

                FormButton(
                    title = state.pomodoroTitle,
                    isFirst = false,
                    isLast = true,
                    note = state.pomodoroNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            ActivityFormPomodoroFs(
                                vm = vm,
                                state = state,
                            )
                        }
                    },
                )

                FormPaddingSectionSection()

                FormButton(
                    title = state.goalsTitle,
                    isFirst = true,
                    isLast = false,
                    note = state.goalsNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            ActivityFormGoalsFs(
                                initGoalFormsData = state.goalFormsData,
                                onDone = { newGoalFormsData ->
                                    vm.setGoalFormsData(newGoalFormsData = newGoalFormsData)
                                },
                            )
                        }
                    },
                )

                FormButton(
                    title = state.timerHintsTitle,
                    isFirst = false,
                    isLast = true,
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

                val activityDb: ActivityDb? = state.activityDb
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
