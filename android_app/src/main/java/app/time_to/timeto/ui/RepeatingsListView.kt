package app.time_to.timeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.time_to.timeto.*
import app.time_to.timeto.R
import timeto.shared.vm.RepeatingsListVM
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RepeatingsListView() {

    val (_, state) = rememberVM { RepeatingsListVM() }

    val isAddRepeatingPresented = remember { mutableStateOf(false) }
    RepeatingFormSheet(
        isPresented = isAddRepeatingPresented,
        editedRepeating = null
    )

    LazyColumn(
        reverseLayout = true,
        contentPadding = PaddingValues(
            start = TAB_TASKS_PADDING_START,
            end = TAB_TASKS_PADDING_END,
            bottom = taskListSectionPadding,
            top = taskListSectionPadding
        ),
        modifier = Modifier.fillMaxHeight()
    ) {

        item {

            MyButton(
                text = "New Repeating Task",
                isEnabled = true,
                backgroundColor = c.blue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = taskListSectionPadding),
                extraPaddings = 0 to 3,
                fontSize = 16.sp
            ) {
                isAddRepeatingPresented.value = true
            }
        }

        itemsIndexed(state.repeatingsUI, key = { _, i -> i.repeating.id }) { index, repeatingUI ->
            val isEditPresented = remember { mutableStateOf(false) }
            RepeatingFormSheet(
                isPresented = isEditPresented,
                editedRepeating = repeatingUI.repeating
            )

            val isLast = index == state.repeatingsUI.size - 1

            // Remember that the list is reversed
            val clip = when {
                index == 0 && isLast -> MySquircleShape()
                index == 0 -> MySquircleShape(angles = listOf(false, false, true, true))
                isLast -> MySquircleShape(angles = listOf(true, true, false, false))
                else -> RoundedCornerShape(0.dp)
            }

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
                    isEditPresented.value = true
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
                        .clip(clip)
                        .background(c.background2),
                    contentAlignment = Alignment.BottomCenter
                ) {

                    val horizontalPadding = 18.dp

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding),
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

                        Text(
                            repeatingUI.listText,
                            modifier = Modifier
                                .padding(horizontal = horizontalPadding)
                                .padding(top = 2.dp)
                        )

                        val badgesHPadding = horizontalPadding - 2.dp
                        val daytimeText = repeatingUI.daytimeText
                        if (daytimeText != null) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = badgesHPadding)
                                    .padding(top = 6.dp, bottom = 0.dp)
                                    .clip(MySquircleShape(len = 50f))
                                    .background(c.blue)
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {

                                Icon(
                                    painterResource(id = R.drawable.sf_clock_medium_medium),
                                    contentDescription = "Daytime",
                                    modifier = Modifier
                                        .size(20.dp, 20.dp)
                                        .padding(3.dp),
                                    tint = c.white
                                )

                                Text(
                                    daytimeText,
                                    modifier = Modifier.padding(start = 1.dp, end = 3.dp),
                                    fontSize = 13.sp,
                                    color = c.white,
                                )
                            }
                        }

                        TriggersView__ListView(
                            triggers = repeatingUI.triggers,
                            withOnClick = true,
                            modifier = Modifier.padding(top = 6.dp),
                            contentPadding = PaddingValues(horizontal = badgesHPadding)
                        )
                    }

                    // Remember that the list is reversed
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
}
