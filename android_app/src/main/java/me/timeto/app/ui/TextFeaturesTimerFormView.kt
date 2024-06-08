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
    onChange: (TextFeatures) -> Unit,
) {

    val formUI = remember(textFeatures) { TextFeaturesTimerFormUI(textFeatures) }

    Column {

        MyListView__ItemView(
            isFirst = true,
            isLast = false,
            bgColor = bgColor,
        ) {
            MyListView__Item__Button(
                text = formUI.activityTitle,
                rightView = {
                    MyListView__Item__Button__RightText(
                        text = formUI.activityNote,
                        color = formUI.activityColorOrNull?.toColor(),
                        extraEndPadding = (-1).dp,
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
            bgColor = bgColor,
            withTopDivider = true,
        ) {
            MyListView__Item__Button(
                text = formUI.timerTitle,
                rightView = {
                    MyListView__Item__Button__RightText(
                        text = formUI.timerNote,
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
