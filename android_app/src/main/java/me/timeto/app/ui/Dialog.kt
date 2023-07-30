package me.timeto.app.ui

import android.widget.CalendarView
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import me.timeto.app.c
import me.timeto.app.toUnixTime
import me.timeto.shared.UnixTime
import me.timeto.shared.toHms
import java.text.SimpleDateFormat
import java.util.*

object Dialog {

    fun show(
        modifier: Modifier = Modifier,
        margin: PaddingValues = PaddingValues(horizontal = 20.dp),
        content: @Composable (WrapperView.Layer) -> Unit
    ) {
        WrapperView.Layer(
            enterAnimation = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
            exitAnimation = fadeOut(spring(stiffness = Spring.StiffnessMedium)),
            alignment = Alignment.Center,
            onClose = {},
            content = { layer ->
                Box(
                    modifier
                        .systemBarsPadding()
                        .imePadding()
                        .padding(margin)
                        .clip(MySquircleShape(80f))
                        .pointerInput(Unit) { }
                ) {
                    content(layer)
                }
            }
        ).show()
    }

    fun showDatePicker(
        defaultTime: UnixTime? = null,
        minPickableDay: Int, // Available to pick
        minSavableDay: Int, // Active add button
        maxDay: Int = UnixTime.MAX_DAY,
        title: String? = null,
        withTimeBtnText: String? = null,
        onSelect: (UnixTime) -> Unit,
    ) {
        show(
            margin = PaddingValues(horizontal = 30.dp)
        ) { layer ->

            val defHms = if (defaultTime != null)
                (defaultTime.time - defaultTime.localDayStartTime()).toHms()
            else null

            var timeHIndex by remember { mutableStateOf(defHms?.get(0) ?: 0) }
            var timeMIndex by remember { mutableStateOf(defHms?.get(1) ?: 0) }

            var selectedDay by remember { mutableStateOf(defaultTime?.localDay ?: minPickableDay) }

            fun onSelectAndClose() {
                onSelect(UnixTime.byLocalDay(selectedDay).inSeconds((timeHIndex * 3600) + (timeMIndex * 60)))
                layer.close()
            }
            Column(
                modifier = Modifier
                    .background(c.background2)
            ) {

                if (title != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(c.datePickerTitleBg)
                    ) {
                        Text(
                            title,
                            modifier = Modifier
                                .padding(top = 19.dp, bottom = 19.dp, start = 20.dp, end = 20.dp)
                                .align(Alignment.Center),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = c.text,
                        )

                        Divider(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(0.8.dp),
                            color = c.dividerBg2,
                        )
                    }
                }

                AndroidView(
                    { CalendarView(it) },
                    modifier = Modifier.padding(top = 4.dp),
                    update = { calendarView ->
                        calendarView.date = UnixTime.byLocalDay(selectedDay).time * 1000L

                        calendarView.maxDate = UnixTime.byLocalDay(maxDay).time * 1000L
                        calendarView.minDate = UnixTime.byLocalDay(minPickableDay).time * 1000L

                        calendarView.setOnDateChangeListener { _, cYear, cMonthIndex, cDay ->
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val newDate = formatter.parse("$cYear-${cMonthIndex + 1}-${cDay}")!!
                            selectedDay = newDate.toUnixTime().localDay
                            if (withTimeBtnText == null)
                                onSelectAndClose()
                        }
                    }
                )

                if (withTimeBtnText != null)
                    Box {
                        Row(
                            modifier = Modifier.padding(bottom = 24.dp, start = 24.dp, end = 23.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(
                                modifier = Modifier
                            ) {
                                MyPicker(
                                    items = (0..23).map { "$it".padStart(2, '0') },
                                    containerWidth = 30.dp,
                                    containerHeight = 30.dp,
                                    itemHeight = 20.dp,
                                    selectedIndex = timeHIndex,
                                    onChange = { index, _ ->
                                        timeHIndex = index
                                    },
                                )
                            }

                            Text(
                                ":",
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .offset(y = (-2.2).dp),
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                            )

                            Box(
                                modifier = Modifier
                            ) {
                                MyPicker(
                                    items = (0..59).map { "$it".padStart(2, '0') },
                                    containerWidth = 30.dp,
                                    containerHeight = 30.dp,
                                    itemHeight = 20.dp,
                                    selectedIndex = timeMIndex,
                                    onChange = { index, _ ->
                                        timeMIndex = index
                                    },
                                )
                            }

                            Box(Modifier.weight(1f))

                            MyButton(
                                text = withTimeBtnText,
                                isEnabled = selectedDay in minSavableDay..maxDay,
                                modifier = Modifier,
                                backgroundColor = c.blue,
                                extraPaddings = -4 to 0
                            ) {
                                onSelectAndClose()
                            }
                        }

                        Icon(
                            Icons.Rounded.Close,
                            "Close",
                            tint = c.textSecondary,
                            modifier = Modifier
                                .offset(x = (-22).dp, y = (-42).dp)
                                .alpha(0.7f)
                                .align(Alignment.TopEnd)
                                .padding(end = 4.dp)
                                .size(30.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(c.transparent)
                                .clickable {
                                    layer.close()
                                }
                                .padding(4.dp)
                        )
                    }
            }
        }
    }
}
