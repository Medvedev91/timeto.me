package me.timeto.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.vm.ActivitiesListVM

private val emojiWidth = 56.dp
private val bgAnimateSpec: AnimationSpec<Color> = spring(stiffness = Spring.StiffnessMediumLow)

@Composable
fun ActivitiesListView(
    modifier: Modifier,
    onTaskStarted: () -> Unit,
) {

    val (_, state) = rememberVM { ActivitiesListVM() }

    Column(
        modifier = modifier
            .background(c.bg),
    ) {

        state.activitiesUI.forEach { activityUI ->

            val timerData = activityUI.data.timerData
            val isActive = timerData != null
            val bgAnimate = animateColorAsState(timerData?.color?.toColor() ?: c.bg, bgAnimateSpec)

            Box(
                modifier = Modifier
                    .background(bgAnimate.value)
                    .clickable {
                        Sheet.show { layer ->
                            ActivityTimerSheet(
                                layer = layer,
                                activity = activityUI.activity,
                                timerContext = null,
                                onStarted = {
                                    onTaskStarted()
                                },
                            )
                        }
                    },
                contentAlignment = Alignment.TopCenter,
            ) {

                HStack(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .padding(end = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    Text(
                        text = activityUI.activity.emoji,
                        style = TextStyle(
                            shadow = Shadow(
                                color = c.white,
                                blurRadius = 2f,
                            ),
                        ),
                        modifier = Modifier
                            .width(emojiWidth),
                        textAlign = TextAlign.Center,
                        fontSize = if (isActive) 20.sp else 22.sp,
                    )

                    HStack(
                        modifier = Modifier
                            .weight(1f),
                    ) {
                        Text(
                            text = activityUI.data.text,
                            modifier = Modifier
                                .weight(1f, fill = false),
                            color = if (isActive) c.white else c.text,
                            fontSize = 16.sp,
                            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        TriggersListIconsView(activityUI.data.textTriggers, 14.sp)
                    }

                    activityUI.timerHints.forEach { hintUI ->
                        Text(
                            text = hintUI.text,
                            modifier = Modifier
                                .padding(top = 1.dp)
                                .clip(roundedShape)
                                .clickable {
                                    hintUI.startInterval()
                                    onTaskStarted()
                                }
                                .padding(horizontal = 4.dp, vertical = 3.dp),
                            color = if (isActive) c.white else c.blue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                        )
                    }
                }

                DividerBg(
                    modifier = Modifier.padding(start = emojiWidth),
                    isVisible = activityUI.withTopDivider,
                )
            }
        }
    }
}
