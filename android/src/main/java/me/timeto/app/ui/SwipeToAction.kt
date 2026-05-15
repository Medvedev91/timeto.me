package me.timeto.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.Haptic
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToAction(
    isStartOrEnd: MutableState<Boolean?>,
    modifier: Modifier = Modifier,
    ignoreOneAction: MutableState<Boolean> = remember { mutableStateOf(false) },
    startView: @Composable (DismissState) -> Unit,
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
            Haptic.shot()
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
                    DismissDirection.StartToEnd -> startView(state)
                    DismissDirection.EndToStart -> endView(state)
                    null -> {}
                }
            },
            dismissContent = content
        )
    }
}


private class ActionsContainer(
    val onStart: () -> Boolean,
    val onEnd: () -> Boolean,
)
