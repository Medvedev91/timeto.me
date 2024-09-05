package me.timeto.app.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.vm.ActivityTimerSheetVM
import me.timeto.shared.vm.ActivitiesTimerSheetVM

fun ActivitiesTimerSheet__show(
    timerContext: ActivityTimerSheetVM.TimerContext?,
    withMenu: Boolean,
) {
    Sheet.show { layer ->
        ActivitiesTimerSheet(
            layerActivitiesSheet = layer,
            timerContext = timerContext,
            withMenu = withMenu,
        )
    }
}

private val activityItemEmojiHPadding = 8.dp
private val activityItemEmojiWidth = 32.dp
private val activityItemPaddingStart = activityItemEmojiWidth + (activityItemEmojiHPadding * 2)

private val listItemHeight = 42.dp
private val topContentPadding = 2.dp
private val bottomContentPadding = 4.dp

private val secondaryFontSize = 14.sp
private val secondaryFontWeight = FontWeight.Light
private val timerHintHPadding = 5.dp
private val listEngPadding = 8.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActivitiesTimerSheet(
    layerActivitiesSheet: WrapperView.Layer,
    timerContext: ActivityTimerSheetVM.TimerContext?,
    withMenu: Boolean,
) {

    val (_, state) = rememberVm(timerContext) { ActivitiesTimerSheetVM(timerContext) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val contentHeight = (listItemHeight * state.allActivities.size) +
                        (if (withMenu) listItemHeight else 0.dp) + // Buttons
                        topContentPadding +
                        bottomContentPadding

    LazyColumn(
        modifier = Modifier
            .background(c.sheetBg)
            .navigationBarsPadding()
            .height(contentHeight.limitMax(screenHeight - 60.dp))
            .fillMaxWidth(),
        contentPadding = PaddingValues(top = topContentPadding, bottom = bottomContentPadding)
    ) {

        items(state.allActivities) { activityUI ->

            val activity = activityUI.activity

            Box(
                contentAlignment = Alignment.BottomCenter, // for divider
            ) {

                Row(
                    modifier = Modifier
                        .height(listItemHeight)
                        .combinedClickable(
                            onClick = {
                                ActivityTimerSheet__show(
                                    activity = activity,
                                    timerContext = timerContext,
                                ) {
                                    layerActivitiesSheet.close()
                                }
                            },
                            onLongClick = {
                                Sheet.show { layer ->
                                    ActivityFormSheet(
                                        layer = layer,
                                        activity = activityUI.activity,
                                    )
                                }
                            },
                        )
                        .padding(end = listEngPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    Text(
                        text = activity.emoji,
                        modifier = Modifier
                            .padding(horizontal = activityItemEmojiHPadding)
                            .width(activityItemEmojiWidth),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                    )

                    Text(
                        activityUI.listText,
                        modifier = Modifier
                            .weight(1f),
                        color = c.text,
                        textAlign = TextAlign.Start,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )

                    TimerHintsView(
                        modifier = Modifier,
                        timerHintsUI = activityUI.timerHints,
                        hintHPadding = timerHintHPadding,
                        fontSize = secondaryFontSize,
                        fontWeight = secondaryFontWeight,
                        onStart = {
                            layerActivitiesSheet.close()
                        }
                    )
                }

                SheetDividerBg(
                    modifier = Modifier
                        .padding(start = activityItemPaddingStart),
                    isVisible = state.allActivities.lastOrNull() != activityUI,
                )

                if (activityUI.isActive)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = (-4).dp)
                            .height(listItemHeight - 2.dp)
                            .clip(roundedShape)
                            .background(c.blue)
                            .width(8.dp)
                    ) {}
            }
        }

        if (withMenu) {

            item {

                HStack(
                    modifier = Modifier
                        .height(listItemHeight)
                        .padding(start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    ChartHistoryButton(
                        text = "Summary",
                        iconResId = R.drawable.sf_chart_pie_small_thin,
                        iconSize = 17.dp,
                    ) {
                        Sheet.show(
                            topPadding = 4.dp,
                        ) { layer ->
                            SummarySheet(layer)
                        }
                    }

                    ChartHistoryButton(
                        "History",
                        iconResId = R.drawable.sf_list_bullet_rectangle_small_thin,
                        iconSize = 19.dp,
                        extraIconPadding = onePx,
                    ) {
                        Dialog.show(
                            modifier = Modifier.fillMaxHeight(0.95f),
                        ) { layer ->
                            HistoryDialogView(layer::close)
                        }
                    }

                    SpacerW1()

                    Text(
                        text = "Edit",
                        modifier = Modifier
                            .padding(end = listEngPadding)
                            .clip(squircleShape)
                            .clickable {
                                Sheet.show { layer ->
                                    EditActivitiesSheet(layer = layer)
                                }
                            }
                            .padding(horizontal = timerHintHPadding, vertical = 4.dp),
                        color = c.blue,
                        fontSize = secondaryFontSize,
                        fontWeight = secondaryFontWeight,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartHistoryButton(
    text: String,
    @DrawableRes iconResId: Int,
    iconSize: Dp,
    extraIconPadding: Dp = 0.dp,
    onClick: () -> Unit,
) {
    HStack(
        modifier = Modifier
            .clip(squircleShape)
            .clickable {
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Icon(
            painterResource(iconResId),
            contentDescription = text,
            tint = c.blue,
            modifier = Modifier
                .padding(end = 5.dp + extraIconPadding)
                .size(iconSize)
        )

        Text(
            text = text,
            color = c.blue,
            fontSize = secondaryFontSize,
            fontWeight = secondaryFontWeight,
        )
    }
}
