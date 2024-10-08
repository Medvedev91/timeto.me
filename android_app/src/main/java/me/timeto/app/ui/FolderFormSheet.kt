package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.vm.FolderFormSheetVm

@Composable
fun FolderFormSheet(
    layer: WrapperView.Layer,
    folder: TaskFolderDb?,
) {
    val (vm, state) = rememberVm(folder) { FolderFormSheetVm(folder) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg)
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

            MyListView__Padding__SectionHeader()

            MyListView__HeaderView(
                state.inputNameHeader,
            )

            MyListView__Padding__HeaderSection()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
            ) {
                MyListView__ItemView__TextInputView(
                    placeholder = state.inputNamePlaceholder,
                    text = state.inputNameValue,
                    onTextChanged = { newText -> vm.setInputNameValue(newText) },
                )
            }

            if (folder != null && !folder.isToday) {

                MyListView__Padding__SectionSection()

                MyListView__ItemView(
                    isFirst = true,
                    isLast = true,
                ) {
                    MyListView__ItemView__ActionView(
                        text = state.deleteFolderText,
                    ) {
                        vm.delete {
                            layer.close()
                        }
                    }
                }
            }
        }
    }
}
