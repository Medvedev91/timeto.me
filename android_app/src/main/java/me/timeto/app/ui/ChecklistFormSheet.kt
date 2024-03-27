package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.vm.ChecklistFormSheetVM

@Composable
fun ChecklistFormSheet(
    layer: WrapperView.Layer,
    checklistDb: ChecklistDb,
    onDelete: () -> Unit,
) {

    val (vm, state) = rememberVM(checklistDb) {
        ChecklistFormSheetVM(checklistDb)
    }

    VStack(
        modifier = Modifier
            .fillMaxSize()
            .background(c.sheetBg),
    ) {

        HStack(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 4.dp)
                .padding(horizontal = H_PADDING_HALF),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            HStack(
                modifier = Modifier
                    .weight(1f)
                    .clip(squircleShape)
                    .clickable {
                        Dialog.show { editNameLayer ->
                            ChecklistEditDialog(
                                layer = editNameLayer,
                                editedChecklist = state.checklistDb,
                                onSave = {},
                            )
                        }
                    }
                    .padding(horizontal = H_PADDING_HALF, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Text(
                    text = state.checklistName,
                    modifier = Modifier
                        .weight(1f),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = c.white,
                )

                Icon(
                    painterResource(R.drawable.sf_pencil_medium_medium),
                    contentDescription = "Edit Name",
                    tint = c.white,
                    modifier = Modifier
                        .offset(y = 1.dp)
                        .padding(start = 16.dp)
                        .size(18.dp),
                )
            }

            Icon(
                painterResource(R.drawable.sf_trash_medium_regular),
                contentDescription = "Delete Checklist",
                tint = c.red,
                modifier = Modifier
                    .offset(y = 1.dp)
                    .padding(start = 12.dp, end = H_PADDING_HALF)
                    .clip(roundedShape)
                    .size(36.dp)
                    .clickable {
                        vm.deleteChecklist(
                            onDelete = {
                                onDelete()
                                layer.close()
                            }
                        )
                    }
                    .padding(8.dp),
            )
        }

        DividerFg(Modifier.padding(horizontal = H_PADDING))

        LazyColumn(
            modifier = Modifier
                .weight(1f),
            contentPadding = PaddingValues(top = 16.dp),
        ) {

            state.checklistItemsUi.forEach { checklistItemUi ->

                item(
                    key = checklistItemUi.checklistItemDb.id,
                ) {

                    ZStack(
                        modifier = Modifier
                            .clickable {
                            },
                    ) {

                        HStack {

                            Text(
                                text = checklistItemUi.checklistItemDb.text,
                                modifier = Modifier
                                    .padding(vertical = 8.dp),
                                color = c.text,
                            )
                        }

                        if (!checklistItemUi.isFirst)
                            DividerFg(
                                modifier = Modifier
                                    .padding(start = H_PADDING)
                                    .align(Alignment.TopCenter),
                            )
                    }
                }
            }
        }

        Sheet__BottomViewDone("Done") {
            layer.close()
        }
    }
}
