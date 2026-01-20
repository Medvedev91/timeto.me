package me.timeto.app.ui.goals.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.Screen
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.form.plain.FormPlainButtonDeletion
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.home.HomeScreen__primaryFontSize
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.timer.TimerSheet
import me.timeto.shared.vm.goals.form.GoalFormTimerHintsVm

@Composable
fun GoalFormTimerHintsFs(
    initTimerHints: List<Int>,
    onDone: (List<Int>) -> Unit,
) {
    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        GoalFormTimerHintsVm(initTimerHints = initTimerHints)
    }

    Screen {

        Header(
            title = "Timer Hints",
            scrollState = null,
            actionButton = HeaderActionButton(
                text = "Done",
                isEnabled = true,
                onClick = {
                    onDone(vm.getTimerHints())
                    navigationLayer.close()
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            ),
        )

        state.timerHintsUi.forEach { timerHintUi ->
            FormPlainButtonDeletion(
                title = timerHintUi.text,
                isFirst = state.timerHintsUi.first() == timerHintUi,
                modifier = Modifier,
                onClick = {},
                onDelete = {
                    vm.delete(timerHintUi.seconds)
                },
            )
        }

        ZStack(
            modifier = Modifier
                .height(HomeScreen__itemHeight),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = "New Timer Hint",
                color = c.blue,
                modifier = Modifier
                    .clip(roundedShape)
                    .clickable {
                        navigationFs.push {
                            TimerSheet(
                                title = "Timer",
                                doneTitle = "Done",
                                initSeconds = 45 * 60,
                                onDone = { newTimer ->
                                    vm.add(seconds = newTimer)
                                },
                            )
                        }
                    }
                    .padding(vertical = 4.dp)
                    .padding(horizontal = H_PADDING),
                fontSize = HomeScreen__primaryFontSize,
            )
        }
    }
}
