package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.time_to.timeto.rememberVM
import timeto.shared.db.ShortcutModel
import timeto.shared.vm.ShortcutsPickerSheetVM

@Composable
fun ShortcutsPickerSheet(
    layer: WrapperView.Layer,
    selectedShortcuts: List<ShortcutModel>,
    onPick: (List<ShortcutModel>) -> Unit,
) {

    val (vm, state) = rememberVM { ShortcutsPickerSheetVM(selectedShortcuts) }

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
            onPick(vm.getSelectedShortcuts())
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

            val shortcutsUI = state.shortcutsUI
            shortcutsUI.forEach { shortcutUI ->
                val isFirst = state.shortcutsUI.first() == shortcutUI
                MyListView__ItemView(
                    isFirst = isFirst,
                    isLast = state.shortcutsUI.last() == shortcutUI,
                    withTopDivider = !isFirst,
                ) {
                    MyListView__ItemView__CheckboxView(
                        text = shortcutUI.text,
                        isChecked = shortcutUI.isSelected,
                    ) {
                        vm.toggleShortcut(shortcutUI)
                    }
                }
            }
        }
    }
}
