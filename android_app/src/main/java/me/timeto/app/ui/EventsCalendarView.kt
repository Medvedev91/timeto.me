package me.timeto.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.vm.EventsCalendarVM

@Composable
fun EventsCalendarView(
    modifier: Modifier,
) {

    val (_, state) = rememberVM { EventsCalendarVM() }

    VStack(
        modifier = modifier
            .padding(start = H_PADDING, end = TasksView__PADDING_END),
    ) {

        LazyColumn {

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

                                if (day == null) {
                                    SpacerW1()
                                } else {

                                    VStack(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(bottom = 1.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {

                                        DividerBg(Modifier.padding(bottom = 6.dp))

                                        Text(
                                            text = day.title,
                                            color = c.white,
                                        )

                                        day.previews.forEach { preview ->
                                            Text(
                                                text = preview,
                                                modifier = Modifier
                                                    .padding(horizontal = 2.dp),
                                                color = c.textSecondary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Light,
                                                maxLines = 1,
                                                softWrap = false,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
