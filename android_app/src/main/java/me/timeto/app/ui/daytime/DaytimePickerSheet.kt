package me.timeto.app.ui.daytime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.timeto.app.VStack
import me.timeto.app.c
import me.timeto.app.roundedShape
import me.timeto.app.ui.DaytimePickerView
import me.timeto.app.ui.Sheet
import me.timeto.app.ui.WrapperView
import me.timeto.shared.misc.DaytimeUi

@Composable
fun DaytimePickerSheet(
    layer: WrapperView.Layer,
    modifier: Modifier = Modifier,
    title: String,
    doneText: String,
    daytimeUi: DaytimeUi,
    withRemove: Boolean,
    onDone: (DaytimeUi) -> Unit,
    onRemove: () -> Unit,
) {

    val selectedHour = remember { mutableIntStateOf(daytimeUi.hour) }
    val selectedMinute = remember { mutableIntStateOf(daytimeUi.minute) }

    VStack(
        modifier = modifier
            .background(c.sheetBg),
    ) {

        Sheet.HeaderViewOld(
            onCancel = { layer.close() },
            title = title,
            doneText = doneText,
            isDoneEnabled = true,
            scrollState = null,
        ) {
            val newDaytimeUi = DaytimeUi(
                hour = selectedHour.intValue,
                minute = selectedMinute.intValue,
            )
            onDone(newDaytimeUi)
            layer.close()
        }

        VStack(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            DaytimePickerView(
                hour = selectedHour.intValue,
                minute = selectedMinute.intValue,
                onHourChanged = { hour -> selectedHour.intValue = hour },
                onMinuteChanged = { minute -> selectedMinute.intValue = minute },
            )

            if (withRemove) {
                Text(
                    text = "Remove",
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clip(roundedShape)
                        .clickable {
                            onRemove()
                            layer.close()
                        }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    color = c.red,
                )
            }
        }
    }
}
