package app.time_to.timeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun DaytimePickerSheet(
    layer: WrapperView__LayerData,
    title: String,
    doneText: String,
    defMinute: Int,
    defHour: Int,
    onPick: (/* seconds or remove */ Int?) -> Unit,
) {
    val selectedHour = remember { mutableStateOf(defHour) }
    val selectedMinute = remember { mutableStateOf(defMinute) }

    Column(
        Modifier.background(c.background2)
    ) {

        SheetHeaderView(
            onCancel = { layer.close() },
            title = title,
            doneText = doneText,
            isDoneEnabled = true,
            scrollToHeader = 0,
        ) {
            onPick((selectedHour.value * 3_600) + (selectedMinute.value * 60))
            layer.close()
        }

        Column(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DayTimePickerView(
                hour = selectedHour.value,
                minute = selectedMinute.value,
                onHourChanged = { hour -> selectedHour.value = hour },
                onMinuteChanged = { minute -> selectedMinute.value = minute },
            )
            Text(
                text = "Remove",
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(99f))
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
