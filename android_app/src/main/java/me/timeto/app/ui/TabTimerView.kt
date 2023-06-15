package me.timeto.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.*
import me.timeto.shared.db.IntervalModel
import me.timeto.shared.vm.TabTimerVM
import me.timeto.shared.vm.TimerTabProgressVM

private val timerTitleFont = FontFamily(Font(R.font.notosansmono_extrabold))
private val timerSubtitleFont = FontFamily(Font(R.font.notosansmono_black))

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TabTimerView() {

    val (_, state) = rememberVM { TabTimerVM() }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg),
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            TimerView()

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 21.dp, vertical = 28.dp),
            ) {

                val activitiesUI = state.activitiesUI
                itemsIndexed(
                    activitiesUI,
                    key = { _, i -> i.activity.id }
                ) { _, uiActivity ->

                    val isActive = uiActivity.isActive
                    val bgAnimate = animateColorAsState(
                        if (isActive) c.blue else c.bg,
                        spring(stiffness = Spring.StiffnessMediumLow)
                    )

                    SwipeToAction(
                        isStartOrEnd = remember { mutableStateOf(null) },
                        modifier = Modifier.clip(MySquircleShape(len = 70f)),
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
                                            timerContext = null
                                        )
                                    }
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.TopCenter,
                        ) {

                            val emojiWidth = 44.dp

                            Column(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 46.dp)
                                    .padding(top = 10.dp, bottom = 10.dp),
                                verticalArrangement = Arrangement.Center
                            ) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = uiActivity.activity.emoji,
                                        modifier = Modifier
                                            .width(emojiWidth),
                                        textAlign = TextAlign.Start,
                                        fontSize = 22.sp,
                                    )

                                    Text(
                                        text = uiActivity.listText,
                                        modifier = Modifier
                                            .weight(1f),
                                        color = if (isActive) c.white else c.text,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        maxLines = 1,
                                    )

                                    uiActivity.timerHints.forEach { hintUI ->
                                        Text(
                                            text = hintUI.text,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(99.dp))
                                                .clickable {
                                                    hintUI.startInterval()
                                                }
                                                .padding(horizontal = 4.dp, vertical = 3.dp),
                                            color = if (isActive) c.white else c.blue,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.W300,
                                        )
                                    }
                                }

                                val triggersListContentPaddings = remember {
                                    PaddingValues(start = emojiWidth - 1.dp)
                                }

                                TextFeaturesTriggersView(
                                    triggers = uiActivity.textFeatures.triggers,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                    contentPadding = triggersListContentPaddings
                                )

                                val noteUI = uiActivity.noteUI
                                if (noteUI != null) {

                                    Row(
                                        modifier = Modifier
                                            .padding(top = 4.dp, bottom = 2.dp, start = emojiWidth),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Text(
                                            noteUI.text,
                                            fontWeight = FontWeight.W300,
                                            fontSize = 14.sp,
                                            color = c.white,
                                            modifier = Modifier
                                                .weight(1f, false)
                                        )

                                        Text(
                                            "cancel",
                                            fontWeight = FontWeight.W500,
                                            fontSize = 13.sp,
                                            color = c.blue,
                                            maxLines = 1,
                                            modifier = Modifier
                                                .offset(y = 0.5.dp)
                                                .padding(start = 8.dp, top = 0.5.dp)
                                                .clip(RoundedCornerShape(99.dp))
                                                .background(c.white)
                                                .clickable {
                                                    scope.launchEx {
                                                        vibrateLong()
                                                        IntervalModel.cancelCurrentInterval()
                                                    }
                                                }
                                                .padding(start = 7.dp, end = 7.dp, bottom = 1.dp),
                                        )
                                    }

                                    TextFeaturesTriggersView(
                                        triggers = noteUI.textFeatures.triggers,
                                        modifier = Modifier.padding(top = 7.dp, bottom = 4.dp),
                                        contentPadding = triggersListContentPaddings
                                    )
                                }
                            }

                            DividerBg(
                                modifier = Modifier.padding(start = emojiWidth, end = 4.dp),
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

@Composable
private fun TimerView() {

    val isDayOrNight = !isSystemInDarkTheme()
    val (vm, state) = rememberVM { TimerTabProgressVM(isDayOrNight) }
    val timerData = state.timerData
    val progressHeight = 16.dp

    Box(
        modifier = Modifier
            .height(136.dp)
    ) {

        val subtitleColor = animateColorAsState(timerData.subtitleColor.toColor())

        AnimatedVisibility(
            timerData.subtitle != null,
            modifier = Modifier
                .padding(top = 13.dp)
                .align(Alignment.TopCenter),
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        ) {
            Text(
                text = timerData.subtitle ?: " ",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                fontFamily = timerSubtitleFont,
                color = subtitleColor.value,
                letterSpacing = 1.sp,
            )
        }

        val titleBottomPadding = animateDpAsState(
            if (timerData.subtitle == null) progressHeight - 2.dp else progressHeight - 7.dp,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        )

        Text(
            text = timerData.title,
            fontSize = 56.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = timerTitleFont,
            modifier = Modifier
                .padding(bottom = titleBottomPadding.value)
                .align(Alignment.BottomCenter)
                .clip(MySquircleShape(80f))
                .clickable {
                    vm.toggleIsCountdown()
                }
                .padding(horizontal = 12.dp) // To ripple
                .padding(bottom = 2.dp),
            color = timerData.titleColor.toColor(),
        )

        val shape = RoundedCornerShape(99.dp)

        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(progressHeight)
                .padding(horizontal = 34.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(onePx, c.timerBarBorder, shape)
                    .clip(shape)
                    .background(c.timerBarBackground)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
            ) {

                val widthAnimate = animateFloatAsState(
                    state.progressRatio,
                    // To fast rollback on start
                    if (time() > state.lastInterval.id) tween(1000, easing = LinearEasing) else spring()
                )

                val animateColorBar = animateColorAsState(state.progressColor.toColor())

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .drawBehind {
                            drawRect(
                                color = animateColorBar.value,
                                size = size.copy(size.width * widthAnimate.value)
                            )
                        }
                )
            }
        }
    }
}
