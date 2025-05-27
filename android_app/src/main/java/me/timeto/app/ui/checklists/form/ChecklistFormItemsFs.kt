package me.timeto.app.ui.checklists.form

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.ui.H_PADDING_HALF
import me.timeto.app.ui.c
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.footer.Footer
import me.timeto.app.ui.footer.FooterAddButton
import me.timeto.app.ui.footer.FooterPlainButton
import me.timeto.app.ui.form.sorted.FormSortedList
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.ui.checklists.form.ChecklistFormItemsVm

@Composable
fun ChecklistFormItemsFs(
    checklistDb: ChecklistDb,
    onDelete: () -> Unit,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ChecklistFormItemsVm(checklistDb = checklistDb)
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

        fun openChecklistFormItemFs(checklistItemDb: ChecklistItemDb) {
            navigationFs.push {
                ChecklistFormItemFs(
                    checklistDb = checklistDb,
                    checklistItemDb = checklistItemDb,
                )
            }
        }

        FormSortedList(
            items = state.checklistItemsDb,
            itemId = { it.id },
            itemTitle = { it.text },
            onItemClick = { checklistItemDb ->
                openChecklistFormItemFs(checklistItemDb)
            },
            onItemLongClick = { checklistItemDb ->
                openChecklistFormItemFs(checklistItemDb)
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
                        ChecklistFormItemFs(
                            checklistDb = checklistDb,
                            checklistItemDb = null,
                        )
                    }
                },
            )
            SpacerW1()
            FooterPlainButton(
                text = "Settings",
                color = c.blue,
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
