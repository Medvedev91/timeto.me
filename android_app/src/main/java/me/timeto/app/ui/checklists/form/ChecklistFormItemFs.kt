package me.timeto.app.ui.checklists.form

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.checklists.ChecklistsPickerFs
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.padding.FormPaddingSectionSection
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.shortcuts.ShortcutsPickerFs
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.vm.checklists.form.ChecklistFormItemVm

@Composable
fun ChecklistFormItemFs(
    checklistDb: ChecklistDb,
    checklistItemDb: ChecklistItemDb?,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ChecklistFormItemVm(
            checklistDb = checklistDb,
            checklistItemDb = checklistItemDb,
        )
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = state.saveButtonText,
                isEnabled = state.isSaveEnabled,
                onClick = {
                    vm.save(
                        dialogsManager = navigationFs,
                        onSuccess = {
                            navigationLayer.close()
                        },
                    )
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
                .fillMaxSize(),
            state = scrollState,
            contentPadding = PaddingValues(bottom = 25.dp),
        ) {

            item {
                FormPaddingTop()
            }

            item {
                FormInput(
                    initText = state.text,
                    placeholder = "",
                    onChange = { newText ->
                        vm.setText(newText)
                    },
                    isFirst = true,
                    isLast = true,
                    isAutoFocus = true,
                    imeAction = ImeAction.Done,
                )
            }

            item {
                FormPaddingSectionSection()
            }

            item {
                FormButton(
                    title = "Checklists",
                    isFirst = true,
                    isLast = false,
                    note = state.checklistsNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            ChecklistsPickerFs(
                                initChecklistsDb = state.nestedChecklistsDb,
                                onDone = { newChecklistsDb ->
                                    vm.setNestedChecklists(newChecklistsDb)
                                },
                            )
                        }
                    },
                )
            }

            item {
                FormButton(
                    title = "Shortcuts",
                    isFirst = false,
                    isLast = true,
                    note = state.shortcutsNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            ShortcutsPickerFs(
                                initShortcutsDb = state.nestedShortcutsDb,
                                onDone = { newShortcutsDb ->
                                    vm.setNestedShortcuts(newShortcutsDb)
                                }
                            )
                        }
                    },
                )
            }
        }
    }
}
