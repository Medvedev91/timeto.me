package me.timeto.app.ui

import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import me.timeto.app.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.vm.ActivityTimerSheetVm

fun ActivityTimerSheet__show(
    activity: ActivityDb,
    timerContext: ActivityTimerSheetVm.TimerContext?,
    onStarted: () -> Unit,
) {
    Sheet.show { layerTimer ->
        ActivityTimerSheet(
            layer = layerTimer,
            activity = activity,
            timerContext = timerContext,
            onStarted = onStarted,
        )
    }
}

@Composable
private fun ActivityTimerSheet(
    layer: WrapperView.Layer,
    activity: ActivityDb,
    timerContext: ActivityTimerSheetVm.TimerContext?,
    onStarted: () -> Unit,
) {

    val (vm, state) = rememberVm(activity, timerContext) {
        ActivityTimerSheetVm(activity, timerContext)
    }

    Column(
        Modifier
            .background(c.sheetBg)
            .padding(top = 5.dp)
    ) {

        Row(
            modifier = Modifier
                .padding(top = 15.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                "Cancel",
                textAlign = TextAlign.Center,
                fontSize = 17.sp,
                color = c.text,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .alpha(0.7f)
                    .padding(start = 18.dp)
                    .clip(roundedShape)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .clickable {
                        layer.close()
                    }
            )

            Text(
                state.title,
                color = c.text,
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .weight(1f)
            )

            Text(
                text = "Start",
                color = c.blue,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(end = 18.dp)
                    .clip(roundedShape)
                    .clickable {
                        vm.start {
                            onStarted()
                            layer.close()
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .padding(top = 30.dp)
                .navigationBarsPadding()
                .fillMaxWidth(),
        ) {

            AndroidView(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 68.dp)
                    .width(200.dp),
                factory = { context ->
                    NumberPicker(context).apply {
                        setOnValueChangedListener { _, _, new ->
                            vm.setFormTimeItemIdx(new)
                        }
                        displayedValues = state.timeItems.map { it.title }.toTypedArray()
                        if (isSDKQPlus())
                            textSize = dpToPx(18f).toFloat()
                        wrapSelectorWheel = false
                        minValue = 0
                        maxValue = state.timeItems.size - 1
                        value = state.formTimeItemIdx // Задавать в конце
                    }
                }
            )

            TimerHintsView(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                timerHintsUI = state.timerHints,
                hintHPadding = 8.dp,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                onStart = {
                    layer.close()
                }
            )
        }
    }
}
