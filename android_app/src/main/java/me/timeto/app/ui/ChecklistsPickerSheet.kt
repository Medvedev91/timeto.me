package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.timeto.app.*
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.vm.Checklist.ChecklistPickerVm

@Composable
fun ChecklistsPickerSheet(
    layer: WrapperView.Layer,
    selectedChecklists: List<ChecklistDb>,
    onPick: (List<ChecklistDb>) -> Unit,
) {

    val (vm, state) = rememberVm { ChecklistPickerVm(selectedChecklists) }

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg)
    ) {

        val scrollState = rememberScrollState()

        Sheet.HeaderViewOld(
            onCancel = { layer.close() },
            title = state.headerTitle,
            doneText = state.doneTitle,
            isDoneEnabled = true,
            scrollState = scrollState,
        ) {
            onPick(vm.getSelectedChecklists())
            layer.close()
        }

        VStack(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {

            ZStack(Modifier.height(20.dp)) { }

            val checklistsUI = state.checklistsUI
            checklistsUI.forEach { checklistUI ->
                val isFirst = state.checklistsUI.first() == checklistUI
                MyListView__ItemView(
                    isFirst = isFirst,
                    isLast = state.checklistsUI.last() == checklistUI,
                    withTopDivider = !isFirst,
                ) {
                    MyListView__ItemView__RadioView(
                        text = checklistUI.text,
                        isActive = checklistUI.isSelected,
                    ) {
                        vm.toggleChecklist(checklistUI)
                    }
                }
            }

            Text(
                text = state.newChecklistButton,
                modifier = Modifier
                    .padding(top = 12.dp, start = 14.dp)
                    .clip(squircleShape)
                    .clickable {
                        Dialog.show { layer ->
                            ChecklistNameDialog(
                                layer = layer,
                                editedChecklist = null,
                                onSave = { newChecklistDb ->
                                    vm.selectById(newChecklistDb.id)
                                    Sheet.show { layer ->
                                        ChecklistFormSheet(
                                            layer = layer,
                                            checklistDb = newChecklistDb,
                                            onDelete = {},
                                        )
                                    }
                                },
                            )
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = c.blue,
            )

            ZStack(Modifier.height(20.dp)) { }
        }
    }
}
