package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.timeto.app.*
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
            startContent = {
                val deleteFun  = state.deleteFun
                if (deleteFun != null) {
                    Icon(
                        Icons.Rounded.Delete,
                        "Delete Note",
                        tint = c.red,
                        modifier = Modifier
                            .padding(start = MyListView.PADDING_OUTER_HORIZONTAL - 0.dp)
                            .size(33.dp)
                            .clip(roundedShape)
                            .clickable {
                                deleteFun {
                                    layer.close()
                                }
                            }
                            .padding(4.dp)
                    )
                }
            }
        )
    }
}
