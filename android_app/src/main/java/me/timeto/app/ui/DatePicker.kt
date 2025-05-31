package me.timeto.app.ui

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import me.timeto.app.toUnixTime
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.navigation.Navigation
import me.timeto.shared.UnixTime
import java.text.SimpleDateFormat
import java.util.*

fun Navigation.showDatePicker(
    unixTime: UnixTime,
    minTime: UnixTime,
    maxTime: UnixTime,
    onDone: (UnixTime) -> Unit,
) {
    this.dialog {
        DatePicker(
            unixTime = unixTime,
            minTime = minTime,
            maxTime = maxTime,
            onDone = onDone,
        )
    }
}

@Composable
private fun DatePicker(
    unixTime: UnixTime,
    minTime: UnixTime,
    maxTime: UnixTime,
    onDone: (UnixTime) -> Unit,
) {
    val navigationLayer = LocalNavigationLayer.current

    ZStack(
        modifier = Modifier
            .background(c.fg)
    ) {

        AndroidView(
            factory = { context ->
                CalendarView(context)
            },
            modifier = Modifier
                .padding(top = 4.dp),
            update = { calendarView ->
                calendarView.date = unixTime.time * 1000L

                calendarView.minDate = minTime.time * 1000L
                calendarView.maxDate = maxTime.time * 1000L

                calendarView.setOnDateChangeListener { _, cYear, cMonthIndex, cDay ->
                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val newDate = formatter.parse("$cYear-${cMonthIndex + 1}-${cDay}")!!
                    onDone(newDate.toUnixTime())
                    navigationLayer.close()
                }
            }
        )
    }
}
