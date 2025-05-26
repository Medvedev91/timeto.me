package me.timeto.app.ui.tasks.form

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.c
import me.timeto.app.ui.halfDpCeil
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.Screen
import me.timeto.app.ui.activities.ActivityPickerFs
import me.timeto.app.ui.checklists.ChecklistsPickerFs
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.padding.FormPaddingSectionSection
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.header.HeaderSecondaryButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.shortcuts.ShortcutsPickerFs
import me.timeto.app.ui.timer.TimerSheet
import me.timeto.shared.ui.tasks.form.TaskFormStrategy
import me.timeto.shared.ui.tasks.form.TaskFormVm

@Composable
fun TaskFormFs(
    strategy: TaskFormStrategy,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val focusRequester = remember { FocusRequester() }
    val isFocused = remember { mutableStateOf(true) }

    val (vm, state) = rememberVm {
        TaskFormVm(
            strategy = strategy,
        )
    }

    val textField = remember {
        val stateText = state.text
        mutableStateOf(TextFieldValue(stateText, TextRange(stateText.length)))
    }

    Screen(
        modifier = Modifier
            .imePadding(),
    ) {

        val scrollState = rememberScrollState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = null,
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            ),
            secondaryButtons = if (strategy is TaskFormStrategy.EditTask) listOf(
                HeaderSecondaryButton(
                    text = "Delete",
                    onClick = {
                        vm.delete(
                            taskDb = strategy.taskDb,
                            dialogsManager = navigationFs,
                            onSuccess = {
                                navigationLayer.close()
                            },
                        )
                    },
                ),
            ) else emptyList(),
        )

        VStack(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(
                    state = scrollState,
                    reverseScrolling = true,
                ),
        ) {

            FormPaddingTop()

            FormButton(
                title = state.activityTitle,
                isFirst = true,
                isLast = false,
                note = state.activityNote,
                noteColor = c.textSecondary,
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
                noteColor = c.textSecondary,
                withArrow = true,
                onClick = {
                    navigationFs.push {
                        TimerSheet(
                            title = state.timerTitle,
                            doneTitle = "Done",
                            initSeconds = state.timerSecondsPicker,
                            onDone = { newTimerSeconds ->
                                vm.setTimer(newTimerSeconds)
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
        }

        HStack(
            modifier = Modifier
                .background(c.fg)
                .padding(vertical = 4.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            ZStack(
                modifier = Modifier
                    .weight(1f),
            ) {

                val text: String = textField.value.text

                BasicTextField(
                    value = textField.value,
                    onValueChange = { newValue ->
                        textField.value = newValue
                        vm.setText(newValue.text)
                    },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            isFocused.value = it.isFocused
                        },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    cursorBrush = SolidColor(c.blue),
                    textStyle = LocalTextStyle.current.copy(
                        color = c.text,
                        fontSize = 16.sp,
                    ),
                    decorationBox = { innerTextField ->
                        ZStack(
                            modifier = Modifier
                                .fillMaxWidth()
                                .sizeIn(minHeight = 40.dp)
                                .padding(
                                    start = H_PADDING,
                                    end = H_PADDING + 16.dp, // For clear button
                                    // Top and bottom for multiline padding
                                    top = 8.dp + halfDpCeil,
                                    bottom = 8.dp,
                                ),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (text.isEmpty()) {
                                Text(
                                    text = state.textPlaceholder,
                                    style = LocalTextStyle.current.copy(
                                        color = c.text.copy(alpha = 0.3f),
                                        fontSize = 16.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }

            Text(
                text = state.doneText,
                modifier = Modifier
                    .padding(start = 10.dp, end = H_PADDING)
                    .clip(roundedShape)
                    .background(c.blue)
                    .clickable {
                        vm.save(
                            dialogsManager = navigationFs,
                            onSuccess = {
                                navigationLayer.close()
                            },
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                color = c.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }

    LaunchedEffect(Unit) {
        delay(100) // Otherwise does not work for dialogs
        focusRequester.requestFocus()
    }
}
