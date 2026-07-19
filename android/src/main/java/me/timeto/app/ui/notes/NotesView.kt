package me.timeto.app.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.Divider
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.rememberVm
import me.timeto.shared.db.NoteFolderDb
import me.timeto.shared.vm.notes.NotesVm

@Composable
fun NotesView(
    noteFolderDb: NoteFolderDb,
    hPadding: Dp,
    scrollState: LazyListState,
    withDivider: Boolean,
    modifier: Modifier,
) {

    val (_, state) = rememberVm {
        NotesVm(
            noteFolderDb = noteFolderDb,
        )
    }

    val navigationFs = LocalNavigationFs.current
    val notesUi = state.notesUi.reversed()

    LazyColumn(
        modifier = modifier,
        state = scrollState,
        reverseLayout = true,
    ) {
        notesUi.forEach { noteUi ->
            item {
                ZStack(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navigationFs.push {
                                NoteFs(
                                    initNoteDb = noteUi.noteDb,
                                )
                            }
                        }
                        .defaultMinSize(minHeight = HomeScreen__itemHeight),
                ) {
                    if (withDivider && (notesUi.last() != noteUi)) {
                        Divider(
                            modifier = Modifier
                                .padding(start = hPadding),
                        )
                    }

                    Text(
                        text = noteUi.text,
                        modifier = Modifier
                            .padding(horizontal = hPadding, vertical = 6.dp),
                        color = c.white,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
