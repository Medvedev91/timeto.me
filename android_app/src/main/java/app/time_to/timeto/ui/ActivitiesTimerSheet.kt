package app.time_to.timeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.time_to.timeto.*
import timeto.shared.db.TaskModel
import timeto.shared.launchEx
import timeto.shared.vm.ActivityTimerSheetVM
import timeto.shared.vm.ActivitiesTimerSheetVM

@Composable
fun ActivitiesTimerSheet(
    layerTaskSheet: WrapperView.Layer,
    task: TaskModel,
    onTaskStarted: () -> Unit,
) {
    val scopeTaskSheet = rememberCoroutineScope()

    val activityItemHeight = 42.dp
    val topContentPadding = 2.dp
    val bottomContentPadding = 20.dp

    val (_, state) = rememberVM(task) { ActivitiesTimerSheetVM(task) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    LazyColumn(
        modifier = Modifier
            .background(c.background2)
            .navigationBarsPadding()
            .height((activityItemHeight * state.allActivities.size + topContentPadding + bottomContentPadding).min(screenHeight - 60.dp))
            .fillMaxWidth(),
        contentPadding = PaddingValues(top = topContentPadding, bottom = bottomContentPadding)
    ) {

        items(state.allActivities) { activityUI ->

            val activity = activityUI.activity

            val emojiHPadding = 8.dp
            val emojiWidth = 30.dp
            val startPadding = emojiWidth + (emojiHPadding * 2)

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
                                    timerContext = ActivityTimerSheetVM.TimerContext.Task(task)
                                ) {
                                    scopeTaskSheet.launchEx {
                                        onTaskStarted()
                                        layerTaskSheet.close() // At the end to keep "scopeTaskSheet"
                                    }
                                }
                            }
                        }
                        .padding(start = 2.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    Text(
                        text = activity.emoji,
                        modifier = Modifier
                            .padding(horizontal = emojiHPadding)
                            .width(emojiWidth),
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
                        val hPadding = if (isPrimary) 6.dp else 5.dp
                        Text(
                            text = hintUI.text,
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .align(Alignment.CenterVertically)
                                .background(if (isPrimary) c.blue else c.transparent)
                                .clickable {
                                    hintUI.startInterval {
                                        scopeTaskSheet.launchEx {
                                            onTaskStarted()
                                            layerTaskSheet.close() // At the end to keep "scopeTaskSheet"
                                        }
                                    }
                                }
                                .padding(start = hPadding, end = hPadding, top = 3.dp, bottom = 4.dp),
                            color = if (isPrimary) c.white else c.blue,
                            fontSize = if (isPrimary) 13.sp else 14.sp,
                            fontWeight = if (isPrimary) FontWeight.W500 else FontWeight.W300,
                        )
                    }
                }

                Divider(
                    color = c.dividerBackground2,
                    modifier = Modifier
                        .padding(start = startPadding),
                    thickness = 0.5.dp
                )
            }
        }
    }
}
