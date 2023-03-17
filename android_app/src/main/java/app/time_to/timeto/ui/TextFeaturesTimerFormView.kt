package app.time_to.timeto.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import app.time_to.timeto.toColor
import timeto.shared.TextFeatures
import timeto.shared.vm.ui.TextFeaturesFormUI

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TextFeaturesTimerFormView(
    textFeatures: TextFeatures,
    onChange: (TextFeatures) -> Unit,
) {

    val formUI = remember(textFeatures) { TextFeaturesFormUI(textFeatures) }

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
                        onChange(formUI.upActivity(it))
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
                            onChange(formUI.upTimer(seconds))
                        }
                    )
                }
            }
        }
    }
}
