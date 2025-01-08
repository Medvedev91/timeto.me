package me.timeto.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.vm.Checklist.ChecklistVm
import me.timeto.shared.vm.Checklist.ChecklistStateUi

private val checklistItemMinHeight = HomeView__MTG_ITEM_HEIGHT

private val itemStartPadding = 8.dp
private val checkboxSize = 18.dp
private val checklistMenuInnerIconPadding = (checklistItemMinHeight - checkboxSize) / 2

@Composable
fun ChecklistView(
    checklistDb: ChecklistDb,
    modifier: Modifier,
    scrollState: LazyListState,
    onDelete: () -> Unit,
    maxLines: Int,
    bottomPadding: Dp = 0.dp,
) {

    val (_, state) = rememberVm(checklistDb) {
        ChecklistVm(checklistDb)
    }

    VStack(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val completionState = state.checklistUI.stateUI

        HStack(
            modifier = Modifier
                .padding(
                    start = H_PADDING - itemStartPadding,
                    end = H_PADDING - checklistMenuInnerIconPadding,
                )
                .weight(1f),
        ) {

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = scrollState,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = bottomPadding),
            ) {

                state.checklistUI.itemsUI.forEach { itemUI ->

                    item {

                        HStack(
                            modifier = Modifier
                                .defaultMinSize(minHeight = checklistItemMinHeight)
                                .fillMaxWidth()
                                .clip(squircleShape)
                                .clickable {
                                    itemUI.toggle()
                                }
                                .padding(start = itemStartPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {

                            Icon(
                                painterResource(
                                    id = if (itemUI.item.isChecked)
                                        R.drawable.sf_checkmark_square_fill_medium_regular
                                    else
                                        R.drawable.sf_square_medium_regular
                                ),
                                contentDescription = "Checkbox",
                                tint = c.white,
                                modifier = Modifier
                                    .size(checkboxSize),
                            )

                            Text(
                                text = itemUI.item.text,
                                color = c.white,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .padding(start = 12.dp),
                                fontSize = HomeView__PRIMARY_FONT_SIZE,
                                textAlign = TextAlign.Start,
                                maxLines = maxLines,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            VStack(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 4.dp)
                    .clip(squircleShape)
                    .clickable {
                        completionState.onClick()
                    },
            ) {

                Icon(
                    painterResource(
                        id = when (completionState) {
                            is ChecklistStateUi.Completed -> R.drawable.sf_checkmark_square_fill_medium_regular
                            is ChecklistStateUi.Empty -> R.drawable.sf_square_medium_regular
                            is ChecklistStateUi.Partial -> R.drawable.sf_minus_square_fill_medium_medium
                        }
                    ),
                    contentDescription = completionState.actionDesc,
                    tint = c.white,
                    modifier = Modifier
                        .size(checklistItemMinHeight)
                        .padding(checklistMenuInnerIconPadding),
                )

                Icon(
                    painterResource(R.drawable.sf_pencil_medium_medium),
                    contentDescription = "Edit Checklist",
                    tint = c.white,
                    modifier = Modifier
                        .size(checklistItemMinHeight)
                        .clip(roundedShape)
                        .clickable {
                            Sheet.show { layer ->
                                ChecklistFormSheet(
                                    layer = layer,
                                    checklistDb = state.checklistUI.checklistDb,
                                    onDelete = { onDelete() },
                                )
                            }
                        }
                        .padding(checklistMenuInnerIconPadding + 1.dp),
                )
            }
        }
    }
}
