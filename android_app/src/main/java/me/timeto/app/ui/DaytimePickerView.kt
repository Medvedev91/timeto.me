package me.timeto.app.ui

import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import me.timeto.app.*
import me.timeto.shared.vm.ui.DaytimePickerUi

private val circleSize = 20.dp
private val circleDefaultOffset = H_PADDING - (circleSize / 2) + 2.dp

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

@Composable
private fun SliderView(
    value: Int,
    ticks: List<DaytimePickerUi.Tick>,
    stepSlide: Int,
    onChange: (Int) -> Unit,
) {

    ZStack {

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
                    .height(4.dp)
                    .clip(roundedShape)
                    .background(c.sheetFg),
            )

            ZStack(
                modifier = Modifier
                    .offset(x = circleDefaultOffset)
                    .size(circleSize)
                    .clip(roundedShape)
                    .background(c.blue),
            )
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

            ZStack(Modifier.weight(0.4f))
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
