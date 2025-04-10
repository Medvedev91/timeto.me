package me.timeto.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack
import me.timeto.app.H_PADDING
import me.timeto.app.R
import me.timeto.app.VStack
import me.timeto.app.ZStack
import me.timeto.app.c
import me.timeto.app.roundedShape
import me.timeto.app.squircleShape
import me.timeto.app.timerFont
import me.timeto.app.toColor
import me.timeto.app.ui.ActivityTimerSheet__show
import me.timeto.app.ui.DaytimePickerSheet
import me.timeto.app.ui.Sheet
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.readme.ReadmeFs
import me.timeto.shared.vm.HomeVm
import me.timeto.shared.vm.ReadmeVm

@Composable
fun HomeTimerView(
    vm: HomeVm,
    state: HomeVm.State,
) {

    val navigationFs = LocalNavigationFs.current

    val noteColor = animateColorAsState(state.timerData.noteColor.toColor()).value
    val timerColor = animateColorAsState(state.timerData.timerColor.toColor()).value
    val timerControlsColor = animateColorAsState(state.timerData.controlsColor.toColor()).value

    VStack(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        ZStack(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 7.dp),
            contentAlignment = Alignment.TopCenter,
        ) {

            TimerDataNoteText(
                text = state.timerData.note,
                color = noteColor,
            )

            HStack {

                val timerText = state.timerData.timerText
                val timerFontSize: TextUnit = run {
                    val len = timerText.count()
                    when {
                        len <= 5 -> 40.sp
                        len <= 7 -> 35.sp
                        else -> 28.sp
                    }
                }

                VStack(
                    modifier = Modifier
                        .weight(1f)
                        .offset(x = 1.dp),
                ) {

                    TimerDataNoteText(" ", c.transparent)

                    ZStack(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(squircleShape)
                            .clickable {
                                vm.toggleIsPurple()
                            },
                        contentAlignment = Alignment.Center,
                    ) {

                        TimerDataTimerText(" ", timerFontSize, c.transparent)

                        Icon(
                            painterResource(id = R.drawable.sf_info_medium_thin),
                            contentDescription = "Timer Info",
                            tint = timerControlsColor,
                            modifier = Modifier
                                .size(16.dp),
                        )
                    }
                }

                VStack(
                    modifier = Modifier
                        .clip(squircleShape)
                        .clickable {
                            state.timerData.togglePomodoro()
                        },
                ) {

                    TimerDataNoteText(" ", c.transparent)

                    TimerDataTimerText(
                        text = timerText,
                        fontSize = timerFontSize,
                        color = timerColor,
                    )
                }

                VStack(
                    modifier = Modifier
                        .weight(1f)
                        .offset(x = (-1).dp),
                ) {

                    TimerDataNoteText(" ", c.transparent)

                    ZStack(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(squircleShape)
                            .clickable {
                                state.timerData.prolong()
                            },
                        contentAlignment = Alignment.Center,
                    ) {

                        TimerDataTimerText(" ", timerFontSize, c.transparent)

                        val prolongedText = state.timerData.prolongText
                        if (prolongedText != null) {
                            Text(
                                text = prolongedText,
                                color = timerControlsColor,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Thin,
                            )
                        } else {
                            Icon(
                                painterResource(id = R.drawable.sf_plus_medium_thin),
                                contentDescription = "Plus",
                                tint = timerControlsColor,
                                modifier = Modifier
                                    .size(16.dp),
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            state.isPurple,
            enter = purpleAnimEnter,
            exit = purpleAnimExit,
        ) {

            val infoUi = state.timerData.infoUi

            HStack(
                modifier = Modifier
                    .offset(y = (-2).dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                TimerInfoButton(
                    text = infoUi.untilDaytimeUi.text,
                    color = timerColor,
                    onClick = {
                        Sheet.show { layer ->
                            DaytimePickerSheet(
                                layer = layer,
                                title = infoUi.untilPickerTitle,
                                doneText = "Start",
                                daytimeUi = infoUi.untilDaytimeUi,
                                withRemove = false,
                                onDone = { daytimePickerUi ->
                                    infoUi.setUntilDaytime(daytimePickerUi)
                                    vm.toggleIsPurple()
                                },
                                onRemove = {},
                            )
                        }
                    },
                )

                TimerInfoButton(
                    text = infoUi.timerText,
                    color = timerColor,
                    onClick = {
                        ActivityTimerSheet__show(
                            activity = state.activeActivityDb,
                            timerContext = state.timerData.infoUi.timerContext,
                            onStarted = {
                                vm.toggleIsPurple()
                            },
                        )
                    },
                )

                TimerInfoButton(
                    text = "?",
                    color = timerColor,
                    onClick = {
                        navigationFs.push {
                            ReadmeFs(
                                defaultItem = ReadmeVm.DefaultItem.pomodoro,
                            )
                        }
                    },
                )
            }
        }
    }
}

///

private val purpleAnimEnter =
    fadeIn() + expandVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh))

private val purpleAnimExit =
    fadeOut() + shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessHigh))

///

@Composable
private fun TimerInfoButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(roundedShape)
            .clickable {
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = color,
        fontSize = 19.sp,
        fontWeight = FontWeight.Thin,
    )
}

@Composable
private fun TimerDataNoteText(
    text: String,
    color: Color,
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(bottom = 4.dp)
            .padding(horizontal = H_PADDING),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun TimerDataTimerText(
    text: String,
    fontSize: TextUnit,
    color: Color,
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(vertical = 4.dp),
        fontSize = fontSize,
        fontFamily = timerFont,
        color = color,
    )
}
