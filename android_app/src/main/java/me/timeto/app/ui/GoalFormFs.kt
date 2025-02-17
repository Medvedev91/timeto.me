package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.models.GoalFormUi
import me.timeto.shared.vm.GoalFormVm

@Composable
fun GoalFormFs(
    _layer: WrapperView.Layer,
    _initGoalFormUi: GoalFormUi?,
    _onSelect: (GoalFormUi) -> Unit,
    onDelete: (() -> Unit)?,
) {

    val (vm, state) = rememberVm(_initGoalFormUi) {
        GoalFormVm(_initGoalFormUi)
    }

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.bg),
    ) {

        val scrollState = rememberScrollState()

        Fs__HeaderAction(
            title = state.headerTitle,
            actionText = state.headerDoneText,
            onCancel = { _layer.close() },
            scrollState = scrollState,
            onDone = {
                vm.buildFormUi { formUi ->
                    _onSelect(formUi)
                    _layer.close()
                }
            },
        )

        VStack(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
        ) {

            MyListView__PaddingFirst()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
                bgColor = c.fg,
            ) {
                MyListView__ItemView__TextInputView(
                    placeholder = state.notePlaceholder,
                    text = state.note,
                    onTextChanged = { newNote ->
                        vm.setNote(newNote)
                    },
                )
            }

            MyListView__Padding__SectionSection()

            MyListView__ItemView(
                isFirst = true,
                isLast = false,
                bgColor = c.fg,
            ) {
                MyListView__Item__Button(
                    text = state.periodTitle,
                    rightView = {
                        MyListView__Item__Button__RightText(
                            text = state.periodNote,
                            color = state.periodNoteColor?.toColor(),
                        )
                    },
                ) {
                    Fs.show { layer ->
                        GoalPeriodFormFs(
                            _layer = layer,
                            _initPeriod = state.period,
                            _onSelect = { newPeriod ->
                                vm.setPeriod(newPeriod)
                            }
                        )
                    }
                }
            }

            MyListView__ItemView(
                isFirst = false,
                isLast = true,
                bgColor = c.fg,
                withTopDivider = true,
            ) {
                MyListView__Item__Button(
                    text = state.durationTitle,
                    rightView = {
                        MyListView__Item__Button__RightText(
                            text = state.durationNote,
                        )
                    },
                ) {
                    Sheet.show { layer ->
                        TimerPickerSheet(
                            layer = layer,
                            title = state.durationPickerSheetTitle,
                            doneText = "Done",
                            defMinutes = state.durationDefMinutes,
                            onPick = { seconds ->
                                vm.setDuration(seconds)
                            }
                        )
                    }
                }
            }

            MyListView__Padding__SectionSection()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
                bgColor = c.fg,
            ) {
                MyListView__Item__Button(
                    text = state.timerTitle,
                    rightView = {
                        MyListView__Item__Button__RightText(
                            text = state.timerNote,
                            color = state.timerNoteColor?.toColor(),
                        )
                    },
                ) {
                    Sheet.show { layer ->
                        TimerPickerSheet(
                            layer = layer,
                            title = state.timerPickerSheetTitle,
                            doneText = "Done",
                            defMinutes = state.timerDefaultMinutes,
                            onPick = { seconds ->
                                vm.setTimer(seconds)
                            }
                        )
                    }
                }
            }

            MyListView__Padding__SectionSection()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
                bgColor = c.fg,
            ) {
                MyListView__Item__Button(
                    text = state.finishedTitle,
                    rightView = {
                        MyListView__Item__Button__RightText(
                            text = state.finishedText,
                            color = c.white,
                            fontSize = 22.sp,
                        )
                    }
                ) {
                    Sheet.show { layer ->
                        SearchEmojiSheet(layer = layer) {
                            vm.setFinishedText(it)
                        }
                    }
                }
            }

            MyListView__Padding__SectionSection()

            TextFeaturesTriggersFormView(
                textFeatures = state.textFeatures,
                bgColor = c.fg,
            ) {
                vm.setTextFeatures(it)
            }

            if (onDelete != null) {

                MyListView__Padding__SectionSection()

                MyListView__ItemView(
                    isFirst = true,
                    isLast = true,
                ) {
                    MyListView__ItemView__ActionView(
                        text = state.deleteGoalText,
                    ) {
                        vm.deleteConfirmation {
                            onDelete()
                            _layer.close()
                        }
                    }
                }
            }
        }
    }
}
