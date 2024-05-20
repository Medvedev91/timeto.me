package me.timeto.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.timeto.app.c
import me.timeto.app.toColor
import me.timeto.shared.TextFeatures
import me.timeto.shared.vm.ui.TextFeaturesTimerFormUI

@Composable
fun TextFeaturesTimerFormView(
    textFeatures: TextFeatures,
    bgColor: Color = c.sheetFg,
    dividerColor: Color = c.sheetDividerFg,
    onChange: (TextFeatures) -> Unit,
) {

    val formUI = remember(textFeatures) { TextFeaturesTimerFormUI(textFeatures) }

    Column {

        MyListView__ItemView(
            isFirst = true,
            isLast = false,
        ) {
            MyListView__ItemView__ButtonView(
                text = formUI.activityTitle,
                withArrow = true,
                bgColor = bgColor,
                rightView = {
                    MyListView__ItemView__ButtonView__RightText(
                        text = formUI.activityNote,
                        paddingEnd = 2.dp,
                        color = formUI.activityColorOrNull?.toColor(),
                    )
                }
            ) {
                Sheet.show { layer ->
                    ActivityPickerSheet(
                        layer = layer,
                    ) {
                        onChange(formUI.setActivity(it))
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
                text = formUI.timerTitle,
                withArrow = true,
                bgColor = bgColor,
                rightView = {
                    MyListView__ItemView__ButtonView__RightText(
                        text = formUI.timerNote,
                        paddingEnd = 2.dp,
                        color = formUI.timerColorOrNull?.toColor(),
                    )
                }
            ) {
                Sheet.show { layer ->
                    TimerPickerSheet(
                        layer = layer,
                        title = "Timer",
                        doneText = "Done",
                        defMinutes = 30,
                        onPick = { seconds ->
                            onChange(formUI.setTimer(seconds))
                        }
                    )
                }
            }
        }
    }
}
