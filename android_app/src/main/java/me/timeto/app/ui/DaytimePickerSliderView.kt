package me.timeto.app.ui

import android.view.MotionEvent
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
import androidx.compose.ui.zIndex
import me.timeto.app.*
import me.timeto.shared.misc.DaytimeUi
import me.timeto.shared.models.DaytimePickerSliderUi

private val hPadding = H_PADDING + 1.dp

// Slider line a little wider to nice ui
private val sliderInternalPadding = 3.dp
private val circleSize = 22.dp
private val circleDefaultOffset = hPadding - (circleSize / 2) + sliderInternalPadding
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
fun DaytimePickerSliderView(
    daytimeUi: DaytimeUi,
    modifier: Modifier,
    onChange: (DaytimeUi) -> Unit,
) {

    val sliderUi = DaytimePickerSliderUi(
        hour = daytimeUi.hour,
        minute = daytimeUi.minute,
    )

    VStack(
        modifier = modifier,
    ) {

        SliderView(
            tickIdx = sliderUi.hour,
            ticks = sliderUi.hourTicks,
            stepTicks = sliderUi.hourStepSlide,
            onChange = { newHour ->
                onChange(daytimeUi.copy(hour = newHour))
            },
        )

        Padding(vertical = 8.dp)

        SliderView(
            tickIdx = sliderUi.minute,
            ticks = sliderUi.minuteTicks,
            stepTicks = sliderUi.minuteStepSlide,
            onChange = { newMinute ->
                onChange(daytimeUi.copy(minute = newMinute))
            },
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SliderView(
    tickIdx: Int,
    ticks: List<DaytimePickerSliderUi.Tick>,
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

                val newIdx = DaytimePickerSliderUi.calcSliderTickIdx(
                    ticksSize = ticks.size,
                    stepTicks = stepTicks,
                    slideXPosition = slideXPosition,
                    stepPx = stepPx,
                )

                if (tickIdx != newIdx)
                    onChange(newIdx)
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
                    .padding(horizontal = hPadding)
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(roundedShape)
                    .background(c.dividerBg)
                    .padding(horizontal = sliderInternalPadding)
                    .onGloballyPositioned { coords ->
                        val newSliderXPx = coords.positionInWindow().x.toInt()
                        // "ticks.size - 1" to make first and last sticks on edges
                        val newTickPx = coords.size.width.toFloat() / (ticks.size - 1)
                        if (sliderXPx.intValue == newSliderXPx && tickAxmPx.floatValue == newTickPx)
                            return@onGloballyPositioned
                        sliderXPx.intValue = newSliderXPx
                        tickAxmPx.floatValue = newTickPx
                    },
            )

            //
            // Circle
            // Ignore init animation

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
                    contentAlignment = Alignment.Center,
                ) {

                    Text(
                        text = ticks[tickIdx].text,
                        modifier = Modifier
                            .offset(y = -onePx * 2),
                        color = c.text,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            ////
        }

        ZStack(
            modifier = Modifier
                .zIndex(0f)
                .padding(top = 16.dp)
                .padding(horizontal = hPadding + sliderInternalPadding)
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

                    if (tick.withSliderStick) {
                        ZStack(
                            modifier = Modifier
                                .width(1.dp)
                                .height(5.dp)
                                .background(c.dividerFg),
                        )
                    }

                    val sliderStickText = tick.sliderStickText
                    if (sliderStickText != null) {
                        Text(
                            text = sliderStickText,
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
