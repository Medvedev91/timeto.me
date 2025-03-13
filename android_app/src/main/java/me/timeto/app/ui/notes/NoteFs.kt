package me.timeto.app.ui.notes

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.H_PADDING
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.header.HeaderSecondaryButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.NoteDb
import me.timeto.shared.ui.notes.NoteVm

@Composable
fun NoteFs(
    initNoteDb: NoteDb,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (_, state) = rememberVm {
        NoteVm(
            noteDb = initNoteDb,
        )
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = "Note",
            scrollState = scrollState,
            actionButton = null,
            cancelButton = HeaderCancelButton(
                text = "Close",
                onClick = {
                    navigationLayer.close()
                },
            ),
            secondaryButtons = listOf(
                HeaderSecondaryButton(
                    text = "Edit",
                    onClick = {
                        navigationFs.push {
                            NoteFormFs(
                                noteDb = state.noteDb,
                                onDelete = {
                                    navigationLayer.close()
                                },
                            )
                        }
                    },
                )
            ),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = scrollState,
            contentPadding = PaddingValues(
                top = 12.dp,
                bottom = 12.dp,
            ),
        ) {
            item {
                Text(
                    text = state.noteDb.text,
                    modifier = Modifier
                        .padding(horizontal = H_PADDING),
                    color = c.text,
                )
            }
        }
    }
}
