package me.timeto.app.ui.notes

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import me.timeto.app.ui.c
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.form.padding.FormPaddingBottom
import me.timeto.app.ui.form.padding.FormPaddingSectionSection
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.NoteDb
import me.timeto.shared.vm.notes.NoteFormVm

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

    Screen(
        modifier = Modifier
            .imePadding(),
    ) {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = state.doneText,
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

        LazyColumn(
            state = scrollState,
        ) {

            item {

                FormPaddingTop()

                FormInput(
                    initText = state.text,
                    placeholder = state.textPlaceholder,
                    onChange = { newText ->
                        vm.setText(newText)
                    },
                    isFirst = true,
                    isLast = true,
                    isAutoFocus = true,
                    imeAction = ImeAction.None,
                )

                if (noteDb != null) {
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

                FormPaddingBottom(
                    withNavigation = true,
                )
            }
        }
    }
}
