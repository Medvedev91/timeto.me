package me.timeto.app.ui.shortcuts

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.FormPlainButtonSelection
import me.timeto.app.ui.form.FormPlainPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.ui.shortcuts.ShortcutsPickerVm

// todo context menu
// todo new shortcut button

@Composable
fun ShortcutsPickerFs(
    initShortcutsDb: List<ShortcutDb>,
    onDone: (List<ShortcutDb>) -> Unit,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ShortcutsPickerVm(
            initShortcutsDb = initShortcutsDb,
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
                    onDone(vm.getSelectedShortcutsDb())
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

            state.shortcutsDbSorted.forEachIndexed { idx, shortcutDb ->
                item(key = shortcutDb.id) {
                    FormPlainButtonSelection(
                        title = shortcutDb.name,
                        isSelected = shortcutDb.id in state.selectedIds,
                        isFirst = idx == 0,
                        modifier = Modifier
                            .animateItem(),
                        onClick = {
                            vm.toggleShortcut(shortcutDb)
                        },
                    )
                }
            }
        }
    }
}
