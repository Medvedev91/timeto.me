package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.*
import me.timeto.shared.db.NoteDb
import me.timeto.shared.vm.NoteSheetVM

@Composable
fun NoteSheet(
    layer: WrapperView.Layer,
    initNote: NoteDb, // TRICK Can change, use VM!
) {

    val (_, state) = rememberVM(initNote) { NoteSheetVM(initNote) }

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                top = 12.dp,
                bottom = 12.dp,
            ),
        ) {
            item {
                Text(
                    text = state.note.text,
                    modifier = Modifier
                        .padding(horizontal = H_PADDING),
                    color = c.text,
                )
            }
        }

        Sheet__BottomView {
            HStack(
                modifier = Modifier
                    .padding(
                        top = 10.dp,
                        end = H_PADDING - 8.dp,
                        bottom = 10.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                SpacerW1()

                Sheet__BottomView__SecondaryButton("Edit") {
                    Sheet.show { layerForm ->
                        NoteFormSheet(
                            layer = layerForm,
                            note = state.note,
                            onDelete = { layer.close() },
                        )
                    }
                }

                Sheet__BottomView__SecondaryButton("Close") {
                    layer.close()
                }
            }
        }
    }
}
