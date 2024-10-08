package me.timeto.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.ActivityDb__Data.TimerHints.HINT_TYPE
import me.timeto.shared.vm.ActivityFormSheetVm

@Composable
fun ActivityFormSheet(
    layer: WrapperView.Layer,
    activity: ActivityDb?,
) {

    val (vm, state) = rememberVm(activity) {
        ActivityFormSheetVm(activity)
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg),
    ) {

        val scrollState = rememberScrollState()

        Sheet.HeaderViewOld(
            onCancel = { layer.close() },
            title = state.headerTitle,
            doneText = state.headerDoneText,
            isDoneEnabled = state.isHeaderDoneEnabled,
            scrollState = scrollState,
        ) {
            vm.save {
                layer.close()
            }
        }

        Column(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {

            ////

            MyListView__Padding__SectionHeader()

            MyListView__HeaderView(
                state.inputNameHeader,
            )

            MyListView__Padding__HeaderSection()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
            ) {
                MyListView__ItemView__TextInputView(
                    placeholder = state.inputNamePlaceholder,
                    text = state.inputNameValue,
                    onTextChanged = { newText -> vm.setInputNameValue(newText) },
                )
            }

            ////

            MyListView__Padding__SectionSection()

            TextFeaturesTriggersFormView(state.textFeatures) {
                vm.setTextFeatures(it)
            }

            MyListView__Padding__SectionSection()

            MyListView__ItemView(
                isFirst = true,
                isLast = false,
            ) {
                MyListView__ItemView__ButtonView(
                    text = state.emojiTitle,
                    withArrow = true,
                    rightView = {
                        val uniqueEmojiValue = state.emoji
                        if (uniqueEmojiValue != null) {
                            Text(
                                uniqueEmojiValue,
                                modifier = Modifier
                                    .padding(end = 4.dp),
                                fontSize = 27.sp
                            )
                        } else {
                            Text(
                                state.emojiNotSelected,
                                modifier = Modifier
                                    .offset(y = -(0.5.dp)),
                                color = c.red,
                                fontSize = 15.sp
                            )
                        }
                    }
                ) {
                    Sheet.show { layer ->
                        SearchEmojiSheet(layer = layer) {
                            vm.setEmoji(it)
                        }
                    }
                }
            }

            MyListView__ItemView(
                isFirst = false,
                isLast = false,
                withTopDivider = true,
            ) {
                MyListView__ItemView__ButtonView(
                    text = state.colorTitle,
                    rightView = {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(30.dp)
                                .clip(roundedShape)
                                .background(state.colorRgba.toColor())
                        )
                    }
                ) {
                    Sheet.show { layer ->
                        ActivityColorSheet(
                            layer = layer,
                            initData = vm.buildColorPickerInitData(),
                        ) {
                            vm.upColorRgba(it)
                        }
                    }
                }
            }

            MyListView__ItemView(
                isFirst = false,
                isLast = false,
                withTopDivider = true,
            ) {
                MyListView__ItemView__SwitchView(
                    text = state.keepScreenOnTitle,
                    isActive = state.keepScreenOn,
                ) {
                    vm.toggleKeepScreenOn()
                }
            }

            MyListView__ItemView(
                isFirst = false,
                isLast = false,
                withTopDivider = true,
            ) {

                MyListView__ItemView__ButtonView(
                    text = state.pomodoroTitle,
                    rightView = {
                        MyListView__ItemView__ButtonView__RightText(
                            text = state.pomodoroNote,
                        )
                    }
                ) {
                    Sheet.show { layer ->
                        ActivityPomodoroSheet(
                            layer = layer,
                            selectedTimer = state.pomodoroTimer,
                            onPick = {
                                vm.setPomodoroTimer(it)
                            }
                        )
                    }
                }
            }

            MyListView__ItemView(
                isFirst = false,
                isLast = true,
                withTopDivider = true,
            ) {

                MyListView__ItemView__ButtonView(
                    text = state.goalsTitle,
                    withArrow = true,
                    rightView = {
                        MyListView__ItemView__ButtonView__RightText(
                            text = state.goalsNote,
                            paddingEnd = 2.dp,
                        )
                    },
                ) {
                    Fs.show { layer ->
                        GoalsFormFs(
                            _layer = layer,
                            _initGoalFormsVmUi = state.goalFormsUi,
                            _onSelected = { goals ->
                                vm.setGoals(goals)
                            },
                        )
                    }
                }
            }

            MyListView__Padding__SectionSection()

            MyListView__HeaderView(
                title = state.timerHintsHeader,
            )

            MyListView__Padding__HeaderSection()

            Column {

                val hintsTypeName: List<Pair<String, HINT_TYPE>> = listOf(
                    Pair("By History", HINT_TYPE.history),
                    Pair("Custom", HINT_TYPE.custom),
                )

                hintsTypeName.forEach { pair ->

                    val isFirst = hintsTypeName.first() == pair

                    MyListView__ItemView(
                        isFirst = isFirst,
                        isLast = hintsTypeName.last() == pair,
                        withTopDivider = !isFirst
                    ) {

                        val isActive = state.activityData.timer_hints.type == pair.second

                        Column {

                            MyListView__ItemView__RadioView(
                                text = pair.first,
                                isActive = isActive
                            ) {
                                vm.setTimerHintsType(pair.second)
                            }

                            AnimatedVisibility(
                                visible = isActive
                            ) {

                                if (pair.second == HINT_TYPE.custom) {

                                    Column {

                                        /**
                                         * Impossible to use LazyColumn because of nested scroll. todo animation
                                         */
                                        state.timerHintsCustomItems.forEach { customItem ->

                                            Row(
                                                modifier = Modifier
                                                    .padding(
                                                        bottom = 8.dp,
                                                        start = H_PADDING
                                                    ),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {

                                                Icon(
                                                    painterResource(id = R.drawable.sf_xmark_large_light),
                                                    contentDescription = "Close",
                                                    modifier = Modifier
                                                        .padding(end = 4.dp)
                                                        .offset(x = (-2).dp)
                                                        .size(19.dp, 19.dp)
                                                        .clip(roundedShape)
                                                        .clickable {
                                                            vm.delCustomTimerHint(customItem.seconds)
                                                        }
                                                        .padding(4.dp),
                                                    tint = c.red
                                                )

                                                Text(
                                                    customItem.text,
                                                    modifier = Modifier
                                                        .padding(start = 1.dp)
                                                        .offset(y = (-1).dp),
                                                    color = c.text,
                                                    fontSize = 14.sp,
                                                )
                                            }
                                        }

                                        Text(
                                            "Add",
                                            modifier = Modifier
                                                .padding(
                                                    start = H_PADDING - 8.dp,
                                                    bottom = 8.dp,
                                                )
                                                .clip(roundedShape)
                                                .clickable {
                                                    Sheet.show { layer ->
                                                        TimerPickerSheet(
                                                            layer = layer,
                                                            title = "Timer Hint",
                                                            doneText = "Add",
                                                            defMinutes = 30,
                                                        ) { seconds ->
                                                            vm.addCustomTimerHint(seconds)
                                                        }
                                                    }
                                                }
                                                .padding(vertical = 4.dp, horizontal = 8.dp),
                                            color = c.blue,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (activity != null) {

                MyListView__Padding__SectionSection()

                MyListView__ItemView(
                    isFirst = true,
                    isLast = true,
                ) {
                    MyListView__ItemView__ActionView(
                        text = state.deleteText,
                    ) {
                        vm.delete {
                            layer.close()
                        }
                    }
                }
            }
        }
    }
}
