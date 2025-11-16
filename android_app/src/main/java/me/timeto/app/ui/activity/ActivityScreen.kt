package me.timeto.app.ui.activity

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.HStack
import me.timeto.app.ui.c
import me.timeto.app.ui.Screen
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.history.HistoryFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.onePx
import me.timeto.app.ui.summary.SummaryFs
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.squircleShape
import me.timeto.shared.vm.summary.SummaryVm

@Composable
fun ActivityScreen(
    onClose: () -> Unit,
) {

    BackHandler {
        onClose()
    }

    val (summaryVm, summaryState) = rememberVm {
        SummaryVm()
    }

    val isListOrSummary = remember {
        mutableStateOf(true)
    }

    Screen {
        ZStack {
            HistoryFs()
            if (!isListOrSummary.value) {
                SummaryFs(
                    vm = summaryVm,
                    state = summaryState,
                )
            }
            BottomMenu(
                summaryVm = summaryVm,
                summaryState = summaryState,
                isListOrSummary = isListOrSummary,
            )
        }
    }
}

///

private val bgBottomMenu = c.black.copy(alpha = .8f)

@Composable
private fun BoxScope.BottomMenu(
    summaryVm: SummaryVm,
    summaryState: SummaryVm.State,
    isListOrSummary: MutableState<Boolean>,
) {
    val navigationFs = LocalNavigationFs.current

    HStack(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .align(Alignment.BottomCenter)
            .height(42.dp)
            .clip(squircleShape)
            .background(bgBottomMenu),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MenuButton(
            text = "List",
            isSelected = isListOrSummary.value,
            onTap = {
                isListOrSummary.value = true
                summaryVm.setPeriodToday()
            },
        )
        MenuSeparator()
        summaryState.periodHints.forEach { periodHintUi ->
            MenuButton(
                text = periodHintUi.title,
                isSelected = !isListOrSummary.value && periodHintUi.isActive,
            ) {
                isListOrSummary.value = false
                summaryVm.setPeriod(
                    pickerTimeStart = periodHintUi.pickerTimeStart,
                    pickerTimeFinish = periodHintUi.pickerTimeFinish,
                )
            }
        }
        MenuSeparator()
        MenuButton(
            text = summaryState.dateTitle,
            isSelected = summaryState.isCustomPeriodSelected,
            onTap = {
                navigationFs.push {
                    /*
                    SummaryCalendarFullScreen(
                        selectedStartTime: summaryState. pickerTimeStart,
                    selectedFinishTime: summaryState.pickerTimeFinish,
                    onSelected: {
                    timeStart, timeFinish in
                    summaryVm.setPeriod(
                        pickerTimeStart: timeStart,
                        pickerTimeFinish: timeFinish,
                    )
                    isListOrSummary = false
                }
                    )
                   */
                }
            }
        )
    }
}

@Composable
private fun MenuButton(
    text: String,
    isSelected: Boolean,
    onTap: () -> Unit,
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(squircleShape)
            .clickable {
                onTap()
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontSize = 14.sp,
        color = if (isSelected) c.text else c.secondaryText,
        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
    )
}

@Composable
private fun MenuSeparator() {
    ZStack(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(onePx)
            .height(16.dp)
            .background(c.secondaryText),
    )
}
