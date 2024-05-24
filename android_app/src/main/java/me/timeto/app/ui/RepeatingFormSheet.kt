package me.timeto.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.vm.RepeatingFormSheetVM

@Composable
fun RepeatingFormSheet(
    layer: WrapperView.Layer,
    editedRepeating: RepeatingDb?,
) {

    val (vm, state) = rememberVM(editedRepeating) {
        RepeatingFormSheetVM(editedRepeating)
    }

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.bg)
    ) {

        val scrollState = rememberScrollState()

        Fs__HeaderAction(
            title = state.headerTitle,
            actionText = state.headerDoneText,
            onCancel = { layer.close() },
            scrollState = scrollState,
        ) {
            vm.save {
                layer.close()
            }
        }

        VStack(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {

            MyListView__PaddingFirst()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
                bgColor = c.fg,
            ) {
                MyListView__ItemView__TextInputView(
                    placeholder = "Task",
                    text = state.inputTextValue,
                    onTextChanged = { vm.setTextValue(it) }
                )
            }

            MyListView__Padding__SectionSection()

            MyListView__ItemView(
                isFirst = true,
                isLast = false,
            ) {
                MyListView__ItemView__ButtonView(
                    text = state.periodTitle,
                    withArrow = true,
                    bgColor = c.fg,
                    rightView = {
                        MyListView__ItemView__ButtonView__RightText(
                            text = state.periodNote,
                            paddingEnd = 2.dp,
                            color = state.periodNoteColor?.toColor(),
                        )
                    }
                ) {
                    Fs.show { layer ->
                        RepeatingFormPeriodFs(
                            layer = layer,
                            defaultPeriod = state.period,
                            onPick = { period ->
                                vm.setPeriod(period)
                            },
                        )
                    }
                }
            }

            MyListView__ItemView(
                isFirst = false,
                isLast = true,
                withTopDivider = true,
                dividerColor = c.dividerFg,
            ) {
                MyListView__ItemView__ButtonView(
                    text = state.daytimeHeader,
                    withArrow = true,
                    bgColor = c.fg,
                    rightView = {
                        MyListView__ItemView__ButtonView__RightText(
                            text = state.daytimeNote,
                            paddingEnd = 2.dp,
                        )
                    }
                ) {
                    Sheet.show { layer ->
                        DaytimePickerSheet(
                            layer = layer,
                            title = state.daytimeHeader,
                            doneText = "Done",
                            daytimeModel = state.defDaytimeModel,
                            withRemove = true,
                            onPick = { daytimePickerUi ->
                                vm.upDaytime(daytimePickerUi)
                            },
                            onRemove = {
                                vm.upDaytime(null)
                            },
                        )
                    }
                }
            }

            MyListView__Padding__SectionSection()

            TextFeaturesTimerFormView(
                textFeatures = state.textFeatures,
                bgColor = c.fg,
                dividerColor = c.dividerFg,
            ) {
                vm.upTextFeatures(it)
            }

            val isMoreSettingsVisible = remember { mutableStateOf(false) }

            AnimatedVisibility(
                visible = !isMoreSettingsVisible.value,
            ) {

                Text(
                    text = state.moreSettingText,
                    modifier = Modifier
                        .padding(horizontal = H_PADDING_HALF)
                        .padding(top = 19.dp)
                        .clip(squircleShape)
                        .clickable {
                            isMoreSettingsVisible.value = !isMoreSettingsVisible.value
                        }
                        .padding(
                            horizontal = H_PADDING_HALF,
                            vertical = 4.dp,
                        ),
                    color = c.blue,
                    fontSize = 14.sp,
                )
            }

            AnimatedVisibility(
                visible = isMoreSettingsVisible.value,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {

                VStack {

                    MyListView__Padding__SectionSection()

                    TextFeaturesTriggersFormView(
                        textFeatures = state.textFeatures,
                        bgColor = c.fg,
                        dividerColor = c.dividerFg,
                    ) {
                        vm.upTextFeatures(it)
                    }

                    MyListView__Padding__SectionSection()

                    MyListView__ItemView(
                        isFirst = true,
                        isLast = true,
                    ) {
                        MyListView__ItemView__SwitchView(
                            text = state.isImportantHeader,
                            isActive = state.isImportant,
                            bgColor = c.fg,
                        ) {
                            vm.toggleIsImportant()
                        }
                    }
                }
            }
        }
    }
}
