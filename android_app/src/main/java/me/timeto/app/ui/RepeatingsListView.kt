package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.vm.RepeatingsListVM

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RepeatingsListView() {

    val (_, state) = rememberVM { RepeatingsListVM() }

    LazyColumn(
        reverseLayout = true,
        contentPadding = PaddingValues(
            end = TasksView__PADDING_END,
            bottom = TasksView__LIST_SECTION_PADDING,
            top = TasksView__LIST_SECTION_PADDING
        ),
        modifier = Modifier.fillMaxHeight()
    ) {

        item {

            Text(
                "New Repeating Task",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = H_PADDING - 2.dp)
                    .padding(top = TasksView__LIST_SECTION_PADDING)
                    .clip(squircleShape)
                    .background(c.blue)
                    .clickable {
                        Sheet.show { layer ->
                            RepeatingFormSheet(
                                layer = layer,
                                editedRepeating = null
                            )
                        }
                    }
                    .padding(
                        vertical = 10.dp
                    ),
                color = c.white,
                fontWeight = FontWeight.W500,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        itemsIndexed(
            state.repeatingsUI,
            key = { _, i -> i.repeating.id }
        ) { index, repeatingUI ->

            SwipeToAction(
                isStartOrEnd = remember { mutableStateOf(null) },
                startView = { SwipeToAction__StartView("Edit", c.blue) },
                endView = { state ->
                    SwipeToAction__DeleteView(
                        state = state,
                        note = repeatingUI.listText,
                        deletionConfirmationNote = repeatingUI.deletionNote,
                    ) {
                        vibrateLong()
                        repeatingUI.delete()
                    }
                },
                onStart = {
                    Sheet.show { layer ->
                        RepeatingFormSheet(
                            layer = layer,
                            editedRepeating = repeatingUI.repeating,
                        )
                    }
                    false
                },
                onEnd = {
                    true
                },
                toVibrateStartEnd = listOf(true, false),
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.bg)
                        .padding(start = H_PADDING),
                    contentAlignment = Alignment.BottomCenter
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(
                                repeatingUI.dayLeftString,
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.W300,
                                color = c.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            Text(
                                repeatingUI.dayRightString,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.W300,
                                color = c.textSecondary,
                                maxLines = 1,
                            )
                        }

                        HStack(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                repeatingUI.listText,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .weight(1f),
                                color = c.text,
                            )
                            TriggersListIconsView(repeatingUI.textFeatures.triggers, 14.sp)

                            if (repeatingUI.isImportant) {
                                Icon(
                                    painterResource(R.drawable.sf_flag_fill_medium_regular),
                                    contentDescription = "Important",
                                    tint = c.red,
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .offset(y = 1.dp)
                                        .size(16.dp),
                                )
                            }
                        }
                    }

                    // Remember that the list is reversed
                    if (index > 0)
                        DividerBg()
                }
            }
        }
    }
}
