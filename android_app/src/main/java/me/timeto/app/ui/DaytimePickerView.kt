package me.timeto.app.ui

import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import me.timeto.app.*
import me.timeto.shared.vm.ui.DaytimePickerUi

private val circleSize = 20.dp

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

    ZStack(
        modifier = Modifier
            .fillMaxWidth()
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
                .size(circleSize)
                .clip(roundedShape)
                .background(c.blue),
        )
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
