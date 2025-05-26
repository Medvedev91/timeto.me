package me.timeto.app.ui.calendar.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.timeto.app.H_PADDING
import me.timeto.app.H_PADDING_HALF
import me.timeto.app.ui.ZStack
import me.timeto.app.c
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.squircleShape
import me.timeto.app.ui.tasks.tab.TasksTabView__LIST_SECTION_PADDING
import me.timeto.app.ui.tasks.tab.TasksTabView__PADDING_END
import me.timeto.shared.ui.calendar.CalendarListVm

@Composable
fun CalendarListView(
    modifier: Modifier,
) {

    val (_, state) = rememberVm {
        CalendarListVm()
    }

    LazyColumn(
        modifier = modifier,
        reverseLayout = true,
        contentPadding = PaddingValues(
            end = TasksTabView__PADDING_END - H_PADDING_HALF,
            top = TasksTabView__LIST_SECTION_PADDING,
        ),
    ) {

        item {
            ZStack(
                modifier = Modifier
                    .padding(start = H_PADDING)
                    .fillMaxWidth()
                    .padding(vertical = TasksTabView__LIST_SECTION_PADDING),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    state.curTimeString,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W300,
                    color = c.textSecondary,
                )
            }
        }

        state.eventsUi.forEachIndexed { idx, eventUi ->
            item(key = eventUi.eventDb.id) {
                CalendarListItemView(
                    eventUi = eventUi,
                    withTopDivider = (state.eventsUi.size - 1) != idx,
                    clip = squircleShape,
                    modifier = Modifier
                        .padding(start = H_PADDING_HALF),
                )
            }
        }
    }
}
