package me.timeto.app.ui.notes

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.ImeAction
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.FormButton
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.FormPaddingFirstItem
import me.timeto.app.ui.form.FormPaddingSectionSection
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.NoteDb
import me.timeto.shared.ui.notes.NoteFormVm

@Composable
fun NoteFormFs(
    noteDb: NoteDb?,
    onDelete: () -> Unit,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        NoteFormVm(
            noteDb = noteDb,
        )
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = state.saveText,
                isEnabled = state.isSaveEnabled,
                onClick = {
                    vm.save(
                        dialogsManager = navigationFs,
                        onSuccess = {
                            navigationLayer.close()
                        },
                    )
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            )
        )

        FormPaddingFirstItem()

        LazyColumn(
            state = scrollState,
        ) {

            item {
                FormInput(
                    initText = state.text,
                    placeholder = state.textPlaceholder,
                    onChange = { newText ->
                        vm.setText(newText)
                    },
                    isFirst = true,
                    isLast = true,
                    isAutoFocus = true,
                    imeAction = ImeAction.Done,
                )
            }

            if (noteDb != null) {
                item {
                    FormPaddingSectionSection()
                    FormButton(
                        title = "Delete Note",
                        titleColor = c.red,
                        isFirst = true,
                        isLast = true,
                        onClick = {
                            vm.delete(
                                noteDb = noteDb,
                                dialogsManager = navigationFs,
                                onDelete = {
                                    onDelete()
                                    navigationLayer.close()
                                },
                            )
                        },
                    )
                }
            }
        }
    }
}
