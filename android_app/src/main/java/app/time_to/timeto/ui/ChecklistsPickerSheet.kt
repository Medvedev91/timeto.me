package app.time_to.timeto.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.time_to.timeto.R
import app.time_to.timeto.rememberVM
import timeto.shared.db.ChecklistModel
import timeto.shared.vm.ChecklistsPickerSheetVM

@Composable
fun ChecklistsPickerSheet(
    layer: WrapperView.Layer,
    selectedChecklists: List<ChecklistModel>,
    onPick: (List<ChecklistModel>) -> Unit,
) {

    val (vm, state) = rememberVM { ChecklistsPickerSheetVM(selectedChecklists) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.bgFormSheet)
    ) {

        val scrollState = rememberScrollState()

        Sheet.HeaderView(
            onCancel = { layer.close() },
            title = state.headerTitle,
            doneText = state.doneTitle,
            isDoneEnabled = true,
            scrollToHeader = scrollState.value,
        ) {
            onPick(vm.getSelectedChecklists())
            layer.close()
        }

        Column(
            modifier = Modifier
                .verticalScroll(
                    state = scrollState
                )
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {

            Row(Modifier.height(20.dp)) { }

            val checklistsUI = state.checklistsUI
            checklistsUI.forEach { checklistUI ->
                val isFirst = state.checklistsUI.first() == checklistUI
                MyListView__ItemView(
                    isFirst = isFirst,
                    isLast = state.checklistsUI.last() == checklistUI,
                    withTopDivider = !isFirst,
                ) {
                    MyListView__ItemView__ButtonView(
                        text = checklistUI.text,
                        rightView = {
                            AnimatedVisibility(
                                visible = checklistUI.isSelected,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.sf_checkmark_medium_medium),
                                    "Checkmark",
                                    tint = c.blue,
                                    modifier = Modifier
                                        .padding(end = MyListView.PADDING_INNER_HORIZONTAL)
                                        .size(20.dp)
                                        .padding(2.dp)
                                )
                            }
                        }
                    ) {
                        vm.toggleChecklist(checklistUI)
                    }
                }
            }
        }
    }
}
