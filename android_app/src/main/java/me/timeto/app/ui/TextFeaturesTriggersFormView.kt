package me.timeto.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import me.timeto.shared.TextFeatures
import me.timeto.shared.vm.ui.TextFeaturesTriggersFormUI

@Composable
fun TextFeaturesTriggersFormView(
    textFeatures: TextFeatures,
    onChange: (TextFeatures) -> Unit,
) {

    val formUI = remember(textFeatures) { TextFeaturesTriggersFormUI(textFeatures) }

    Column {

        MyListView__ItemView(
            isFirst = true,
            isLast = false,
        ) {
            MyListView__ItemView__ButtonView(
                text = formUI.checklistsTitle,
                withArrow = true,
                rightView = {
                    MyListView__ItemView__ButtonView__RightText(
                        text = formUI.checklistsNote,
                        paddingEnd = 2.dp,
                    )
                }
            ) {
                Sheet.show { layer ->
                    ChecklistsPickerSheet(
                        layer = layer,
                        selectedChecklists = formUI.textFeatures.checklists,
                    ) {
                        onChange(formUI.setChecklists(it))
                    }
                }
            }
        }

        MyListView__ItemView(
            isFirst = false,
            isLast = true,
            withTopDivider = true,
        ) {
            MyListView__ItemView__ButtonView(
                text = formUI.shortcutsTitle,
                withArrow = true,
                rightView = {
                    MyListView__ItemView__ButtonView__RightText(
                        text = formUI.shortcutsNote,
                        paddingEnd = 2.dp,
                    )
                }
            ) {
                Sheet.show { layer ->
                    ShortcutsPickerSheet(
                        layer = layer,
                        selectedShortcuts = formUI.textFeatures.shortcuts,
                    ) {
                        onChange(formUI.setShortcuts(it))
                    }
                }
            }
        }
    }
}
