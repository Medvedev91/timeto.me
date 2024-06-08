package me.timeto.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import me.timeto.app.c
import me.timeto.shared.TextFeatures
import me.timeto.shared.vm.ui.TextFeaturesTriggersFormUI

@Composable
fun TextFeaturesTriggersFormView(
    textFeatures: TextFeatures,
    bgColor: Color = c.sheetFg,
    onChange: (TextFeatures) -> Unit,
) {

    val formUI = remember(textFeatures) { TextFeaturesTriggersFormUI(textFeatures) }

    Column {

        MyListView__ItemView(
            isFirst = true,
            isLast = false,
            bgColor = bgColor,
        ) {
            MyListView__Item__Button(
                text = formUI.checklistsTitle,
                rightView = {
                    MyListView__Item__Button__RightText(
                        text = formUI.checklistsNote,
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
            bgColor = bgColor,
            withTopDivider = true,
        ) {
            MyListView__Item__Button(
                text = formUI.shortcutsTitle,
                rightView = {
                    MyListView__Item__Button__RightText(
                        text = formUI.shortcutsNote,
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
