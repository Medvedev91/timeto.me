package me.timeto.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.vm.TabTimerVM

private val timerButtonsHeight = 26.dp

private val emojiWidth = 52.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TabTimerView() {

    val (vm, state) = rememberVM { TabTimerVM() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg),
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            LazyColumn(
                contentPadding = PaddingValues(vertical = 48.dp),
            ) {

                val activitiesUI = state.activitiesUI
                itemsIndexed(
                    activitiesUI,
                    key = { _, i -> i.activity.id }
                ) { _, uiActivity ->

                    val timerData = uiActivity.data.timerData
                    val isActive = timerData != null
                    val bgAnimate = animateColorAsState(
                        timerData?.color?.toColor() ?: c.bg,
                        spring(stiffness = Spring.StiffnessMediumLow)
                    )

                    SwipeToAction(
                        isStartOrEnd = remember { mutableStateOf(null) },
                        modifier = Modifier,
                        ignoreOneAction = remember { mutableStateOf(false) },
                        startView = {
                            SwipeToAction__StartView(
                                text = "Edit",
                                bgColor = c.blue
                            )
                        },
                        endView = { state ->
                            SwipeToAction__DeleteView(
                                state = state,
                                note = uiActivity.deletionHint,
                                deletionConfirmationNote = uiActivity.deletionConfirmation,
                            ) {
                                uiActivity.delete()
                            }
                        },
                        onStart = {
                            Sheet.show { layer ->
                                ActivityFormSheet(
                                    layer = layer,
                                    editedActivity = uiActivity.activity,
                                )
                            }
                            false
                        },
                        onEnd = {
                            true
                        },
                        toVibrateStartEnd = listOf(true, false),
                    ) {

                        Box(
                            modifier = Modifier
                                .background(bgAnimate.value)
                                .clickable {
                                    Sheet.show { layer ->
                                        ActivityTimerSheet(
                                            layer = layer,
                                            activity = uiActivity.activity,
                                            timerContext = null,
                                        )
                                    }
                                },
                            contentAlignment = Alignment.TopCenter,
                        ) {

                            Column(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 50.dp)
                                    .padding(top = 8.dp, bottom = 8.dp),
                                verticalArrangement = Arrangement.Center,
                            ) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {

                                    Text(
                                        text = uiActivity.activity.emoji,
                                        modifier = Modifier
                                            .width(emojiWidth),
                                        textAlign = TextAlign.Center,
                                        fontSize = if (isActive) 20.sp else 22.sp,
                                    )

                                    VStack(
                                        modifier = Modifier
                                            .weight(1f),
                                    ) {
                                        HStack {
                                            Text(
                                                text = uiActivity.data.text,
                                                color = if (isActive) c.white else c.text,
                                                fontSize = 16.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            TriggersListIconsView(
                                                triggers = uiActivity.data.textTriggers,
                                                fontSize = 15.sp,
                                            )
                                        }

                                        val listNote = uiActivity.data.note
                                        if (listNote != null)
                                            HStack(
                                                modifier = Modifier
                                                    .offset(y = (-2).dp)
                                            ) {
                                                Text(
                                                    text = listNote,
                                                    color = c.white,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Light,
                                                )
                                                TriggersListIconsView(
                                                    triggers = uiActivity.data.noteTriggers,
                                                    fontSize = 12.sp,
                                                )
                                            }
                                    }

                                    uiActivity.timerHints.forEach { hintUI ->
                                        Text(
                                            text = hintUI.text,
                                            modifier = Modifier
                                                .padding(top = 1.dp)
                                                .clip(roundedShape)
                                                .clickable {
                                                    hintUI.startInterval()
                                                }
                                                .padding(horizontal = 4.dp, vertical = 3.dp),
                                            color = if (isActive) c.white else c.blue,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Light,
                                        )
                                    }
                                }

                                if (timerData != null) {

                                    ZStack(
                                        modifier = Modifier
                                            .padding(top = 10.dp, bottom = 2.dp, start = 12.dp, end = 10.dp)
                                            .fillMaxWidth(),
                                    ) {

                                        val timerDataTitleLen = timerData.title.length
                                        val timerTitleFontSize = when {
                                            timerDataTitleLen <= 5 -> 29.sp
                                            timerDataTitleLen <= 7 -> 24.sp
                                            else -> 20.sp
                                        }

                                        Text(
                                            text = timerData.title,
                                            modifier = Modifier
                                                .clickable {
                                                    vm.toggleIsPurple()
                                                }
                                                .align(Alignment.BottomStart),
                                            fontFamily = timerFont,
                                            fontSize = timerTitleFontSize,
                                            color = c.white,
                                        )

                                        HStack(
                                            modifier = Modifier
                                                .padding(top = 8.dp)
                                                .align(Alignment.BottomEnd),
                                        ) {

                                            Icon(
                                                painterResource(R.drawable.sf_pause_small_medium),
                                                contentDescription = "Pause",
                                                tint = c.white,
                                                modifier = Modifier
                                                    .size(timerButtonsHeight)
                                                    .border(1.dp, c.white, roundedShape)
                                                    .clip(roundedShape)
                                                    .clickable {
                                                        uiActivity.pauseLastInterval()
                                                    }
                                                    .padding(8.dp),
                                            )

                                            HStack(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .padding(start = 8.dp)
                                                    .height(timerButtonsHeight)
                                                    .border(1.dp, c.white, roundedShape)
                                                    .clip(roundedShape)
                                                    .clickable {
                                                        timerData.restart()
                                                    }
                                                    .padding(start = 7.dp, end = 6.dp),
                                            ) {

                                                Icon(
                                                    painterResource(id = R.drawable.sf_clock_arrow_circlepath_small_light),
                                                    contentDescription = "Restart",
                                                    tint = c.white,
                                                    modifier = Modifier
                                                        .size(15.dp),
                                                )

                                                Text(
                                                    text = timerData.restartText,
                                                    modifier = Modifier
                                                        .padding(start = 3.dp, bottom = 1.dp),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Light,
                                                    color = c.white,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            DividerBg(
                                modifier = Modifier.padding(start = emojiWidth),
                                isVisible = uiActivity.withTopDivider,
                            )
                        }
                    }
                }

                item {

                    Row(
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .padding(horizontal = 4.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(onePx, c.dividerBg, squircleShape)
                                .clip(squircleShape)
                                .clickable {
                                    Dialog.show(
                                        modifier = Modifier.fillMaxHeight(0.95f),
                                    ) { layer ->
                                        ChartDialogView(layer::close)
                                    }
                                },
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Chart", color = c.text)
                            }
                        }

                        Box(modifier = Modifier.width(25.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(onePx, c.dividerBg, squircleShape)
                                .clip(squircleShape)
                                .clickable {
                                    Dialog.show(
                                        modifier = Modifier.fillMaxHeight(0.95f),
                                    ) { layer ->
                                        HistoryDialogView(layer::close)
                                    }
                                },
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("History", color = c.text)
                            }
                        }
                    }
                }

                item {

                    Row(
                        modifier = Modifier
                            .padding(top = 18.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        GrayTextButton(
                            text = state.newActivityText,
                            modifier = Modifier.padding(start = 2.dp),
                        ) {
                            Sheet.show { layer ->
                                ActivityFormSheet(layer = layer, editedActivity = null)
                            }
                        }

                        GrayTextButton(
                            text = state.sortActivitiesText,
                            modifier = Modifier.padding(start = 12.dp),
                        ) {
                            Sheet.show { layer ->
                                EditActivitiesSheet(layer = layer)
                            }
                        }

                        GrayTextButton(
                            text = state.settingsText,
                            modifier = Modifier.padding(start = 12.dp),
                        ) {
                            Sheet.show { layer ->
                                SettingsSheet(layer = layer)
                            }
                        }
                    }
                }

                item {

                    @Composable
                    fun prepTextStyle(fontWeight: FontWeight = FontWeight.Normal) = LocalTextStyle.current.merge(
                        TextStyle(
                            color = c.textSecondary.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = fontWeight,
                            lineHeight = 17.sp,
                        )
                    )

                    Text(
                        "Set a timer for each task to stay focused.",
                        modifier = Modifier.padding(top = 12.dp, start = 8.dp, end = 8.dp),
                        style = prepTextStyle(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        "No \"stop\" option is the main feature of this app. Once you have completed one activity, you have to set a timer for the next one, even if it's a \"sleeping\" activity.",
                        modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp),
                        style = prepTextStyle()
                    )

                    val s8 = buildAnnotatedString {
                        append("This time-tracking approach provides real 24/7 data on how long everything takes. You can see it on the ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Chart")
                        }
                        append(". ")
                    }

                    Text(
                        text = s8,
                        modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp),
                        style = prepTextStyle(),
                    )
                }
            }
        }
    }
}

@Composable
private fun GrayTextButton(
    text: String,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        modifier = modifier
            .clip(MySquircleShape())
            .clickable {
                onClick()
            }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        color = c.blue,
        fontSize = 14.sp,
        fontWeight = FontWeight.Light,
    )
}
