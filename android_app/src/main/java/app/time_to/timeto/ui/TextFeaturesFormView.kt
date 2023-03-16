package app.time_to.timeto.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import app.time_to.timeto.rememberVM
import app.time_to.timeto.toColor
import timeto.shared.TextFeatures
import timeto.shared.vm.TextFeaturesFormVM

@Composable
fun TextFeaturesFormView(
    textFeatures: TextFeatures,
    onChange: (TextFeatures) -> Unit,
) {

    val (vm, state) = rememberVM(textFeatures) { TextFeaturesFormVM(textFeatures) }

    Column {
        TriggersFormView(vm, state, onChange)
    }
}

@Composable
private fun TriggersFormView(
    vm: TextFeaturesFormVM,
    state: TextFeaturesFormVM.State,
    onChange: (TextFeatures) -> Unit,
) {

    MyListView__ItemView(
        isFirst = true,
        isLast = false,
    ) {
        MyListView__ItemView__ButtonView(
            text = state.titleChecklist,
            withArrow = true,
            rightView = {
                MyListView__ItemView__ButtonView__RightText(
                    text = state.noteChecklists,
                    paddingEnd = 2.dp,
                )
            }
        ) {
            Sheet.show { layer ->
                ChecklistsPickerSheet(
                    layer = layer,
                    selectedChecklists = state.textFeatures.checklists,
                ) {
                    onChange(vm.upChecklists(it))
                }
            }
        }
    }

    MyListView__ItemView(
        isFirst = false,
        isLast = true,
        withTopDivider = true,
    ) {
        MyListView__ItemView__ButtonView(
            text = state.titleShortcuts,
            withArrow = true,
            rightView = {
                MyListView__ItemView__ButtonView__RightText(
                    text = state.noteShortcuts,
                    paddingEnd = 2.dp,
                )
            }
        ) {
            Sheet.show { layer ->
                ShortcutsPickerSheet(
                    layer = layer,
                    selectedShortcuts = state.textFeatures.shortcuts,
                ) {
                    onChange(vm.upShortcuts(it))
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TimerFormView(
    vm: TextFeaturesFormVM,
    state: TextFeaturesFormVM.State,
    onChange: (TextFeatures) -> Unit,
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    MyListView__ItemView(
        isFirst = true,
        isLast = false,
    ) {
        MyListView__ItemView__ButtonView(
            text = state.activityTitle,
            withArrow = true,
            rightView = {
                MyListView__ItemView__ButtonView__RightText(
                    text = state.activityNote,
                    paddingEnd = 2.dp,
                    color = state.activityColorOrNull?.toColor(),
                )
            }
        ) {
            Sheet.show { layer ->
                ActivityPickerSheet(
                    layer = layer,
                ) {
                    onChange(vm.upActivity(it))
                }
            }
        }
    }

    MyListView__ItemView(
        isFirst = false,
        isLast = true,
        withTopDivider = true,
    ) {
        MyListView__ItemView__ButtonView(
            text = state.timerTitle,
            withArrow = true,
            rightView = {
                MyListView__ItemView__ButtonView__RightText(
                    text = state.timerNote,
                    paddingEnd = 2.dp,
                    color = state.timerColorOrNull?.toColor(),
                )
            }
        ) {
            keyboardController?.hide()
            Sheet.show { layer ->
                TimerPickerSheet(
                    layer = layer,
                    title = "Timer",
                    doneText = "Done",
                    defMinutes = 30,
                    onPick = { seconds ->
                        onChange(vm.upTimer(seconds))
                    }
                )
            }
        }
    }
}
