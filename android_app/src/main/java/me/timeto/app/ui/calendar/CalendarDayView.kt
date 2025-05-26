package me.timeto.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.HStack
import me.timeto.app.H_PADDING_HALF
import me.timeto.app.ui.VStack
import me.timeto.app.c
import me.timeto.app.onePx
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.Divider
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.calendar.list.CalendarListItemView
import me.timeto.app.ui.events.EventFormFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.shared.ui.calendar.CalendarDayVm

@Composable
fun CalendarDayView(
    unixDay: Int,
) {

    val navigationFs = LocalNavigationFs.current

    val (_, state) = rememberVm(unixDay) {
        CalendarDayVm(
            unixDay = unixDay,
        )
    }

    VStack(
        modifier = Modifier
            // Ignore swipe to action overflow
            .clipToBounds()
            .background(c.fg),
    ) {

        Divider(color = c.blue)

        HStack(
            modifier = Modifier
                .padding(horizontal = H_PADDING_HALF)
                .padding(top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                text = state.inNote,
                color = c.white,
                fontSize = 14.sp,
            )

            SpacerW1()

            Text(
                text = state.newEventText,
                modifier = Modifier
                    .clip(roundedShape)
                    .background(c.blue)
                    .clickable {
                        navigationFs.push {
                            EventFormFs(
                                initEventDb = null,
                                initText = null,
                                initTime = state.initTime,
                                onDone = {},
                            )
                        }
                    }
                    .padding(horizontal = 10.dp)
                    .padding(top = 2.dp + onePx, bottom = 2.dp),
                color = c.white,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        state.eventsUi.forEachIndexed { idx, eventUi ->
            key(eventUi.eventDb.id) {
                CalendarListItemView(
                    eventUi = eventUi,
                    withTopDivider = (idx > 0),
                    clip = RectangleShape,
                    modifier = Modifier,
                )
            }
        }
    }
}
