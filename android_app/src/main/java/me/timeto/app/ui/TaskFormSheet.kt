package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.shared.db.TaskDb
import me.timeto.shared.vm.TaskFormSheetVm

@Composable
fun TaskFormSheet(
    task: TaskDb?,
    layer: WrapperView.Layer,
) {

    val (vm, state) = rememberVm(task) { TaskFormSheetVm(task) }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .background(c.sheetBg)
            .navigationBarsPadding()
            .imePadding(),
    ) {

        val scrollState = rememberScrollState()

        Sheet.HeaderViewOld(
            onCancel = { layer.close() },
            title = state.headerTitle,
            doneText = state.headerDoneText,
            isDoneEnabled = state.isHeaderDoneEnabled,
            scrollState = scrollState,
        ) {
            vm.save {
                layer.close()
            }
        }

        Column(
            modifier = Modifier
                .verticalScroll(state = scrollState)
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
