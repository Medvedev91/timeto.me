package me.timeto.app.ui.checklists

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.H_PADDING_HALF
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.footer.Footer
import me.timeto.app.ui.footer.FooterAddButton
import me.timeto.app.ui.footer.FooterRightButton
import me.timeto.app.ui.form.FormSortedList
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.ui.checklists.ChecklistItemsFormVm

@Composable
fun ChecklistItemsFormFs(
    checklistDb: ChecklistDb,
    onDelete: () -> Unit,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ChecklistItemsFormVm(checklistDb = checklistDb)
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.checklistName,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = "Done",
                isEnabled = true,
                onClick = {
                    val isDoneAllowed = vm.isDoneAllowed(
                        dialogsManager = navigationFs,
                    )
                    if (isDoneAllowed) {
                        navigationLayer.close()
                    }
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Close",
                onClick = {
                    navigationLayer.close()
                },
            ),
        )

        FormSortedList(
            items = state.checklistItemsDb,
            itemId = { it.id },
            itemTitle = { it.text },
            onItemClick = { checklistItemDb ->
                navigationFs.push {
                    ChecklistItemFormFs(
                        checklistDb = checklistDb,
                        checklistItemDb = checklistItemDb,
                    )
                }
            },
            scrollState = scrollState,
            modifier = Modifier
                .weight(1f),
            onMove = { fromIdx, toIdx ->
                vm.moveAndroidLocal(fromIdx, toIdx)
            },
            onFinish = {
                vm.moveAndroidSync()
            },
            onItemDelete = { itemDb ->
                vm.deleteItemWithConfirmation(
                    itemDb = itemDb,
                    dialogsManager = navigationFs,
                )
            },
        )

        Footer(
            scrollState = scrollState,
            contentModifier = Modifier
                .padding(horizontal = H_PADDING_HALF),
        ) {
            FooterAddButton(
                text = state.newItemText,
                onClick = {
                    navigationFs.push {
                        ChecklistItemFormFs(
                            checklistDb = checklistDb,
                            checklistItemDb = null,
                        )
                    }
                },
            )
            SpacerW1()
            FooterRightButton(
                text = "Settings",
                onClick = {
                    navigationFs.push {
                        ChecklistFormFs(
                            checklistDb = checklistDb,
                            onSave = {},
                            onDelete = {
                                onDelete()
                                navigationLayer.close()
                            },
                        )
                    }
                },
            )
        }
    }
}
