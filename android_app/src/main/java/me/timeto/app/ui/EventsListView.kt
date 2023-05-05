package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EventsListView() {

    val (_, state) = rememberVM { EventsListVM() }

    LazyColumn(
        reverseLayout = true,
        contentPadding = PaddingValues(
            end = TAB_TASKS_PADDING_END,
            top = taskListSectionPadding
        ),
        modifier = Modifier.fillMaxHeight()
    ) {

        val minHeight = 42.dp

        item {

            val saveBtnPadding = 4.dp

            Column {

                EventsHistoryView(
                    spaceAround = TAB_TASKS_PADDING_START + 1.dp,
                    paddingTop = taskListSectionPadding
                )

                Box(
                    Modifier
                        .padding(
                            start = TAB_TASKS_PADDING_START,
                            top = 16.dp,
                            bottom = taskListSectionPadding
                        )
                        .height(minHeight)
                        .clip(MySquircleShape())
                        .background(c.background2)
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
                            .clip(MySquircleShape(45f))
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
                    .padding(start = TAB_TASKS_PADDING_START)
                    .fillMaxWidth()
                    .padding(top = taskListSectionPadding),
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

        itemsIndexed(state.uiEvents, key = { _, uiEvent -> uiEvent.event.id }) { index, uiEvent ->
            val isLast = index == state.uiEvents.size - 1

            // Remember the list is reversed
            val clip = when {
                index == 0 && isLast -> MySquircleShape()
                index == 0 -> MySquircleShape(angles = listOf(false, false, true, true))
                isLast -> MySquircleShape(angles = listOf(true, true, false, false))
                else -> RoundedCornerShape(0.dp)
            }

            Box(
                modifier = Modifier
                    .padding(start = TAB_TASKS_PADDING_START)
                    .clip(clip)
                    .background(c.background2),
                contentAlignment = Alignment.BottomCenter
            ) {

                SwipeToAction(
                    isStartOrEnd = remember { mutableStateOf(null) },
                    startView = { SwipeToAction__StartView("Edit", c.blue) },
                    endView = { state ->
                        SwipeToAction__DeleteView(
                            state = state,
                            note = uiEvent.event.text,
                            deletionConfirmationNote = uiEvent.deletionNote,
                        ) {
                            vibrateLong()
                            uiEvent.delete()
                        }
                    },
                    onStart = {
                        EventFormSheet__show(editedEvent = uiEvent.event) {}
                        false
                    },
                    onEnd = {
                        true
                    },
                    toVibrateStartEnd = listOf(true, false),
                ) {
                    val horizontalPadding = 18.dp

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(c.background2)
                            .padding(vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = horizontalPadding)
                        ) {
                            Text(
                                uiEvent.dateString,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W300,
                                color = c.textSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                uiEvent.dayLeftString,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W300,
                                color = c.textSecondary,
                                modifier = Modifier
                            )
                        }

                        Text(
                            uiEvent.listText,
                            modifier = Modifier
                                .padding(horizontal = horizontalPadding),
                            color = c.text,
                        )

                        TextFeaturesTriggersView(
                            triggers = uiEvent.textFeatures.triggers,
                            modifier = Modifier.padding(top = 6.dp),
                            contentPadding = PaddingValues(horizontal = horizontalPadding - 2.dp)
                        )
                    }
                }

                // Remember the list is reversed
                if (index > 0)
                    Divider(
                        color = c.dividerBackground2,
                        modifier = Modifier
                            .padding(start = 18.dp),
                        thickness = 0.7.dp
                    )
            }
        }
    }
}
