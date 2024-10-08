package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.vm.ShortcutsPickerSheetVm

@Composable
fun ShortcutsPickerSheet(
    layer: WrapperView.Layer,
    selectedShortcuts: List<ShortcutDb>,
    onPick: (List<ShortcutDb>) -> Unit,
) {

    val (vm, state) = rememberVm { ShortcutsPickerSheetVm(selectedShortcuts) }

    Column(
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
            onPick(vm.getSelectedShortcuts())
            layer.close()
        }

        Column(
            modifier = Modifier
                .verticalScroll(state = scrollState)
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
