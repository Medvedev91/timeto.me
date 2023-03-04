package app.time_to.timeto.ui

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.time_to.timeto.LocalWrapperViewLayers
import timeto.shared.UnixTime
import java.util.*

@Composable
fun MyDatePicker(
    defaultTime: UnixTime,
    modifier: Modifier = Modifier,
    minPickableDay: Int,
    minSavableDay: Int,
    maxDay: Int = UnixTime.MAX_DAY,
    withTimeBtnText: String? = null,
    onSelect: (UnixTime) -> Unit,
) {
    val layers = LocalWrapperViewLayers.current
    val calendar = Calendar.getInstance(Locale.ENGLISH)
    calendar.timeInMillis = defaultTime.time * 1_000L

    Surface(
        elevation = 6.dp,
        shape = MySquircleShape(),
        color = c.blue,
        modifier = modifier
            .height(30.dp)
            .clickable {
                MyDialog.showDatePicker(
                    layers = layers,
                    defaultTime = defaultTime,
                    minPickableDay = minPickableDay,
                    minSavableDay = minSavableDay,
                    maxDay = maxDay,
                    withTimeBtnText = withTimeBtnText,
                    onSelect = onSelect
                )
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            val is0000 = defaultTime.localDayStartTime() == defaultTime.time
            val format = if (is0000) "d MMM, E" else "d MMM, E HH:mm"
            Text(
                DateFormat.format(format, calendar).toString(),
                color = c.white,
                modifier = Modifier.padding(horizontal = 10.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.W600
            )
        }
    }
}
