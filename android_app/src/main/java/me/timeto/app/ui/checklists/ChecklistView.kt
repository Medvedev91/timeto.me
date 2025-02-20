package me.timeto.app.ui.checklists

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
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.home.HomeScreen__primaryFontSize
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.ui.checklists.ChecklistVm
import me.timeto.shared.ui.checklists.ChecklistStateUi

private val checklistItemMinHeight = HomeScreen__itemHeight

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
    topPadding: Dp,
    bottomPadding: Dp,
) {

    val (_, state) = rememberVm(checklistDb) {
        ChecklistVm(checklistDb)
    }

    val navigationFs = LocalNavigationFs.current

    VStack(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val completionState = state.stateUi

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
                contentPadding = PaddingValues(top = topPadding, bottom = bottomPadding),
            ) {

                state.itemsUi.forEach { itemUI ->

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
                                    id = if (itemUI.itemDb.isChecked)
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
                                text = itemUI.itemDb.text,
                                color = c.white,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .padding(start = 12.dp),
                                fontSize = HomeScreen__primaryFontSize,
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
                    .padding(start = 4.dp, top = topPadding)
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
                            navigationFs.push {
                                ChecklistItemsFormFs(
                                    checklistDb = state.checklistDb,
                                    onDelete = {
                                        onDelete()
                                    },
                                )
                            }
                        }
                        .padding(checklistMenuInnerIconPadding + 1.dp),
                )
            }
        }
    }
}
