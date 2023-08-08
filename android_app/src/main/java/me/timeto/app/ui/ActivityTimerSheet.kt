package me.timeto.app.ui

import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.launchEx
import me.timeto.shared.vm.ActivityTimerSheetVM

@Composable
fun ActivityTimerSheet(
    layer: WrapperView.Layer,
    activity: ActivityModel,
    timerContext: ActivityTimerSheetVM.TimerContext?,
    onStarted: (() -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()

    val (vm, state) = rememberVM(activity, timerContext) {
        ActivityTimerSheetVM(activity, timerContext)
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
                    .clip(RoundedCornerShape(99.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .clickable {
                        scope.launchEx {
                            layer.close()
                        }
                    }
            )

            Text(
                state.title,
                color = c.text,
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                fontWeight = FontWeight.W500,
                modifier = Modifier
                    .weight(1f)
            )

            Text(
                text = "Start",
                color = c.blue,
                fontWeight = FontWeight.W700,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(end = 18.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .clickable {
                        vm.start {
                            onStarted?.invoke()
                            layer.close()
                        }
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 20.dp, bottom = 60.dp)
                .height(220.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier.width(200.dp),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
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
            }
        }
    }
}
