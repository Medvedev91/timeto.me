package me.timeto.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import me.timeto.app.toColor
import me.timeto.shared.TextFeatures
import me.timeto.shared.vm.ui.TextFeaturesTimerFormUI

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TextFeaturesTimerFormView(
    textFeatures: TextFeatures,
    onChange: (TextFeatures) -> Unit,
) {

    val formUI = remember(textFeatures) { TextFeaturesTimerFormUI(textFeatures) }

    Column {

        val keyboardController = LocalSoftwareKeyboardController.current

        MyListView__ItemView(
            isFirst = true,
            isLast = false,
        ) {
            MyListView__ItemView__ButtonView(
                text = formUI.activityTitle,
                withArrow = true,
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
        ) {
            MyListView__ItemView__ButtonView(
                text = formUI.timerTitle,
                withArrow = true,
                rightView = {
                    MyListView__ItemView__ButtonView__RightText(
                        text = formUI.timerNote,
                        paddingEnd = 2.dp,
                        color = formUI.timerColorOrNull?.toColor(),
                    )
                }
            ) {
                keyboardController?.hide()
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
