package me.timeto.app.ui.zen_mode

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.timeto.app.MainActivity
import me.timeto.app.WindowInsets
import me.timeto.app.toColor
import me.timeto.app.ui.HStack
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.checklists.ChecklistView
import me.timeto.app.ui.pxToDp
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.squircleShape
import me.timeto.app.ui.timerFont
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.vm.zen_mode.ZenModeVm
import kotlin.math.max
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

    val mainActivity = LocalActivity.current as MainActivity
    val windowInsetsController: WindowInsetsControllerCompat = remember {
        val window = mainActivity.window
        WindowCompat.getInsetsController(window, window.decorView)
    }

    val scope = rememberCoroutineScope()

    val checklistDb: ChecklistDb? = state.checklistDb

    val showChecklist = remember { mutableStateOf(state.initShowChecklist && (checklistDb != null)) }
    val isControlsShowed = remember { mutableStateOf(true) }
    val hideControlsJob = remember { mutableStateOf<Job?>(null) }
    val controlsAlphaValue = animateFloatAsState(if (isControlsShowed.value) 1f else 0f).value

    fun hideSystemBars() {
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
    }

    fun hideControls() {
        hideSystemBars()
        isControlsShowed.value = false
    }

    fun scheduleHideControls(
        delay: Duration = 3_000.milliseconds,
    ) {
        hideControlsJob.value?.cancel()
        hideControlsJob.value = scope.launch {
            // Т.к. системные элементы затухают медленнее,
            // сперва гасим их. 1.6 - Экспериментально.
            val partDelay: Duration = delay / 1.6
            delay(partDelay)
            hideSystemBars()
            delay(delay - partDelay)
            hideControls()
        }
    }

    fun showControls() {
        scheduleHideControls()
        windowInsetsController.show(WindowInsetsCompat.Type.statusBars())
        windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
        isControlsShowed.value = true
    }

    LaunchedEffect(Unit) {
        scheduleHideControls(1_000.milliseconds)
    }

    val timerWeight: Float = animateFloatAsState(
        1f - (if (showChecklist.value) 0.35f else 0.0001f),
    ).value

    val windowInsets: WindowInsets =
        mainActivity.windowInsetsFlow.collectAsState().value
    val hPadding: Dp = remember(windowInsets) {
        pxToDp(max(windowInsets.left, windowInsets.right)).dp
    }

    val windowTopInset: Int = windowInsets.top
    LaunchedEffect(windowTopInset) {
        if (windowTopInset > 0)
            showControls()
    }

    HStack(
        modifier = Modifier
            .fillMaxSize()
            .background(c.black)
            .clickable {
                if (isControlsShowed.value) {
                    hideControlsJob.value?.cancel()
                    hideControls()
                } else {
                    showControls()
                }
            }
            .padding(horizontal = hPadding),
    ) {

        ZStack(
            contentAlignment = Alignment.Center,
        ) {

            VStack(
                modifier = Modifier
                    .alpha(controlsAlphaValue)
                    .padding(top = 4.dp, bottom = 20.dp)
                    .zIndex(2f),
            ) {

                Text(
                    text = state.dateText,
                    color = c.secondaryText,
                    fontSize = dateFontSize,
                    fontWeight = FontWeight.SemiBold,
                )

                SpacerW1()

                if (checklistDb != null) {
                    Text(
                        text = if (showChecklist.value) "Hide Checklist" else "Show Checklist",
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .clip(roundedShape)
                            .clickable(isControlsShowed.value) {
                                if (showChecklist.value) vm.hideChecklist() else vm.showChecklist()
                                showChecklist.value = !showChecklist.value
                                showControls()
                            }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        color = c.secondaryText,
                        fontSize = dateFontSize,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            HStack {

                VStack(
                    modifier = Modifier
                        .weight(timerWeight)
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
                        val timerTextFontSizeAnimate =
                            animateIntAsState(if (!showChecklist.value) 60 else 48)
                        Text(
                            text = state.timerStateUi.timerText,
                            modifier = Modifier
                                .padding(vertical = 4.dp),
                            fontSize = timerTextFontSizeAnimate.value.sp,
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

                if (checklistDb != null) {
                    AnimatedVisibility(
                        visible = showChecklist.value,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f - timerWeight),
                    ) {
                        val checklistScrollState = rememberLazyListState()
                        ChecklistView(
                            checklistDb = checklistDb,
                            modifier = Modifier,
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
    }
}
