package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.vm.EventsListVM

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EventsListEventView(
    eventUi: EventsListVM.EventUi,
    bgColor: Color,
    paddingStart: Dp,
    paddingEnd: Dp,
    withTopDivider: Boolean,
) {

    SwipeToAction(
        isStartOrEnd = remember { mutableStateOf(null) },
        startView = { SwipeToAction__StartView("Edit", c.blue) },
        endView = { state ->
            SwipeToAction__DeleteView(
                state = state,
                note = eventUi.event.text,
                deletionConfirmationNote = eventUi.deletionNote,
            ) {
                vibrateLong()
                eventUi.delete()
            }
        },
        onStart = {
            EventFormSheet__show(editedEvent = eventUi.event) {}
            false
        },
        onEnd = {
            true
        },
        toVibrateStartEnd = listOf(true, false),
    ) {

        ZStack(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(start = paddingStart, end = paddingEnd),
            contentAlignment = Alignment.BottomCenter,
        ) {

            VStack(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
            ) {

                HStack {

                    Text(
                        eventUi.dateString,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W300,
                        color = c.textSecondary,
                        modifier = Modifier.weight(1f),
                    )

                    Text(
                        eventUi.dayLeftString,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W300,
                        color = c.textSecondary,
                    )
                }

                HStack(
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    Text(
                        eventUi.listText,
                        modifier = Modifier
                            .weight(1f),
                        color = c.text,
                    )

                    TriggersListIconsView(eventUi.textFeatures.triggers, 14.sp)
                }
            }

            if (withTopDivider)
                DividerBg()
        }
    }
}
