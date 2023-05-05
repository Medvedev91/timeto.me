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
import app.time_to.timeto.*
import timeto.shared.TimerPickerItem
import timeto.shared.launchEx

@Composable
fun TimerPickerSheet(
    layer: WrapperView.Layer,
    title: String,
    doneText: String,
    defMinutes: Int,
    onPick: (/* seconds */ Int) -> Unit,
) {
    val scope = rememberCoroutineScope()

    // todo what if change defMinutes/defSeconds -> timeItems
    val defSeconds = defMinutes * 60
    val timeItems = remember { TimerPickerItem.buildList(defSeconds) }
    ////

    var formTimeItemIdx by remember { mutableStateOf(timeItems.indexOfFirst { it.seconds == defSeconds }) }

    ////

    Column(
        Modifier
            .background(c.background2)
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
                title,
                color = c.text,
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                fontWeight = FontWeight.W500,
                modifier = Modifier
                    .weight(1f)
            )

            Text(
                text = doneText,
                color = c.blue,
                fontWeight = FontWeight.W700,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(end = 18.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .clickable {
                        scope.launchEx {
                            onPick(timeItems[formTimeItemIdx].seconds)
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
                                formTimeItemIdx = new
                            }
                            displayedValues = timeItems.map { it.title }.toTypedArray()
                            if (isSDKQPlus())
                                textSize = dpToPx(18f).toFloat()
                            wrapSelectorWheel = false
                            minValue = 0
                            maxValue = timeItems.size - 1
                            value = formTimeItemIdx // Set last
                        }
                    }
                )
            }
        }
    }
}
