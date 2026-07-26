package me.timeto.app.ui.zen_mode

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.timeto.app.toColor
import me.timeto.app.ui.HStack
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.checklists.ChecklistView
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.squircleShape
import me.timeto.app.ui.timerFont
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.vm.zen_mode.ZenModeVm
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val notePadding = 12.dp
private val noteFontSize = 28.sp
private val dateFontSize = 18.sp

@Composable
fun ZenModeView() {

    val (vm, state) = rememberVm {
        ZenModeVm()
    }

    val scope = rememberCoroutineScope()

    val showChecklist = remember { mutableStateOf(state.initShowChecklist) }
    val showControls = remember { mutableStateOf(true) }
    val hideControlsJob = remember { mutableStateOf<Job?>(null) }
    val controlsAlphaValue = animateFloatAsState(if (showControls.value) 1f else 0f).value

    fun scheduleHideControls(
        delay: Duration = 3_000.milliseconds,
    ) {
        hideControlsJob.value?.cancel()
        hideControlsJob.value = scope.launch {
            delay(delay)
            showControls.value = false
        }
    }

    LaunchedEffect(Unit) {
        scheduleHideControls(1_000.milliseconds)
    }

    val checklistDb: ChecklistDb? =
        if (showChecklist.value) state.checklistDb else null

    val timerWeight: Float = 1f - 0.35f

    HStack(
        modifier = Modifier
            .fillMaxSize()
            .background(c.black)
            .clickable {
                if (showControls.value) {
                    hideControlsJob.value?.cancel()
                    showControls.value = false
                } else {
                    scheduleHideControls()
                    showControls.value = true
                }
            }
            .padding(end = 24.dp),
    ) {

        HStack {

            ZStack(
                modifier = Modifier
                    .weight(timerWeight),
                contentAlignment = Alignment.Center,
            ) {

                VStack(
                    modifier = Modifier
                        .alpha(controlsAlphaValue)
                        .padding(vertical = 12.dp)
                        .zIndex(2f),
                ) {

                    Text(
                        text = state.dateText,
                        color = c.secondaryText,
                        fontSize = dateFontSize,
                        fontWeight = FontWeight.SemiBold,
                    )

                    SpacerW1()

                    if (state.checklistDb != null) {
                        Text(
                            text = if (showChecklist.value) "Hide Checklist" else "Show Checklist",
                            modifier = Modifier
                                .padding(top = 12.dp)
                                .clip(roundedShape)
                                .clickable(showControls.value) {
                                    if (showChecklist.value) vm.hideChecklist() else vm.showChecklist()
                                    scheduleHideControls()
                                    showChecklist.value = !showChecklist.value
                                }
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            color = c.secondaryText,
                            fontSize = dateFontSize,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                VStack(
                    modifier = Modifier
                        .zIndex(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    val noteColor = animateColorAsState(state.timerStateUi.noteColor.toColor()).value
                    val timerColor = animateColorAsState(state.timerStateUi.timerColor.toColor()).value

                    Text(
                        text = state.timerStateUi.note,
                        modifier = Modifier
                            .alpha(controlsAlphaValue)
                            .padding(bottom = notePadding),
                        fontSize = noteFontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = noteColor,
                    )

                    VStack(
                        modifier = Modifier
                            .clip(squircleShape)
                            .clickable {
                                state.timerStateUi.togglePomodoro()
                            },
                    ) {
                        Text(
                            text = state.timerStateUi.timerText,
                            modifier = Modifier
                                .padding(vertical = 4.dp),
                            fontSize = if (checklistDb == null) 60.sp else 42.sp,
                            fontFamily = timerFont,
                            color = timerColor,
                        )
                    }

                    Text(
                        text = "--Hidden Padding--",
                        modifier = Modifier
                            .alpha(0f)
                            .padding(top = notePadding),
                        fontSize = noteFontSize,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            if (checklistDb != null) {
                val checklistScrollState = rememberLazyListState()
                ChecklistView(
                    checklistDb = checklistDb,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .weight(1f - timerWeight),
                    scrollState = checklistScrollState,
                    maxLines = 1,
                    fullHeight = false,
                    withAddButton = false,
                    topPadding = 0.dp,
                    bottomPadding = 0.dp,
                    withNavigationPadding = true,
                )
            }
        }
    }
}
