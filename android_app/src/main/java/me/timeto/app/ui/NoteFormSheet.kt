package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.H_PADDING
import me.timeto.app.VStack
import me.timeto.app.c
import me.timeto.app.rememberVM
import me.timeto.shared.db.NoteModel
import me.timeto.shared.vm.NoteFormSheetVM

@Composable
fun NoteFormSheet(
    layer: WrapperView.Layer,
    note: NoteModel?,
) {

    val (vm, state) = rememberVM(note) { NoteFormSheetVM(note) }

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg)
            .imePadding(),
    ) {

        Sheet__HeaderView(
            title = state.headerTitle,
            scrollState = null,
            bgColor = c.sheetBg,
        )

        Column(
            modifier = Modifier
                .padding(bottom = H_PADDING)
                .weight(1f)
        ) {

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
            ) {
                MyListView__ItemView__TextInputView(
                    placeholder = state.inputTextPlaceholder,
                    text = state.inputTextValue,
                    onTextChanged = { newText -> vm.setInputText(newText) },
                )
            }
        }

        Sheet__BottomViewDefault(
            primaryText = state.doneTitle,
            primaryAction = {
                vm.save {
                    layer.close()
                }
            },
            secondaryText = "Cancel",
            secondaryAction = {
                layer.close()
            },
        )
    }
}
