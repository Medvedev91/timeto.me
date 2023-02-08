package app.time_to.timeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.time_to.timeto.rememberVM
import app.time_to.timeto.setFalse
import timeto.shared.db.TaskFolderModel
import timeto.shared.vm.FolderFormSheetVM

@Composable
fun FolderFormSheet(
    isPresented: MutableState<Boolean>,
    folder: TaskFolderModel?,
) {
    TimetoSheet(isPresented = isPresented) {

        val (vm, state) = rememberVM(folder) { FolderFormSheetVM(folder) }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(c.bgFormSheet)
        ) {

            val scrollState = rememberScrollState()

            SheetHeaderView(
                onCancel = { isPresented.value = false },
                title = state.headerTitle,
                doneText = state.headerDoneText,
                isDoneEnabled = state.isHeaderDoneEnabled,
                scrollToHeader = scrollState.value,
            ) {
                vm.save {
                    isPresented.value = false
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
                                isPresented.setFalse()
                            }
                        }
                    }
                }
            }
        }
    }
}
