package me.timeto.app.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.VStack
import me.timeto.app.ui.c
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.Divider
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.tasks.tab.TasksTabView__PADDING_END
import me.timeto.shared.ui.calendar.CalendarVm

@Composable
fun CalendarView(
    modifier: Modifier,
) {

    val (_, state) = rememberVm {
        CalendarVm()
    }

    val selectedDay = remember {
        mutableStateOf<Int?>(null)
    }

    VStack(
        modifier = modifier
            .padding(start = H_PADDING, end = TasksTabView__PADDING_END),
    ) {

        VStack {

            HStack {

                state.weekTitles.forEach { weekTitle ->
                    Text(
                        text = weekTitle.title,
                        modifier = Modifier
                            .weight(1f),
                        color = if (weekTitle.isBusiness) c.text else c.secondaryText,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Divider(Modifier.padding(top = 2.dp))
        }

        LazyColumn(
            contentPadding = PaddingValues(top = 8.dp),
        ) {

            state.months.forEach { month ->

                item {

                    HStack {

                        repeat(month.emptyStartDaysCount) {
                            SpacerW1()
                        }

                        Text(
                            text = month.title,
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 16.dp, bottom = 8.dp),
                            color = c.white,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )

                        repeat(month.emptyEndDaysCount) {
                            SpacerW1()
                        }
                    }

                    month.weeks.forEach { week ->

                        HStack {

                            week.forEach { day ->

                                if (day == null)
                                    SpacerW1()
                                else {

                                    val bgAnimate = animateColorAsState(
                                        if (day.unixDay == selectedDay.value) c.blue
                                        else if (day.isToday) c.purple
                                        else c.transparent
                                    )

                                    VStack(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(bgAnimate.value)
                                            .clickable {
                                                selectedDay.value =
                                                    if (selectedDay.value == day.unixDay) null
                                                    else day.unixDay
                                            }
                                            .padding(bottom = 2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {

                                        Divider()

                                        Text(
                                            text = day.title,
                                            modifier = Modifier.padding(top = 6.dp),
                                            color = if (day.isBusiness) c.white else c.secondaryText,
                                        )

                                        day.previews.forEach { preview ->
                                            Text(
                                                text = preview,
                                                modifier = Modifier
                                                    .padding(horizontal = 2.dp),
                                                color = c.secondaryText,
                                                fontSize = 10.sp,
                                                lineHeight = 12.sp,
                                                fontWeight = FontWeight.Light,
                                                maxLines = 1,
                                                softWrap = false,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        val selectedDayLocal = week
                            .filterNotNull()
                            .firstOrNull { it.unixDay == selectedDay.value }

                        val isDaySelected = selectedDayLocal != null
                        AnimatedVisibility(
                            visible = isDaySelected,
                            enter = expandVertically(expandFrom = Alignment.Top),
                            exit = shrinkVertically(),
                        ) {
                            if (isDaySelected) {
                                CalendarDayView(
                                    unixDay = selectedDayLocal!!.unixDay,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
