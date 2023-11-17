package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import me.timeto.app.*
import me.timeto.shared.db.EventTemplateDB
import me.timeto.shared.vm.EventTemplateFormSheetVM

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EventTemplateFormSheet(
    layer: WrapperView.Layer,
    eventTemplate: EventTemplateDB?,
) {

    val (vm, state) = rememberVM(eventTemplate) { EventTemplateFormSheetVM(eventTemplate) }
    val keyboardController = LocalSoftwareKeyboardController.current

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg)
            .imePadding(),
    ) {

        val scrollState = rememberScrollState()

        Sheet__HeaderView(
            title = state.headerTitle,
            scrollState = scrollState,
            bgColor = c.sheetBg,
        )

        Column(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .padding(bottom = 20.dp)
                .weight(1f)
        ) {

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
                modifier = Modifier
                    .padding(top = 12.dp)
            ) {

                MyListView__ItemView__TextInputView(
                    placeholder = "Text",
                    text = state.inputTextValue,
                    onTextChanged = { vm.setInputTextValue(it) },
                )
            }

            MyListView__Padding__SectionSection()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
            ) {
                MyListView__ItemView__ButtonView(
                    text = state.daytimeTitle,
                    withArrow = true,
                    rightView = {
                        MyListView__ItemView__ButtonView__RightText(
                            text = state.daytimeNote,
                            paddingEnd = 2.dp,
                            color = state.daytimeNoteColor?.toColor(),
                        )
                    }
                ) {
                    keyboardController?.hide()
                    Sheet.show { layer ->
                        DaytimePickerSheet(
                            layer = layer,
                            title = state.daytimeTitle,
                            doneText = "Done",
                            defHour = state.daytimeDefHour,
                            defMinute = state.daytimeDefMinute,
                            onPick = { secondsOrNull ->
                                vm.setDaytime(secondsOrNull)
                            }
                        )
                    }
                }
            }

            MyListView__Padding__SectionSection()

            TextFeaturesTimerFormView(state.textFeatures) {
                vm.setTextFeatures(it)
            }

            MyListView__Padding__SectionSection()

            TextFeaturesTriggersFormView(state.textFeatures) {
                vm.setTextFeatures(it)
            }

            if (eventTemplate != null) {

                MyListView__Padding__SectionSection()

                MyListView__ItemView(
                    isFirst = true,
                    isLast = true,
                ) {
                    MyListView__ItemView__ActionView(
                        text = state.deleteText,
                    ) {
                        vm.delete(eventTemplate) {
                            layer.close()
                        }
                    }
                }
            }
        }

        Sheet__BottomViewDefault(
            primaryText = state.doneText,
            primaryAction = {
                vm.save {
                    layer.close()
                }
            },
            secondaryText = "Cancel",
            secondaryAction = {
                layer.close()
            },
        )
    }
}
