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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import me.timeto.app.*
import me.timeto.shared.limitMinMax
import me.timeto.shared.vm.ui.DaytimePickerUi

private val circleSize = 20.dp
private val circleDefaultOffset = H_PADDING - (circleSize / 2)
private val allowedMotionEventActions = setOf(
    MotionEvent.ACTION_DOWN,
    MotionEvent.ACTION_MOVE,
    MotionEvent.ACTION_UP,
)
private val circleAnimation = spring(
    visibilityThreshold = Dp.VisibilityThreshold,
    stiffness = Spring.StiffnessHigh,
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
            value = data.minute,
            ticks = data.minuteTicks,
            stepSlide = data.minuteStepSlide,
            onChange = { newMinute ->
                onChange(data.copy(minute = newMinute))
            },
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SliderView(
    value: Int,
    ticks: List<DaytimePickerUi.Tick>,
    stepSlide: Int,
    onChange: (Int) -> Unit,
) {

    // 0 also used as "no value"
    val sliderXPx = remember { mutableIntStateOf(0) }
    val tickPx = remember { mutableFloatStateOf(0f) }

    ZStack(
        modifier = Modifier
            .motionEventSpy { event ->
                if (event.action !in allowedMotionEventActions)
                    return@motionEventSpy

                val slideLength = event.x - sliderXPx.intValue
                val preciseValue = (slideLength / tickPx.floatValue)
                    .toInt()
                    .limitMinMax(min = 0, max = ticks.size - 1)

                val remStep = preciseValue % stepSlide
                val newValue: Int = if (
                    ((preciseValue + stepSlide) > ticks.size) ||
                    ((remStep.toFloat() / stepSlide) < 0.5)
                )
                    preciseValue - remStep
                else
                    preciseValue + (stepSlide - remStep)

                if (value != newValue)
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
                        val newTickPx = coords.size.width.toFloat() / ticks.size
                        if (sliderXPx.intValue == newSliderXPx && tickPx.floatValue == newTickPx)
                            return@onGloballyPositioned
                        sliderXPx.intValue = newSliderXPx
                        tickPx.floatValue = newTickPx
                    }
                    .height(4.dp)
                    .clip(roundedShape)
                    .background(c.sheetFg),
            )

            // To ignore init animation
            if (sliderXPx.intValue > 0) {

                val circleOffset = remember(value) {
                    derivedStateOf {
                        circleDefaultOffset + pxToDp((tickPx.floatValue * value).toInt()).dp
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

        HStack(
            modifier = Modifier
                .zIndex(0f)
                .padding(top = 16.dp)
                .padding(horizontal = H_PADDING)
                .fillMaxWidth(),
        ) {

            ticks.forEach { tick ->

                val tickText = tick.text
                if (tickText != null) {
                    val isFirst = (tick.value == 0)
                    VStack(
                        modifier = Modifier
                            .weight(if (isFirst) 0.5f else 1f),
                        horizontalAlignment = if (isFirst) Alignment.Start else Alignment.CenterHorizontally,
                    ) {

                        ZStack(
                            modifier = Modifier
                                .offset(x = if (isFirst) 2.dp else 0.dp, y = 1.dp)
                                .width(1.dp)
                                .height(5.dp)
                                .background(c.sheetFg),
                        )

                        Text(
                            text = tickText,
                            modifier = Modifier
                                .offset(x = if (isFirst) (-1).dp else 0.dp),
                            color = c.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                        )
                    }
                }
            }

            ZStack(Modifier.weight(0.5f))
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
