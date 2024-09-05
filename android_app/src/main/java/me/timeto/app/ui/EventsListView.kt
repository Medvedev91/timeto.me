package me.timeto.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.vm.EventsListVM

@Composable
fun EventsListView(
    modifier: Modifier,
) {

    val (_, state) = rememberVm { EventsListVM() }

    LazyColumn(
        modifier = modifier,
        reverseLayout = true,
        contentPadding = PaddingValues(
            end = TasksView__PADDING_END,
            top = TasksView__LIST_SECTION_PADDING,
        ),
    ) {

        item {
            Box(
                modifier = Modifier
                    .padding(start = H_PADDING)
                    .fillMaxWidth()
                    .padding(vertical = TasksView__LIST_SECTION_PADDING),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    state.curTimeString,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W300,
                    color = c.textSecondary,
                )
            }
        }

        itemsIndexed(
            state.uiEvents,
            key = { _, eventUi -> eventUi.event.id },
        ) { idx, eventUi ->
            EventsListEventView(
                eventUi = eventUi,
                bgColor = c.bg,
                paddingStart = H_PADDING,
                paddingEnd = 0.dp,
                dividerColor = c.dividerBg,
                withTopDivider = (state.uiEvents.size - 1) != idx,
            )
        }
    }
}
