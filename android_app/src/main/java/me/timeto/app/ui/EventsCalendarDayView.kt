package me.timeto.app.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.vm.EventsCalendarDayVM

private val bgColor = c.fg
private val hPadding = 8.dp

@Composable
fun EventsCalendarDayView(
    unixDay: Int,
) {

    val (_, state) = rememberVM(unixDay) { EventsCalendarDayVM(unixDay) }

    VStack(
        modifier = Modifier
            // Ignore swipe to action overflow
            .clipToBounds(),
    ) {

        Divider(c.blue)

        HStack(
            modifier = Modifier
                .background(bgColor)
                .padding(horizontal = hPadding)
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
                text = "New Event",
                modifier = Modifier
                    .clip(roundedShape)
                    .background(c.blue)
                    .clickable {
                        EventFormSheet__show(
                            editedEvent = null,
                            defTime = state.formDefTime,
                        ) {}
                    }
                    .padding(horizontal = 8.dp)
                    .padding(top = 4.dp, bottom = 4.dp + halfDp),
                color = c.white,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        state.eventsUi.forEachIndexed { idx, eventUi ->
            key(eventUi.event.id) {
                EventsListEventView(
                    eventUi = eventUi,
                    bgColor = bgColor,
                    paddingStart = hPadding,
                    paddingEnd = hPadding,
                    withTopDivider = (idx > 0),
                )
            }
        }
    }
}
