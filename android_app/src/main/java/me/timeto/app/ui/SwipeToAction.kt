package me.timeto.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import kotlinx.coroutines.launch
import me.timeto.app.ui.navigation.LocalNavigationFs
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToAction(
    isStartOrEnd: MutableState<Boolean?>,
    modifier: Modifier = Modifier,
    ignoreOneAction: MutableState<Boolean> = remember { mutableStateOf(false) },
    startView: @Composable () -> Unit,
    endView: @Composable (DismissState) -> Unit,
    onStart: () -> Boolean, // false - restart state
    onEnd: () -> Boolean, // false - restart state
    thresholdsDp: List<Dp> = listOf(60.dp, 80.dp), // start, end
    toVibrateStartEnd: List<Boolean> = listOf(true, true),
    slopPx: Int = dpToPx(15f),
    stateOffsetAbsDp: MutableState<Dp> = remember { mutableStateOf(0.dp) },
    content: @Composable RowScope.() -> Unit
) {

    ///
    /// TRICK
    /// rememberDismissState() keeps initial onStart/onEnd references. Using
    /// actionsTrick to pass the state reference to rememberDismissState().
    ///
    /// Bug example without using this hack:
    /// After editing and saving the task, re-editing form contains the outdated data.
    /// In SwipeToAction.onStart{} the task object with the old data.

    val actionsTrick = remember { mutableStateOf(ActionsContainer(onStart, onEnd)) }
    actionsTrick.value = ActionsContainer(onStart, onEnd)
    val state = rememberDismissState(
        confirmStateChange = {

            if (ignoreOneAction.value) {
                ignoreOneAction.value = false
                return@rememberDismissState false
            }

            if (stateOffsetAbsDp.value < 30.dp)
                return@rememberDismissState false

            when (it) {
                DismissValue.DismissedToStart -> actionsTrick.value.onEnd()
                DismissValue.DismissedToEnd -> actionsTrick.value.onStart()
                DismissValue.Default -> false
            }
        }
    )

    //////

    stateOffsetAbsDp.value = pxToDp(state.offset.value.toInt()).absoluteValue.dp

    var lastStateOffsetAbsDp by remember { mutableStateOf(stateOffsetAbsDp.value) }
    LaunchedEffect(stateOffsetAbsDp.value) {
        val threshold = thresholdsDp[if (isStartOrEnd.value == true) 0 else 1]
        val toVibrate = (isStartOrEnd.value == true && toVibrateStartEnd[0]) || (isStartOrEnd.value == false && toVibrateStartEnd[1])
        if (toVibrate && lastStateOffsetAbsDp < threshold && stateOffsetAbsDp.value >= threshold)
            vibrateShort()
        lastStateOffsetAbsDp = stateOffsetAbsDp.value
    }

    LaunchedEffect(state.dismissDirection) {
        isStartOrEnd.value = when (state.dismissDirection) {
            DismissDirection.StartToEnd -> true
            DismissDirection.EndToStart -> false
            null -> null
        }
    }

    val localViewConfiguration = LocalViewConfiguration.current
    CompositionLocalProvider(
        LocalViewConfiguration provides remember {
            object : ViewConfiguration by localViewConfiguration {
                override val touchSlop = slopPx.toFloat()
            }
        }
    ) {
        SwipeToDismiss(
            state = state,
            modifier = modifier,
            dismissThresholds = {
                FixedThreshold(
                    thresholdsDp[if (it == DismissDirection.StartToEnd) 0 else 1]
                )
            },
            background = {
                when (state.dismissDirection) {
                    DismissDirection.StartToEnd -> startView()
                    DismissDirection.EndToStart -> endView(state)
                    null -> {}
                }
            },
            dismissContent = content
        )
    }
}

@Composable
fun SwipeToAction__StartView(
    text: String,
    bgColor: Color,
) {
    val bgAnimate = animateColorAsState(bgColor)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgAnimate.value)
    ) {
        Text(
            text,
            modifier = Modifier
                .padding(start = 19.dp, bottom = 1.dp)
                .align(Alignment.CenterStart),
            color = c.white,
            fontSize = 15.sp
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToAction__DeleteView(
    state: DismissState,
    note: String,
    deletionConfirmationNote: String? = null,
    onDelete: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val navigationFs = LocalNavigationFs.current

    Row(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.red),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = note,
            color = c.white,
            fontWeight = FontWeight.W300,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 16.dp, end = 8.dp)
                .weight(1f)
        )

        Text(
            "Cancel",
            color = c.white,
            modifier = Modifier
                .padding(end = 8.dp)
                .clip(squircleShape)
                .clickable {
                    // С launchEx can be canceled
                    scope.launch {
                        state.reset()
                    }
                }
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .alpha(0.9f),
            fontSize = 15.sp,
        )

        Text(
            "Delete",
            color = c.red,
            modifier = Modifier
                .padding(end = 10.dp)
                .clip(squircleShape)
                .background(c.white)
                .clickable {
                    if (deletionConfirmationNote != null) {
                        navigationFs.confirmation(
                            message = deletionConfirmationNote,
                            buttonText = "Delete",
                            onConfirm = {
                                onDelete()
                            },
                        )
                    } else {
                        onDelete()
                    }
                }
                .padding(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 5.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

private class ActionsContainer(
    val onStart: () -> Boolean,
    val onEnd: () -> Boolean,
)
