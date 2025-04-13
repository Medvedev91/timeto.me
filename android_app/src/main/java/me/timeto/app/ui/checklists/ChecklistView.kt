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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.app.mics.Haptic
import me.timeto.app.ui.checklists.form.ChecklistFormItemFs
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.home.HomeScreen__primaryFontSize
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.ui.checklists.ChecklistVm
import me.timeto.shared.ui.checklists.ChecklistStateUi

private val checklistItemMinHeight = HomeScreen__itemHeight

private val itemStartPadding = 8.dp
private val checkboxSize = 18.dp
private val checklistMenuInnerIconPadding: Dp = (checklistItemMinHeight - checkboxSize) / 2
private val itemFontSize: TextUnit = HomeScreen__primaryFontSize

@Composable
fun ChecklistView(
    checklistDb: ChecklistDb,
    modifier: Modifier,
    scrollState: LazyListState,
    maxLines: Int,
    withAddButton: Boolean,
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
                contentPadding = PaddingValues(top = topPadding, bottom = bottomPadding),
            ) {

                state.itemsUi.forEach { itemUi ->

                    item {

                        HStack(
                            modifier = Modifier
                                .defaultMinSize(minHeight = checklistItemMinHeight)
                                .fillMaxWidth()
                                .clip(squircleShape)
                                .clickable {
                                    itemUi.toggle()
                                    Haptic.shot()
                                }
                                .padding(start = itemStartPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {

                            Icon(
                                painterResource(
                                    id = if (itemUi.itemDb.isChecked)
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
                                text = itemUi.itemDb.text,
                                color = c.white,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .padding(start = 12.dp),
                                fontSize = itemFontSize,
                                textAlign = TextAlign.Start,
                                maxLines = maxLines,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                if (withAddButton) {
                    item {
                        ZStack(
                            modifier = Modifier
                                .height(checklistItemMinHeight),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Text(
                                text = "New Item",
                                color = c.blue,
                                modifier = Modifier
                                    .clip(squircleShape)
                                    .clickable {
                                        navigationFs.push {
                                            ChecklistFormItemFs(
                                                checklistDb = checklistDb,
                                                checklistItemDb = null,
                                            )
                                        }
                                    }
                                    .padding(vertical = 4.dp)
                                    .padding(horizontal = itemStartPadding),
                                fontSize = itemFontSize,
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
                        Haptic.shot()
                    },
            ) {

                Icon(
                    painter = painterResource(
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
            }
        }
    }
}
