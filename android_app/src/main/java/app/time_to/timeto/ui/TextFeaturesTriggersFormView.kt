package app.time_to.timeto.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import app.time_to.timeto.rememberVM
import timeto.shared.TextFeatures
import timeto.shared.vm.TextFeaturesFormVM

@Composable
fun TextFeaturesTriggersFormView(
    textFeatures: TextFeatures,
    onChange: (TextFeatures) -> Unit,
) {

    val (vm, state) = rememberVM(textFeatures) { TextFeaturesFormVM(textFeatures) }

    Column {

        MyListView__ItemView(
            isFirst = true,
            isLast = false,
        ) {
            MyListView__ItemView__ButtonView(
                text = state.titleChecklist,
                withArrow = true,
                rightView = {
                    MyListView__ItemView__ButtonView__RightText(
                        text = state.noteChecklists,
                        paddingEnd = 2.dp,
                    )
                }
            ) {
                Sheet.show { layer ->
                    ChecklistsPickerSheet(
                        layer = layer,
                        selectedChecklists = textFeatures.checklists,
                    ) {
                        onChange(vm.upChecklists(it))
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
                text = state.titleShortcuts,
                withArrow = true,
                rightView = {
                    MyListView__ItemView__ButtonView__RightText(
                        text = state.noteShortcuts,
                        paddingEnd = 2.dp,
                    )
                }
            ) {
                Sheet.show { layer ->
                    ShortcutsPickerSheet(
                        layer = layer,
                        selectedShortcuts = textFeatures.shortcuts,
                    ) {
                        onChange(vm.upShortcuts(it))
                    }
                }
            }
        }
    }
}
