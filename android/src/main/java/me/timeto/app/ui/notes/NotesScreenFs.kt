package me.timeto.app.ui.notes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.H_PADDING_HALF
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.footer.Footer
import me.timeto.app.ui.footer.FooterAddButton
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.header.HeaderSecondaryButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.note_folder.NoteFolderFormFs
import me.timeto.app.ui.rememberVm
import me.timeto.shared.db.NoteFolderDb
import me.timeto.shared.vm.notes.NoteFormLogic
import me.timeto.shared.vm.notes.NotesScreenVm

@Composable
fun NotesScreenFs(
    noteFolderDb: NoteFolderDb,
) {
    val (_, state) = rememberVm {
        NotesScreenVm(noteFolderDb)
    }

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = null,
            cancelButton = HeaderCancelButton(
                text = "Back",
                onClick = {
                    navigationLayer.close()
                },
            ),
            secondaryButtons = listOf(
                HeaderSecondaryButton(
                    text = "Settings",
                    onClick = {
                        navigationFs.push {
                            NoteFolderFormFs(
                                noteFolderDb = noteFolderDb,
                                onDelete = {
                                    navigationLayer.close()
                                },
                            )
                        }
                    },
                )
            ),
        )

        NotesView(
            noteFolderDb = noteFolderDb,
            hPadding = H_PADDING,
            scrollState = scrollState,
            withDivider = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        Footer(
            scrollState = scrollState,
            contentModifier = Modifier
                .padding(horizontal = H_PADDING_HALF),
        ) {

            FooterAddButton(
                text = "New Note",
                onClick = {
                    navigationFs.push {
                        NoteFormFs(
                            noteFormLogic = NoteFormLogic.NewNote(
                                noteFolderDb = noteFolderDb,
                            ),
                            onDelete = {},
                        )
                    }
                },
            )

            SpacerW1()
        }
    }
}
