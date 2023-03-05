package app.time_to.timeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
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
    task: TaskModel,
    layer: WrapperView.Layer,
) {

    val (vm, state) = rememberVM(task) { TaskFormSheetVM(task) }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .background(c.bgFormSheet)
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = 24.dp),
    ) {

        Sheet.HeaderView(
            onCancel = { layer.close() },
            title = state.headerTitle,
            doneText = state.headerDoneText,
            isDoneEnabled = state.isHeaderDoneEnabled,
            scrollToHeader = 0,
        ) {
            vm.save {
                layer.close()
            }
        }

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

        TriggersView__FormView(
            triggers = state.textFeatures.triggers,
            onTriggersChanged = { vm.setTriggers(it) },
            modifier = Modifier.padding(top = 18.dp),
            contentPaddingHints = PaddingValues(horizontal = MyListView.PADDING_OUTER_HORIZONTAL),
            defBg = if (MaterialTheme.colors.isLight) c.white else c.bgFormSheet,
        )
    }
}
