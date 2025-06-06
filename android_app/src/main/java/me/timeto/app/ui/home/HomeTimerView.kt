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
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING
import me.timeto.app.R
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.squircleShape
import me.timeto.app.ui.timerFont
import me.timeto.app.toColor
import me.timeto.app.ui.daytime_picker.DaytimePickerSheet
import me.timeto.app.ui.activities.timer.ActivityTimerFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.readme.ReadmeFs
import me.timeto.shared.vm.home.HomeVm
import me.timeto.shared.vm.readme.ReadmeVm

@Composable
fun HomeTimerView(
    vm: HomeVm,
    state: HomeVm.State,
) {

    val navigationFs = LocalNavigationFs.current

    val noteColor = animateColorAsState(state.timerStateUi.noteColor.toColor()).value
    val timerColor = animateColorAsState(state.timerStateUi.timerColor.toColor()).value
    val timerControlsColor = animateColorAsState(
        state.timerStateUi.controlsColorEnum?.toColor() ?: c.homeTimerControls
    ).value

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
                text = state.timerStateUi.note,
                color = noteColor,
            )

            HStack {

                val timerText = state.timerStateUi.timerText
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
                            state.timerStateUi.togglePomodoro()
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
                                state.timerStateUi.prolong()
                            },
                        contentAlignment = Alignment.Center,
                    ) {

                        TimerDataTimerText(" ", timerFontSize, c.transparent)

                        val prolongedText = state.timerStateUi.prolongText
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

            val infoUi = state.timerStateUi.infoUi

            HStack(
                modifier = Modifier
                    .offset(y = (-2).dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                TimerInfoButton(
                    text = infoUi.untilDaytimeUi.text,
                    color = timerColor,
                    onClick = {
                        navigationFs.push {
                            DaytimePickerSheet(
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
                        navigationFs.push {
                            ActivityTimerFs(
                                activityDb = state.activeActivityDb,
                                strategy = state.timerStateUi.infoUi.timerStrategy,
                            )
                        }
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
