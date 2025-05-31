package me.timeto.app.ui.calendar.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING_HALF
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.TriggersIconsView
import me.timeto.app.ui.events.EventFormFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.shared.vm.calendar.CalendarListVm

@Composable
fun CalendarListItemView(
    eventUi: CalendarListVm.EventUi,
    withTopDivider: Boolean,
    clip: Shape,
    modifier: Modifier,
) {

    val navigationFs = LocalNavigationFs.current

    ZStack(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {

        VStack(
            modifier = Modifier
                .fillMaxWidth()
                .clip(clip)
                .clickable {
                    navigationFs.push {
                        EventFormFs(
                            initEventDb = eventUi.eventDb,
                            initText = null,
                            initTime = null,
                            onDone = {},
                        )
                    }
                }
                .padding(horizontal = H_PADDING_HALF)
                .padding(vertical = 10.dp),
        ) {

            HStack {

                Text(
                    text = eventUi.dateString,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W300,
                    color = c.secondaryText,
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = eventUi.dayLeftString,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W300,
                    color = c.secondaryText,
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

                TriggersIconsView(
                    checklistsDb = eventUi.textFeatures.checklistsDb,
                    shortcutsDb = eventUi.textFeatures.shortcutsDb,
                )
            }
        }

        if (withTopDivider) {
            Divider(
                modifier = Modifier
                    .padding(horizontal = H_PADDING_HALF),
            )
        }
    }
}
