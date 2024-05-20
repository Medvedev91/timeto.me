package me.timeto.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.timeto.app.c
import me.timeto.shared.TextFeatures
import me.timeto.shared.vm.ui.TextFeaturesTriggersFormUI

@Composable
fun TextFeaturesTriggersFormView(
    textFeatures: TextFeatures,
    bgColor: Color = c.sheetFg,
    dividerColor: Color = c.sheetDividerFg,
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
                bgColor = bgColor,
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
            dividerColor = dividerColor,
        ) {
            MyListView__ItemView__ButtonView(
                text = formUI.shortcutsTitle,
                withArrow = true,
                bgColor = bgColor,
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
