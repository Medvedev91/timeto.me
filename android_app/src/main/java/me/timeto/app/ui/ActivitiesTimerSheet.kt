package me.timeto.app.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
) {
    Sheet.show { layer ->
        ActivitiesTimerSheet(
            layerActivitiesSheet = layer,
            timerContext = timerContext,
        )
    }
}

private val activityItemEmojiHPadding = 8.dp
private val activityItemEmojiWidth = 32.dp
private val activityItemPaddingStart = activityItemEmojiWidth + (activityItemEmojiHPadding * 2)

private val secondaryFontSize = 14.sp
private val secondaryFontWeight = FontWeight.Light
private val timerHintHPadding = 5.dp
private val listEngPadding = 8.dp

@Composable
private fun ActivitiesTimerSheet(
    layerActivitiesSheet: WrapperView.Layer,
    timerContext: ActivityTimerSheetVM.TimerContext?,
) {
    val activityItemHeight = 42.dp
    val topContentPadding = 2.dp
    val bottomContentPadding = 20.dp

    val (_, state) = rememberVM(timerContext) { ActivitiesTimerSheetVM(timerContext) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    LazyColumn(
        modifier = Modifier
            .background(c.sheetBg)
            .navigationBarsPadding()
            .height((activityItemHeight * state.allActivities.size + topContentPadding + bottomContentPadding).limitMax(screenHeight - 60.dp))
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
                        .height(activityItemHeight)
                        .clickable {
                            Sheet.show { layerTimer ->
                                ActivityTimerSheet(
                                    layer = layerTimer,
                                    activity = activity,
                                    timerContext = timerContext,
                                ) {
                                    layerActivitiesSheet.close()
                                }
                            }
                        }
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

                    activityUI.timerHints.forEach { hintUI ->
                        val isPrimary = hintUI.isPrimary
                        val hPadding = if (isPrimary) 6.dp else timerHintHPadding
                        Text(
                            text = hintUI.text,
                            modifier = Modifier
                                .clip(roundedShape)
                                .align(Alignment.CenterVertically)
                                .background(if (isPrimary) c.blue else c.transparent)
                                .clickable {
                                    hintUI.startInterval {
                                        layerActivitiesSheet.close()
                                    }
                                }
                                .padding(start = hPadding, end = hPadding, top = 3.dp, bottom = 4.dp),
                            color = if (isPrimary) c.white else c.blue,
                            fontSize = if (isPrimary) 13.sp else secondaryFontSize,
                            fontWeight = if (isPrimary) FontWeight.W500 else secondaryFontWeight,
                        )
                    }
                }

                DividerSheetBg(
                    modifier = Modifier
                        .padding(start = activityItemPaddingStart),
                    isVisible = state.allActivities.lastOrNull() != activityUI,
                )
            }
        }

        item {

            HStack(
                modifier = Modifier
                    .padding(top = 8.dp, start = 8.dp),
            ) {

                ChartHistoryButton(
                    text = "Chart",
                    iconResId = R.drawable.sf_chart_pie_small_thin,
                    iconSize = 17.dp,
                ) {
                    Dialog.show(
                        modifier = Modifier.fillMaxHeight(0.95f),
                    ) { layer ->
                        ChartDialogView(layer::close)
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
