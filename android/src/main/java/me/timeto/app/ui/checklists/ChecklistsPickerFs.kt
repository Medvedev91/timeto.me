package me.timeto.app.ui.checklists

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.ui.H_PADDING_HALF
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.checklists.form.ChecklistFormFs
import me.timeto.app.ui.checklists.form.ChecklistFormItemsFs
import me.timeto.app.ui.footer.Footer
import me.timeto.app.ui.footer.FooterAddButton
import me.timeto.app.ui.form.plain.FormPlainButtonSelection
import me.timeto.app.ui.form.plain.FormPlainPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.vm.checklists.ChecklistsPickerVm

// todo context menu

@Composable
fun ChecklistsPickerFs(
    initChecklistsDb: List<ChecklistDb>,
    onDone: (List<ChecklistDb>) -> Unit,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ChecklistsPickerVm(
            initChecklistsDb = initChecklistsDb,
        )
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = state.doneText,
                isEnabled = true,
                onClick = {
                    onDone(vm.getSelectedChecklistsDb())
                    navigationLayer.close()
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            ),
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f),
            state = scrollState,
        ) {

            item {
                FormPlainPaddingTop()
            }

            state.checklistsDbSorted.forEachIndexed { idx, checklistDb ->
                item(key = checklistDb.id) {
                    FormPlainButtonSelection(
                        title = checklistDb.name,
                        isSelected = checklistDb.id in state.selectedIds,
                        isFirst = idx == 0,
                        modifier = Modifier
                            .animateItem(),
                        onClick = {
                            vm.toggleChecklist(checklistDb)
                        },
                    )
                }
            }
        }

        Footer(
            scrollState = scrollState,
            contentModifier = Modifier
                .padding(horizontal = H_PADDING_HALF),
        ) {
            FooterAddButton(
                text = state.newChecklistText,
                onClick = {
                    navigationFs.push {
                        ChecklistFormFs(
                            checklistDb = null,
                            onSave = { newChecklistDb ->
                                vm.addSelectedId(id = newChecklistDb.id)
                                navigationFs.push {
                                    ChecklistFormItemsFs(
                                        checklistDb = newChecklistDb,
                                        onDelete = {},
                                    )
                                }
                            },
                            onDelete = {},
                        )
                    }
                },
            )
        }
    }
}
