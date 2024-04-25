package me.timeto.app.ui

import android.view.MotionEvent
import android.widget.NumberPicker
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import me.timeto.app.*
import me.timeto.shared.limitMinMax
import me.timeto.shared.vm.ui.DaytimePickerUi

private val circleSize = 20.dp
private val circleDefaultOffset = H_PADDING - (circleSize / 2)
private val circleAnimation = spring(
    visibilityThreshold = Dp.VisibilityThreshold,
    stiffness = Spring.StiffnessHigh,
)

// No size of tick, but for absolute positioned view
private val tickNoteWidthDp: Dp = 16.dp

private val allowedMotionEventActions = setOf(
    MotionEvent.ACTION_DOWN,
    MotionEvent.ACTION_MOVE,
    MotionEvent.ACTION_UP,
)

@Composable
fun DayTimePickerView(
    data: DaytimePickerUi,
    modifier: Modifier,
    onChange: (DaytimePickerUi) -> Unit,
) {

    VStack(
        modifier = modifier,
    ) {

        SliderView(
            tickIdx = data.hour,
            ticks = data.hourTicks,
            stepTicks = data.hourStepSlide,
            onChange = { newHour ->
                onChange(data.copy(hour = newHour))
            },
        )

        Padding(vertical = 8.dp)

        SliderView(
            tickIdx = data.minute,
            ticks = data.minuteTicks,
            stepTicks = data.minuteStepSlide,
            onChange = { newMinute ->
                onChange(data.copy(minute = newMinute))
            },
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SliderView(
    tickIdx: Int,
    ticks: List<DaytimePickerUi.Tick>,
    stepTicks: Int,
    onChange: (Int) -> Unit,
) {

    // 0 also used as "no value"
    val sliderXPx = remember { mutableIntStateOf(0) }
    // Not exact but approximate `slide width` / `ticks size`
    val tickAxmPx = remember { mutableFloatStateOf(0f) }

    ZStack(
        modifier = Modifier
            .motionEventSpy { event ->
                if (event.action !in allowedMotionEventActions)
                    return@motionEventSpy

                val slideXPosition = event.x - sliderXPx.intValue
                val stepPx = tickAxmPx.floatValue * stepTicks

                val prevStep = slideXPosition / stepPx
                val prevStepExtra = prevStep - prevStep.toInt()
                val newStep = (if (prevStepExtra < 0.5) prevStep else prevStep + 1).toInt()
                val newValue = (newStep * stepTicks).limitMinMax(min = 0, max = ticks.size - 1)

                if (tickIdx != newValue)
                    onChange(newValue)
            }
    ) {

        ZStack(
            modifier = Modifier
                .zIndex(1f)
                .height(circleSize),
        ) {

            ZStack(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = H_PADDING)
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        val newSliderXPx = coords.positionInWindow().x.toInt()
                        // "ticks.size - 1" to make first and last sticks on edges
                        val newTickPx = coords.size.width.toFloat() / (ticks.size - 1)
                        if (sliderXPx.intValue == newSliderXPx && tickAxmPx.floatValue == newTickPx)
                            return@onGloballyPositioned
                        sliderXPx.intValue = newSliderXPx
                        tickAxmPx.floatValue = newTickPx
                    }
                    .height(4.dp)
                    .clip(roundedShape)
                    .background(c.sheetFg),
            )

            // To ignore init animation
            if (sliderXPx.intValue > 0) {

                val circleOffset = remember(tickIdx) {
                    derivedStateOf {
                        circleDefaultOffset + pxToDp((tickAxmPx.floatValue * tickIdx).toInt()).dp
                    }
                }
                val circleOffsetAnimation = animateDpAsState(
                    targetValue = circleOffset.value,
                    animationSpec = circleAnimation,
                )

                ZStack(
                    modifier = Modifier
                        .offset(x = circleOffsetAnimation.value)
                        .size(circleSize)
                        .clip(roundedShape)
                        .background(c.blue),
                )
            }
        }

        ZStack(
            modifier = Modifier
                .zIndex(0f)
                .padding(top = 16.dp)
                .padding(horizontal = H_PADDING)
                .fillMaxWidth(),
        ) {

            ticks.forEachIndexed { tickIdx, tick ->

                ZStack(
                    modifier = Modifier
                        .offset {
                            val tickNoteHalfPx = (tickNoteWidthDp.value * this.density) / 2
                            IntOffset(((tickAxmPx.floatValue * tickIdx) - tickNoteHalfPx).toInt(), 0)
                        }
                        .width(tickNoteWidthDp),
                    contentAlignment = Alignment.TopCenter,
                ) {

                    if (tick.withStick) {
                        ZStack(
                            modifier = Modifier
                                .width(1.dp)
                                .height(5.dp)
                                .background(c.sheetFg),
                        )
                    }

                    val tickText = tick.text
                    if (tickText != null) {
                        Text(
                            text = tickText,
                            modifier = Modifier
                                .padding(top = 4.dp),
                            color = c.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayTimePickerViewOld(
    hour: Int,
    minute: Int,
    onHourChanged: (Int) -> Unit,
    onMinuteChanged: (Int) -> Unit,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier.width(100.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    val hourIndexes = 0..23
                    NumberPicker(context).apply {
                        setOnValueChangedListener { _, _, new ->
                            onHourChanged(new)
                        }
                        displayedValues = hourIndexes.map { "$it".padStart(2, '0') }.toTypedArray()
                        if (isSDKQPlus())
                            textSize = dpToPx(18f).toFloat()
                        wrapSelectorWheel = false
                        minValue = 0
                        maxValue = hourIndexes.last
                        value = hour // Set last
                    }
                }
            )
        }

        Box(
            modifier = Modifier.width(100.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    val minuteIndexes = 0..59
                    NumberPicker(context).apply {
                        setOnValueChangedListener { _, _, new ->
                            onMinuteChanged(new)
                        }
                        displayedValues = minuteIndexes.map { "$it".padStart(2, '0') }.toTypedArray()
                        if (isSDKQPlus())
                            textSize = dpToPx(18f).toFloat()
                        wrapSelectorWheel = false
                        minValue = 0
                        maxValue = minuteIndexes.last
                        value = minute // Set last
                    }
                }
            )
        }
    }
}
