package me.timeto.app.ui.daytime_picker

import android.widget.NumberPicker
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import me.timeto.app.HStack
import me.timeto.app.ZStack
import me.timeto.app.dpToPx
import me.timeto.app.isSDKQPlus

@Composable
fun DaytimePickerView(
    hour: Int,
    minute: Int,
    onHourChanged: (Int) -> Unit,
    onMinuteChanged: (Int) -> Unit,
) {

    HStack(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {

        ZStack(
            modifier = Modifier.width(100.dp),
            contentAlignment = Alignment.Center,
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
                },
            )
        }

        ZStack(
            modifier = Modifier.width(100.dp),
            contentAlignment = Alignment.Center,
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
                },
            )
        }
    }
}
