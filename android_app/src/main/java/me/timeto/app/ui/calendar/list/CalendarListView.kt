package me.timeto.app.ui.calendar.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.ui.H_PADDING_HALF
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.squircleShape
import me.timeto.app.ui.tasks.tab.TasksTabView__LIST_SECTION_PADDING
import me.timeto.app.ui.tasks.tab.TasksTabView__PADDING_END
import me.timeto.shared.vm.calendar.CalendarListVm

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
