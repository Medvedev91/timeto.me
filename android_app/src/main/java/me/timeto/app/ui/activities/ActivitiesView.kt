package me.timeto.app.ui.activities

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack
import me.timeto.app.ZStack
import me.timeto.app.c
import me.timeto.app.R
import me.timeto.app.rememberVm
import me.timeto.app.roundedShape
import me.timeto.app.squircleShape
import me.timeto.app.ui.ActivityTimerSheet__show
import me.timeto.app.ui.Divider
import me.timeto.app.ui.activities.form.ActivityFormFs
import me.timeto.app.ui.activities.form.ActivityFormTimerHintsFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.ui.activities.ActivitiesVm

val ActivitiesView__listItemHeight = 42.dp
val ActivitiesView__timerHintHPadding = 5.dp
val ActivitiesView__listEndPadding = 8.dp

private val activityItemEmojiHPadding = 8.dp
private val activityItemEmojiWidth = 32.dp
private val activityItemPaddingStart = activityItemEmojiWidth + (activityItemEmojiHPadding * 2)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ActivitiesView(
    modifier: Modifier,
) {

    val navigationFs = LocalNavigationFs.current

    val (vm, state) = rememberVm {
        ActivitiesVm()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        reverseLayout = true,
    ) {

        val activitiesUi = state.activitiesUi.reversed()
        activitiesUi.forEach { activityUi ->

            val activityDb: ActivityDb = activityUi.activityDb

            item {

                ZStack(
                    contentAlignment = Alignment.BottomCenter, // for divider
                ) {

                    HStack(
                        modifier = Modifier
                            .height(ActivitiesView__listItemHeight)
                            .combinedClickable(
                                onClick = {
                                    ActivityTimerSheet__show(
                                        activity = activityDb,
                                        timerContext = null,
                                        onStarted = {},
                                    )
                                },
                                onLongClick = {
                                    navigationFs.push {
                                        ActivityFormFs(
                                            initActivityDb = activityDb,
                                        )
                                    }
                                },
                            )
                            .padding(end = ActivitiesView__listEndPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        Text(
                            text = activityDb.emoji,
                            modifier = Modifier
                                .padding(horizontal = activityItemEmojiHPadding)
                                .width(activityItemEmojiWidth),
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                        )

                        Text(
                            text = activityUi.text,
                            modifier = Modifier
                                .weight(1f),
                            color = c.text,
                            textAlign = TextAlign.Start,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )

                        val timerHintsUi = activityUi.timerHintsUi
                        if (timerHintsUi.isNotEmpty()) {
                            HStack(
                                modifier = Modifier
                                    .padding(end = 2.dp),
                            ) {
                                timerHintsUi.forEach { timerHintUi ->
                                    Text(
                                        text = timerHintUi.title,
                                        modifier = Modifier
                                            .clip(roundedShape)
                                            .clickable {
                                                timerHintUi.onTap()
                                            }
                                            .padding(horizontal = ActivitiesView__timerHintHPadding),
                                        color = c.blue,
                                    )
                                }
                            }
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.sf_plus_medium_regular),
                                contentDescription = "New Timer Hint",
                                tint = c.textSecondary,
                                modifier = Modifier
                                    .clip(roundedShape)
                                    .clickable {
                                        navigationFs.push {
                                            ActivityFormTimerHintsFs(
                                                initTimerHints = activityDb.timerHints,
                                                onDone = { newTimerHints ->
                                                    vm.updateTimerHints(
                                                        activityDb = activityDb,
                                                        newTimerHints = newTimerHints,
                                                    )
                                                }
                                            )
                                        }
                                    }
                                    .padding(8.dp)
                                    .size(13.dp),
                            )
                        }
                    }

                    if (activitiesUi.first() != activityUi) {
                        Divider(Modifier.padding(start = activityItemPaddingStart))
                    }

                    if (activityUi.isActive) {
                        ZStack(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .offset(x = (-4).dp)
                                .height(ActivitiesView__listItemHeight - 2.dp)
                                .clip(roundedShape)
                                .background(c.blue)
                                .width(8.dp)
                        )
                    }
                }
            }
        }
    }
}
