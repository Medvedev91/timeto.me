package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import app.time_to.timeto.rememberVM
import timeto.shared.db.TaskModel
import timeto.shared.vm.TaskFormSheetVM

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TaskFormSheet(
    task: TaskModel?,
    layer: WrapperView.Layer,
) {

    val (vm, state) = rememberVM(task) { TaskFormSheetVM(task) }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .background(c.bgFormSheet)
            .navigationBarsPadding()
            .imePadding(),
    ) {

        val scrollState = rememberScrollState()

        Sheet.HeaderView(
            onCancel = { layer.close() },
            title = state.headerTitle,
            doneText = state.headerDoneText,
            isDoneEnabled = state.isHeaderDoneEnabled,
            scrollToHeader = scrollState.value,
        ) {
            vm.save {
                layer.close()
            }
        }

        Column(
            modifier = Modifier
                .verticalScroll(
                    state = scrollState
                )
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
            ) {

                MyListView__ItemView__TextInputView(
                    placeholder = "Task",
                    text = state.inputTextValue,
                    onTextChanged = { vm.setInputTextValue(it) },
                    isAutofocus = true,
                    keyboardButton = ImeAction.Done,
                    keyboardEvent = { keyboardController?.hide() },
                )
            }

            MyListView__Padding__SectionSection()

            TextFeaturesTimerFormView(state.textFeatures) {
                vm.setTextFeatures(it)
            }

            MyListView__Padding__SectionSection()

            TextFeaturesTriggersFormView(state.textFeatures) {
                vm.setTextFeatures(it)
            }
        }
    }
}
