package me.timeto.app.ui.home.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import me.timeto.app.toColor
import me.timeto.app.ui.HStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.home.HomeScreen__itemCircleFontSize
import me.timeto.app.ui.home.HomeScreen__itemCircleFontWeight
import me.timeto.app.ui.home.HomeScreen__itemCircleHPadding
import me.timeto.app.ui.home.HomeScreen__itemCircleHeight
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.onePx
import me.timeto.app.ui.roundedShape
import me.timeto.shared.vm.home.buttons.HomeButtonType

@Composable
fun HomeButtonGoalView(
    goal: HomeButtonType.Goal,
) {

    HStack(
        modifier = Modifier
            .height(HomeScreen__itemHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        ZStack(
            modifier = Modifier
                .height(HomeScreen__itemCircleHeight)
                .fillMaxWidth()
                .clip(roundedShape)
                .background(c.homeFg)
                .clickable {
                    goal.startInterval()
                },
        ) {

            ZStack(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(goal.progressRatio)
                    .background(goal.bgColor.toColor())
                    .clip(roundedShape)
                    .align(Alignment.CenterStart),
            )

            Text(
                text = goal.leftText,
                modifier = Modifier
                    .padding(start = HomeScreen__itemCircleHPadding, top = onePx)
                    .align(Alignment.CenterStart),
                color = c.white,
                fontSize = HomeScreen__itemCircleFontSize,
                fontWeight = HomeScreen__itemCircleFontWeight,
                lineHeight = 18.sp,
            )

            Text(
                text = goal.rightText,
                modifier = Modifier
                    .padding(end = HomeScreen__itemCircleHPadding, top = onePx)
                    .align(Alignment.CenterEnd),
                color = c.white,
                fontSize = HomeScreen__itemCircleFontSize,
                fontWeight = HomeScreen__itemCircleFontWeight,
                lineHeight = 18.sp,
            )
        }
    }
}
