package me.timeto.app.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
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
import me.timeto.shared.vm.ChecklistFormVm

private val deleteButtonSize = 28.dp
private val deleteButtonInnerPadding = 5.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChecklistFormSheet(
    layer: WrapperView.Layer,
    checklistDb: ChecklistDb,
    onDelete: () -> Unit,
) {

    val (vm, state) = rememberVm(checklistDb) {
        ChecklistFormVm(checklistDb)
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
                            ChecklistNameDialog(
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
                    .padding(start = 12.dp)
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
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {

            state.checklistItemsUi.forEach { checklistItemUi ->

                item(
                    key = checklistItemUi.checklistItemDb.id,
                ) {

                    ZStack(
                        modifier = Modifier
                            .animateItemPlacement()
                            .clickable {
                                Dialog.show { editItemLayer ->
                                    ChecklistItemEditDialog(
                                        layer = editItemLayer,
                                        checklist = state.checklistDb,
                                        editedChecklistItem = checklistItemUi.checklistItemDb,
                                    )
                                }
                            },
                    ) {

                        HStack(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {

                            Icon(
                                painterResource(id = R.drawable.sf_minus_circle_fill_medium_regular),
                                contentDescription = "Delete",
                                modifier = Modifier
                                    .padding(start = H_PADDING - deleteButtonInnerPadding)
                                    .size(deleteButtonSize)
                                    .clip(roundedShape)
                                    .clickable {
                                        vm.deleteItem(checklistItemUi.checklistItemDb)
                                    }
                                    .padding(deleteButtonInnerPadding),
                                tint = c.red,
                            )

                            Text(
                                text = checklistItemUi.checklistItemDb.text,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp)
                                    .padding(start = 8.dp),
                                color = c.text,
                            )

                            Icon(
                                painterResource(id = R.drawable.sf_pencil_medium_medium),
                                contentDescription = "Edit",
                                modifier = Modifier
                                    .size(15.dp),
                                tint = c.blue,
                            )

                            Icon(
                                Icons.Rounded.ArrowDownward,
                                "Down",
                                tint = c.blue,
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(24.dp)
                                    .clip(roundedShape)
                                    .clickable {
                                        vm.down(checklistItemUi)
                                    }
                                    .padding(1.dp)
                            )

                            Icon(
                                Icons.Rounded.ArrowUpward,
                                "Up",
                                tint = c.blue,
                                modifier = Modifier
                                    .padding(start = 4.dp, end = 8.dp)
                                    .size(24.dp)
                                    .clip(roundedShape)
                                    .clickable {
                                        vm.up(checklistItemUi)
                                    }
                                    .padding(1.dp)
                            )
                        }

                        if (!checklistItemUi.isFirst)
                            DividerFg(
                                modifier = Modifier
                                    .padding(start = H_PADDING + deleteButtonSize)
                                    .align(Alignment.TopCenter),
                            )
                    }
                }
            }

            item(
                key = "new_item"
            ) {
                Text(
                    text = state.newItemButton,
                    modifier = Modifier
                        .padding(top = 4.dp, start = 8.dp)
                        .clip(squircleShape)
                        .clickable {
                            Dialog.show { newItemLayer ->
                                ChecklistItemEditDialog(
                                    layer = newItemLayer,
                                    checklist = state.checklistDb,
                                    editedChecklistItem = null,
                                )
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = c.blue,
                )

                ZStack(Modifier.height(20.dp)) {}
            }
        }

        Sheet__BottomViewDone("Done") {
            if (vm.isDoneAllowed())
                layer.close()
        }
    }
}
