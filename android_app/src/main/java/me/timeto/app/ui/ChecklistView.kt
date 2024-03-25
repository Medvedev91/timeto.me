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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.vm.ChecklistVM
import me.timeto.shared.vm.ui.ChecklistStateUI

@Composable
fun ChecklistView(
    checklistDb: ChecklistDb,
    modifier: Modifier,
    scrollState: LazyListState,
    withNavigationPadding: Boolean = false,
    bottomPadding: Dp = 0.dp,
) {

    val (_, state) = rememberVM { ChecklistVM(checklistDb) }

    VStack(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val itemStartPadding = 8.dp
        val checkboxSize = 18.dp
        val checklistItemMinHeight = 44.dp

        val completionState = state.checklistUI.stateUI
        val checklistMenuInnerIconPadding = (checklistItemMinHeight - checkboxSize) / 2

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
                                    .padding(start = 14.dp),
                                fontSize = HomeView__PRIMARY_FONT_SIZE,
                                textAlign = TextAlign.Start,
                            )
                        }
                    }
                }

                if (withNavigationPadding) {
                    item {
                        ZStack(Modifier.navigationBarsPadding())
                    }
                }
            }

            HStack(
                modifier = Modifier
                    .height(IntrinsicSize.Max)
            ) {

                Column {

                    Icon(
                        painterResource(
                            id = when (completionState) {
                                is ChecklistStateUI.Completed -> R.drawable.sf_checkmark_square_fill_medium_regular
                                is ChecklistStateUI.Empty -> R.drawable.sf_square_medium_regular
                                is ChecklistStateUI.Partial -> R.drawable.sf_minus_square_fill_medium_medium
                            }
                        ),
                        contentDescription = completionState.actionDesc,
                        tint = c.white,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(checklistItemMinHeight)
                            .clip(roundedShape)
                            .clickable {
                                completionState.onClick()
                            }
                            .padding(checklistMenuInnerIconPadding),
                    )
                }
            }
        }
    }
}
