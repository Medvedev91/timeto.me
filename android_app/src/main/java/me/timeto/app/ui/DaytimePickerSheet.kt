package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import me.timeto.app.c
import me.timeto.app.roundedShape

@Composable
fun DaytimePickerSheet(
    layer: WrapperView.Layer,
    title: String,
    doneText: String,
    defMinute: Int,
    defHour: Int,
    onPick: (/* seconds or remove */ Int?) -> Unit,
) {
    val selectedHour = remember { mutableIntStateOf(defHour) }
    val selectedMinute = remember { mutableIntStateOf(defMinute) }

    Column(
        Modifier.background(c.sheetBg)
    ) {

        Sheet.HeaderViewOld(
            onCancel = { layer.close() },
            title = title,
            doneText = doneText,
            isDoneEnabled = true,
            scrollState = null,
        ) {
            onPick((selectedHour.intValue * 3_600) + (selectedMinute.intValue * 60))
            layer.close()
        }

        Column(
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

            Text(
                text = "Remove",
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clip(roundedShape)
                    .clickable {
                        onPick(null)
                        layer.close()
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                color = c.red,
            )
        }
    }
}
