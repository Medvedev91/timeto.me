package me.timeto.app.ui.main

import android.view.MotionEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.HStack
import me.timeto.app.R
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.c
import me.timeto.app.ui.halfDpFloor
import me.timeto.app.misc.Haptic
import me.timeto.app.ui.onePx
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.squircleShape
import me.timeto.app.ui.timerFont
import me.timeto.app.misc.extensions.toColor
import me.timeto.shared.ui.main.MainTabsVm

val MainTabsView__height = 56.dp
val MainTabsView__backgroundColor = Color(18, 18, 19)
val MainTabsView__dividerColor = Color(32, 35, 35)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainTabsView(
    tab: MainTabEnum,
    onTabChanged: (MainTabEnum) -> Unit,
) {

    val (_, state) = rememberVm {
        MainTabsVm()
    }

    LaunchedEffect(state.lastIntervalId) {
        onTabChanged(MainTabEnum.home)
        Haptic.long()
    }

    val withBackground: Boolean = tab != MainTabEnum.home
    val backgroundColor = animateColorAsState(
        if (withBackground) MainTabsView__backgroundColor else c.black,
    )
    val dividerColor = animateColorAsState(
        if (withBackground) MainTabsView__dividerColor else c.black,
    )

    ZStack(
        modifier = Modifier
            .drawBehind {
                drawRect(color = backgroundColor.value)
            }
            .navigationBarsPadding()
            .height(MainTabsView__height),
    ) {

        ZStack(
            modifier = Modifier
                .height(onePx)
                .fillMaxWidth()
                .drawBehind {
                    drawRect(color = dividerColor.value)
                },
        )

        HStack {

            TabButton(
                icon = R.drawable.sf_timer_medium_thin,
                contentDescription = "Timer",
                isSelected = tab == MainTabEnum.activities,
                onTouch = {
                    onTabChanged(
                        if (tab == MainTabEnum.activities) MainTabEnum.home
                        else MainTabEnum.activities,
                    )
                },
            )

            VStack(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(squircleShape)
                    .motionEventSpy { event ->
                        if (event.action == MotionEvent.ACTION_DOWN)
                            onTabChanged(
                                if (tab == MainTabEnum.home) MainTabEnum.tasks
                                else MainTabEnum.home
                            )
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = state.timeText,
                    color = c.mainTabsMenuPrimaryColor,
                    fontSize = 9.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = timerFont,
                    modifier = Modifier
                        .padding(top = 3.dp, bottom = 5.dp),
                )

                HStack(
                    modifier = Modifier
                        .padding(end = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    val batteryUi = state.batteryUi
                    val batteryTextColor = animateColorAsState(batteryUi.colorRgba.toColor())

                    Icon(
                        painterResource(
                            id = if (batteryUi.isHighlighted)
                                R.drawable.sf_bolt_fill_medium_bold
                            else
                                R.drawable.sf_bolt_fill_medium_light
                        ),
                        contentDescription = "Battery",
                        tint = batteryTextColor.value,
                        modifier = Modifier
                            .offset(y = -halfDpFloor)
                            .size(10.dp)
                    )

                    Text(
                        text = batteryUi.text,
                        color = batteryTextColor.value,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = if (batteryUi.isHighlighted) FontWeight.Bold else FontWeight.Light,
                    )

                    Icon(
                        painterResource(id = R.drawable.sf_smallcircle_filled_circle_small_light),
                        contentDescription = "Tasks",
                        tint = c.mainTabsMenuSecondaryColor,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(10.dp + halfDpFloor)
                            .offset(y = -halfDpFloor),
                    )

                    Text(
                        text = state.tasksText,
                        modifier = Modifier
                            .padding(start = 2.dp + halfDpFloor),
                        color = c.mainTabsMenuSecondaryColor,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Light,
                    )
                }
            }

            TabButton(
                icon = R.drawable.sf_ellipsis_circle_medium_thin,
                contentDescription = "Settings",
                isSelected = tab == MainTabEnum.settings,
                onTouch = {
                    onTabChanged(
                        if (tab == MainTabEnum.settings) MainTabEnum.home
                        else MainTabEnum.settings
                    )
                },
            )
        }
    }
}

///

private val tabButtonModifier: Modifier =
    Modifier
        .size(MainTabsView__height)
        .padding(14.dp)

///

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun RowScope.TabButton(
    @androidx.annotation.DrawableRes icon: Int,
    contentDescription: String,
    isSelected: Boolean,
    onTouch: () -> Unit,
) {
    ZStack(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(squircleShape)
            .motionEventSpy { event ->
                if (event.action == MotionEvent.ACTION_DOWN)
                    onTouch()
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            tint = if (isSelected) c.blue else c.mainTabsMenuSecondaryColor,
            modifier = tabButtonModifier,
        )
    }
}
