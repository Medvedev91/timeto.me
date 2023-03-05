package app.time_to.timeto.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.time_to.timeto.*
import app.time_to.timeto.R
import kotlinx.coroutines.delay
import timeto.shared.*
import timeto.shared.db.IntervalModel
import timeto.shared.vm.TabTimerVM

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TabTimerView() {

    val (_, state) = rememberVM { TabTimerVM() }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.background),
    ) {
        val isAddSheetPresented = remember { mutableStateOf(false) }
        ActivityFormSheet(
            isPresented = isAddSheetPresented,
            editedActivity = null
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            TimerView(state.lastInterval)

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 22.dp),
            ) {

                val activitiesUI = state.activitiesUI
                itemsIndexed(
                    activitiesUI,
                    key = { _, i -> i.activity.id }
                ) { index, uiActivity ->

                    val isLast = index == activitiesUI.size - 1
                    val isActive = uiActivity.isActive
                    val bgAnimate = animateColorAsState(
                        if (isActive) c.blue else c.background2,
                        spring(stiffness = Spring.StiffnessMediumLow)
                    )

                    val clip = when {
                        index == 0 && isLast -> MySquircleShape()
                        index == 0 -> MySquircleShape(angles = listOf(true, true, false, false))
                        isLast -> MySquircleShape(angles = listOf(false, false, true, true))
                        else -> RoundedCornerShape(0.dp)
                    }

                    val isEditSheetPresented = remember { mutableStateOf(false) }
                    ActivityFormSheet(
                        isPresented = isEditSheetPresented,
                        editedActivity = uiActivity.activity,
                    )

                    SwipeToAction(
                        isStartOrEnd = remember { mutableStateOf(null) },
                        modifier = Modifier.clip(clip),
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
                            isEditSheetPresented.value = true
                            false
                        },
                        onEnd = {
                            true
                        },
                        toVibrateStartEnd = listOf(true, false),
                    ) {

                        val timerSheetState = ActivityTimerSheet(activity = uiActivity.activity, timerContext = null)

                        Box(
                            modifier = Modifier
                                .background(bgAnimate.value)
                                .clickable {
                                    scope.launchEx {
                                        timerSheetState.value = true
                                    }
                                },
                            contentAlignment = Alignment.TopCenter,
                        ) {

                            val emojiHPadding = 8.dp
                            val emojiWidth = 30.dp
                            val startPadding = emojiWidth + (emojiHPadding * 2)
                            val endPadding = 12.dp

                            Column(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 46.dp)
                                    .padding(top = 10.dp, bottom = 10.dp),
                                verticalArrangement = Arrangement.Center
                            ) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(end = endPadding - 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = uiActivity.activity.emoji,
                                        modifier = Modifier
                                            .padding(horizontal = emojiHPadding)
                                            .width(emojiWidth),
                                        textAlign = TextAlign.Center,
                                        fontSize = 20.sp,
                                    )

                                    Text(
                                        text = uiActivity.listText,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 4.dp),
                                        color = if (isActive) c.white else c.text,
                                        overflow = TextOverflow.Ellipsis,
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
                                                .padding(horizontal = 3.dp, vertical = 3.dp),
                                            color = if (isActive) c.white else c.blue,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.W300,
                                        )
                                    }
                                }

                                val triggersListContentPaddings = remember {
                                    PaddingValues(start = startPadding - 1.dp, end = endPadding)
                                }

                                TriggersView__ListView(
                                    triggers = uiActivity.triggers,
                                    withOnClick = true,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                    contentPadding = triggersListContentPaddings
                                )

                                val noteUI = uiActivity.noteUI
                                if (noteUI != null) {

                                    Row(
                                        modifier = Modifier
                                            .padding(top = 6.dp, bottom = 2.dp, end = endPadding),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        val leadingEmoji = noteUI.leadingEmoji
                                        if (leadingEmoji != null)
                                            Text(
                                                text = leadingEmoji,
                                                modifier = Modifier
                                                    .padding(horizontal = emojiHPadding)
                                                    .width(emojiWidth),
                                                fontSize = 14.sp,
                                                textAlign = TextAlign.Center,
                                            )

                                        Text(
                                            noteUI.text,
                                            fontWeight = FontWeight.W300,
                                            fontSize = 14.sp,
                                            color = c.white,
                                            modifier = Modifier
                                                .weight(1f, false)
                                                .padding(start = if (leadingEmoji != null) 0.dp else startPadding)
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

                                    TriggersView__ListView(
                                        triggers = noteUI.triggers,
                                        withOnClick = true,
                                        modifier = Modifier.padding(top = 7.dp, bottom = 4.dp),
                                        contentPadding = triggersListContentPaddings
                                    )
                                }
                            }

                            if (uiActivity.withTopDivider)
                                Divider(
                                    color = c.dividerBackground2,
                                    modifier = Modifier
                                        .padding(start = startPadding),
                                    thickness = 0.5.dp
                                )
                        }
                    }
                }

                item {

                    Row(
                        modifier = Modifier.padding(top = 20.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(MySquircleShape())
                                .background(c.background2)
                                .clickable {
                                    MyDialog.show(
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
                                    .padding(horizontal = 18.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Chart", color = c.text)
                            }
                        }

                        Box(modifier = Modifier.width(15.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(MySquircleShape())
                                .background(c.background2)
                                .clickable {
                                    MyDialog.show(
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
                                    .padding(horizontal = 18.dp, vertical = 12.dp),
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
                            .padding(top = 14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            painterResource(id = R.drawable.ic_round_add_24),
                            "New activity",
                            tint = c.textSecondary,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .alpha(0.5f)
                                .size(32.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(c.background)
                                .clickable {
                                    isAddSheetPresented.value = true
                                }
                                .padding(4.dp)
                        )

                        val isEditActivitiesDialogPresented = remember { mutableStateOf(false) }
                        EditActivitiesSheet(
                            isPresented = isEditActivitiesDialogPresented
                        )

                        Text(
                            "Edit",
                            modifier = Modifier
                                .padding(top = 1.dp, start = 12.dp)
                                .clip(MySquircleShape())
                                .clickable {
                                    isEditActivitiesDialogPresented.value = true
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            style = TextStyle(
                                color = c.textSecondary.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                            )
                        )
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
private fun TimerView(
    interval: IntervalModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val refreshMls = 200L

    var nowTimeMillis by remember { mutableStateOf(timeMls()) }
    LaunchedEffect(nowTimeMillis) {
        delay(refreshMls)
        nowTimeMillis = timeMls()
    }

    val timerData = TimerData(interval, ColorNative.text)

    var isCountdownOrPast by remember { mutableStateOf(true) }
    LaunchedEffect(interval.id) {
        isCountdownOrPast = true
    }

    Box(
        modifier = Modifier.padding(top = 6.dp)
    ) {

        val animateColor = animateColorAsState(targetValue = timerData.color.toColor())

        val timerTitle = timerData.title
        if (timerTitle != null)
            Text(
                text = timerTitle,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(Alignment.TopCenter),
                color = animateColor.value,
                letterSpacing = 3.sp,
            )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Spacer(Modifier.weight(1f))

                Icon(
                    painterResource(id = R.drawable.sf_arrow_counterclockwise_medium_regular),
                    "Restart",
                    tint = animateColor.value,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(top = 1.dp)
                        .offset(x = 10.dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .clickable {
                            scope.launchEx {
                                IntervalModel.restartActualInterval()
                            }
                        }
                        .padding(10.dp)
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = if (isCountdownOrPast) timerData.timer else TimerData.secondsToString(time() - interval.id),
                    fontSize = 50.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isCountdownOrPast = !isCountdownOrPast }
                        .padding(horizontal = 6.dp),
                    fontFamily = FontFamily.Monospace,
                    color = if (isCountdownOrPast) animateColor.value else c.purple
                )

                Spacer(Modifier.weight(1f))

                Icon(
                    painterResource(id = R.drawable.sf_up_left_medium_light),
                    "Fullscreen",
                    tint = animateColor.value,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(top = 1.dp)
                        .offset(x = (-10).dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .clickable {
                            FullScreenUI.open()
                        }
                        .padding(11.dp)
                )

                Spacer(Modifier.weight(1f))
            }

            val shape = RoundedCornerShape(99.dp)

            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(15.dp)
                    .padding(horizontal = 24.5.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape)
                        .background(c.timerBarBorder)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.5.dp) // for border
                        .clip(shape)
                        .background(c.timerBarBackground)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape)
                ) {

                    val ratio: Float =
                        (nowTimeMillis - interval.id * 1000L).toFloat() / (interval.deadline * 1000L).toFloat()

                    val widthAnimate = animateFloatAsState(
                        ratio,
                        // To fast rollback on start
                        if (time() > interval.id) tween(refreshMls.toInt(), easing = LinearEasing) else spring()
                    )

                    val animateColorBar = animateColorAsState(
                        targetValue = if (ratio < 1) c.blue else timerData.color.toColor()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(widthAnimate.value)
                            .background(animateColorBar.value)
                    )
                }
            }
        }
    }
}
