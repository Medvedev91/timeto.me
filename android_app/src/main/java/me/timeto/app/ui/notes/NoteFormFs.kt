package me.timeto.app.ui.notes

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.input.ImeAction
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.FormPaddingFirstItem
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
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        NoteFormVm(
            noteDb = noteDb,
        )
    }

    Screen {

        Header(
            title = state.title,
            scrollState = null,
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
}
