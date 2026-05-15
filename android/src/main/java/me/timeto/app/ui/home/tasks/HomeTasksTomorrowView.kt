package me.timeto.app.ui.home.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.R
import me.timeto.app.ui.HStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.home.HomeScreen__hPadding
import me.timeto.app.ui.home.HomeScreen__itemCircleFontSize
import me.timeto.app.ui.home.HomeScreen__itemCircleFontWeight
import me.timeto.app.ui.home.HomeScreen__itemCircleHPadding
import me.timeto.app.ui.home.HomeScreen__itemCircleHeight
import me.timeto.app.ui.home.HomeScreen__itemCircleMarginTrailing
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.home.HomeScreen__primaryFontSize
import me.timeto.app.ui.onePx
import me.timeto.app.ui.roundedShape
import me.timeto.shared.vm.home.tasks.HomeTasksItemUi

@Composable
fun HomeTasksTomorrowView(
    tomorrowUi: HomeTasksItemUi.HomeTomorrowItemUi,
) {
    HStack(
        modifier = Modifier
            .height(HomeScreen__itemHeight)
            .fillMaxWidth()
            .background(c.black)
            .padding(horizontal = HomeScreen__hPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        TypeIcon(tomorrowUi.type)

        val timeUi = tomorrowUi.timeUi
        if (timeUi != null) {
            HStack(
                modifier = Modifier
                    .padding(end = HomeScreen__itemCircleMarginTrailing)
                    .height(HomeScreen__itemCircleHeight)
                    .clip(roundedShape)
                    .background(c.blue)
                    .padding(horizontal = HomeScreen__itemCircleHPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = timeUi.text,
                    modifier = Modifier
                        .padding(top = onePx),
                    fontWeight = HomeScreen__itemCircleFontWeight,
                    fontSize = HomeScreen__itemCircleFontSize,
                    lineHeight = 18.sp,
                    color = c.white,
                )
            }
        }

        Text(
            text = tomorrowUi.text,
            modifier = Modifier
                .padding(end = 4.dp)
                .weight(1f),
            fontSize = HomeScreen__primaryFontSize,
            color = c.white,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun TypeIcon(
    type: HomeTasksItemUi.HomeTomorrowItemUi.TomorrowType,
) {
    ZStack(
        modifier = Modifier
            .fillMaxHeight()
            .padding(
                end = when (type) {
                    HomeTasksItemUi.HomeTomorrowItemUi.TomorrowType.repeating ->
                        9.dp
                    HomeTasksItemUi.HomeTomorrowItemUi.TomorrowType.calendar ->
                        8.dp
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(
                when (type) {
                    HomeTasksItemUi.HomeTomorrowItemUi.TomorrowType.repeating ->
                        R.drawable.sf_repeat_medium_semibold
                    HomeTasksItemUi.HomeTomorrowItemUi.TomorrowType.calendar ->
                        R.drawable.sf_calendar_medium_regular
                }
            ),
            contentDescription = when (type) {
                HomeTasksItemUi.HomeTomorrowItemUi.TomorrowType.repeating ->
                    "Repeating Task"
                HomeTasksItemUi.HomeTomorrowItemUi.TomorrowType.calendar ->
                    "Calendar Event"
            },
            modifier = Modifier
                .size(
                    when (type) {
                        HomeTasksItemUi.HomeTomorrowItemUi.TomorrowType.repeating ->
                            19.dp
                        HomeTasksItemUi.HomeTomorrowItemUi.TomorrowType.calendar ->
                            20.dp
                    }
                ),
            tint = c.blue,
        )
    }
}
