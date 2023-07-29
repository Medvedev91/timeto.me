package me.timeto.app.ui

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.data.TimerTabActivityData
import me.timeto.shared.vm.TabTimerVM

private val timerButtonsHeight = 28.dp
private val emojiWidth = 56.dp
private val activitiesInnerHPadding = 12.dp
private val timerHintsHPadding = 4.dp
private val menuTextButtonHPadding = (activitiesInnerHPadding + timerHintsHPadding) / 2

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TabTimerView() {

    val (vm, state) = rememberVM { TabTimerVM() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg),
    ) {

        Row(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 10.dp, end = menuTextButtonHPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Icon(
                painterResource(R.drawable.sf_line_3_horizontal_small_thin),
                contentDescription = "Menu",
                tint = c.blue,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .offset(y = 1.dp)
                    .clip(roundedShape)
                    .clickable {
                        Sheet.show { layer ->
                            SettingsSheet(layer = layer)
                        }
                    }
                    .size(32.dp)
                    .padding(5.dp)
            )

            SpacerW1()

            Text(
                text = "Edit",
                modifier = Modifier
                    .clip(squircleShape)
                    .clickable {
                        Sheet.show { layer ->
                            EditActivitiesSheet(layer = layer)
                        }
                    }
                    .padding(horizontal = menuTextButtonHPadding, vertical = 4.dp),
                color = c.blue,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {

            val activitiesUI = state.activitiesUI
            itemsIndexed(
                activitiesUI,
                key = { _, i -> i.activity.id }
            ) { _, activityUI ->

                val timerData = activityUI.data.timerData
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
                            note = activityUI.deletionHint,
                            deletionConfirmationNote = activityUI.deletionConfirmation,
                        ) {
                            activityUI.delete()
                        }
                    },
                    onStart = {
                        Sheet.show { layer ->
                            ActivityFormSheet(
                                layer = layer,
                                activity = activityUI.activity,
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
                                        activity = activityUI.activity,
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
                                    .padding(end = activitiesInnerHPadding),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {

                                Text(
                                    text = activityUI.activity.emoji,
                                    style = TextStyle(
                                        shadow = Shadow(
                                            color = c.white,
                                            blurRadius = 2f,
                                        )
                                    ),
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
                                        val textFontSize = if (isActive) 17 else 16
                                        Text(
                                            text = activityUI.data.text,
                                            modifier = Modifier
                                                .weight(1f, fill = false),
                                            color = if (isActive) c.white else c.text,
                                            fontSize = textFontSize.sp,
                                            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        TriggersListIconsView(activityUI.data.textTriggers, (textFontSize - 2).sp)
                                    }

                                    val listNote = activityUI.data.note
                                    if (listNote != null)
                                        HStack(
                                            modifier = Modifier
                                                .offset(y = (-1).dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            val noteIcon = activityUI.data.noteIcon
                                            if (noteIcon != null) {
                                                val noteIconResId: Int = when (noteIcon) {
                                                    TimerTabActivityData.NoteIcon.event -> R.drawable.sf_calendar_medium_light
                                                }
                                                Icon(
                                                    painterResource(noteIconResId),
                                                    contentDescription = "Event",
                                                    tint = c.white,
                                                    modifier = Modifier
                                                        .padding(end = 5.dp)
                                                        .size(14.dp),
                                                )
                                            }
                                            Text(
                                                text = listNote,
                                                modifier = Modifier
                                                    .weight(1f, fill = false),
                                                color = c.white,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Light,
                                            )
                                            TriggersListIconsView(activityUI.data.noteTriggers, 12.sp)
                                        }
                                }

                                activityUI.timerHints.forEach { hintUI ->
                                    Text(
                                        text = hintUI.text,
                                        modifier = Modifier
                                            .padding(top = 1.dp)
                                            .clip(roundedShape)
                                            .clickable {
                                                hintUI.startInterval()
                                            }
                                            .padding(horizontal = timerHintsHPadding, vertical = 3.dp),
                                        color = if (isActive) c.white else c.blue,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Light,
                                    )
                                }
                            }

                            if (timerData != null) {

                                ZStack(
                                    modifier = Modifier
                                        .padding(top = 8.dp, bottom = 2.dp, start = 12.dp, end = 10.dp)
                                        .fillMaxWidth(),
                                ) {

                                    val timerDataTitleLen = timerData.title.length
                                    val timerTitleFontSize = when {
                                        timerDataTitleLen <= 5 -> 34.sp
                                        timerDataTitleLen <= 7 -> 32.sp
                                        else -> 28.sp
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
                                                    activityUI.pauseLastInterval()
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
                            isVisible = activityUI.withTopDivider,
                        )
                    }
                }
            }

            item {

                HStack(
                    modifier = Modifier
                        .padding(top = 12.dp, start = 10.dp),
                ) {

                    ChartHistoryButton(
                        text = "Chart",
                        iconResId = R.drawable.sf_chart_pie_small_thin,
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
                        iconSize = 20.dp,
                    ) {
                        Dialog.show(
                            modifier = Modifier.fillMaxHeight(0.95f),
                        ) { layer ->
                            HistoryDialogView(layer::close)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartHistoryButton(
    text: String,
    @DrawableRes iconResId: Int,
    iconSize: Dp = 18.dp,
    onClick: () -> Unit,
) {
    HStack(
        modifier = Modifier
            .clip(squircleShape)
            .clickable {
                onClick()
            }
            .padding(horizontal = menuTextButtonHPadding, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Icon(
            painterResource(iconResId),
            contentDescription = text,
            tint = c.blue,
            modifier = Modifier
                .padding(end = 5.dp)
                .size(iconSize)
        )

        Text(
            text = text,
            color = c.blue,
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
        )
    }
}
