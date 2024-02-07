package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.vm.EventsListVM

private val bgColor = c.bg

@Composable
fun EventsListView(
    modifier: Modifier,
) {

    val (_, state) = rememberVM { EventsListVM() }

    LazyColumn(
        modifier = modifier,
        reverseLayout = true,
        contentPadding = PaddingValues(
            end = TasksView__PADDING_END,
            top = TasksView__LIST_SECTION_PADDING,
        ),
    ) {

        val minHeight = 42.dp

        item {

            val saveBtnPadding = 4.dp

            Column {

                EventTemplatesView(
                    spaceAround = H_PADDING - 2.dp,
                    paddingTop = TasksView__LIST_SECTION_PADDING,
                )

                Box(
                    Modifier
                        .padding(
                            start = H_PADDING - 2.dp,
                            top = 16.dp,
                            bottom = TasksView__LIST_SECTION_PADDING,
                        )
                        .height(minHeight)
                        .border(onePx, c.dividerBg, TasksView__INPUT_SHAPE)
                        .clip(TasksView__INPUT_SHAPE)
                        .background(bgColor)
                        .clickable {
                            EventFormSheet__show(editedEvent = null) {}
                        }
                        .padding(vertical = saveBtnPadding),
                ) {

                    Text(
                        "Event",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = MyListView.PADDING_INNER_HORIZONTAL)
                            .align(Alignment.CenterStart),
                        color = c.textSecondary.copy(alpha = 0.4f)
                    )

                    Text(
                        "DATE",
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(end = saveBtnPadding)
                            .clip(squircleShape)
                            .background(c.blue.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp)
                            .align(Alignment.CenterEnd)
                            .wrapContentSize(), // vertical center
                        color = c.white,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W600,
                    )
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .padding(start = H_PADDING)
                    .fillMaxWidth()
                    .padding(top = TasksView__LIST_SECTION_PADDING),
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
        ) { index, eventUi ->
            EventsListEventView(
                eventUi = eventUi,
                bgColor = bgColor,
                paddingStart = H_PADDING,
                withTopDivider = (index > 0),
            )
        }
    }
}
