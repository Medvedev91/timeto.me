package me.timeto.app.ui.activities.timer

import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import me.timeto.app.ui.HStack
import me.timeto.app.H_PADDING_HALF
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.c
import me.timeto.app.dpToPx
import me.timeto.app.isSDKQPlus
import me.timeto.app.ui.rememberVm
import me.timeto.app.roundedShape
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.ui.activities.timer.ActivityTimerStrategy
import me.timeto.shared.ui.activities.timer.ActivityTimerVm

@Composable
fun ActivityTimerFs(
    activityDb: ActivityDb,
    strategy: ActivityTimerStrategy,
) {

    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm(activityDb, strategy) {
        ActivityTimerVm(
            activityDb = activityDb,
            strategy = strategy,
        )
    }

    val timerItemsUi = state.timerItemsUi
    val formTimeItemIdx: MutableState<Int> = remember {
        mutableStateOf(timerItemsUi.indexOfFirst { it.seconds == state.initSeconds })
    }

    VStack {

        SpacerW1()

        VStack(
            Modifier
                .weight(1f)
                .background(c.fg),
        ) {

            HStack(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Text(
                    text = "Cancel",
                    textAlign = TextAlign.Center,
                    color = c.blue,
                    modifier = Modifier
                        .padding(start = H_PADDING_HALF)
                        .clip(roundedShape)
                        .clickable {
                            navigationLayer.close()
                        }
                        .padding(horizontal = H_PADDING_HALF, vertical = 6.dp),
                )

                Text(
                    text = state.title,
                    color = c.text,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f),
                )

                Text(
                    text = "Start",
                    color = c.blue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(end = H_PADDING_HALF, start = 12.dp)
                        .clip(roundedShape)
                        .clickable {
                            vm.start(
                                seconds = timerItemsUi[formTimeItemIdx.value].seconds,
                                onSuccess = {
                                    navigationLayer.close()
                                },
                            )
                        }
                        .padding(horizontal = H_PADDING_HALF, vertical = 6.dp),
                )
            }

            val note: String? = state.note
            if (note != null) {
                Text(
                    text = note,
                    color = c.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(),
                )
            }

            ZStack(
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
                                formTimeItemIdx.value = new
                            }
                            displayedValues = timerItemsUi.map { it.title }.toTypedArray()
                            if (isSDKQPlus())
                                textSize = dpToPx(18f).toFloat()
                            wrapSelectorWheel = false
                            minValue = 0
                            maxValue = timerItemsUi.size - 1
                            value = formTimeItemIdx.value // Set last
                        }
                    }
                )
            }
        }
    }
}
