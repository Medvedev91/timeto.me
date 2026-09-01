package me.timeto.app.widget.ui.tasks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import me.timeto.app.MainActivity
import me.timeto.app.R
import me.timeto.app.toColor
import me.timeto.app.toGlanceColorProvider
import me.timeto.app.ui.c
import me.timeto.app.widget.ui.buttons.myWidgetButtonFontSize
import me.timeto.app.widget.ui.buttons.myWidgetButtonFontStyle
import me.timeto.app.widget.ui.myWidgetItemCircleHeight
import me.timeto.app.widget.ui.myWidgetItemHeight
import me.timeto.app.widget.ui.myWidgetPrimaryFontSize
import me.timeto.app.widget.ui.symbol.MyWidgetSymbolView
import me.timeto.shared.ActivityUi
import me.timeto.shared.Symbol
import me.timeto.shared.TextFeatures
import me.timeto.shared.vm.home.tasks.HomeTasksItemUi

@Composable
fun MyWidgetTaskView(
    homeTaskUi: HomeTasksItemUi.HomeTaskUi,
) {

    Row(
        modifier = GlanceModifier
            .height(myWidgetItemHeight)
            .fillMaxWidth()
            .background(c.black)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        val timeUi = homeTaskUi.timeUi
        if (timeUi != null) {
            val bgColor: Color = when (timeUi.status) {
                TextFeatures.TimeData.STATUS.IN -> c.homeFg
                TextFeatures.TimeData.STATUS.SOON -> c.blue
                TextFeatures.TimeData.STATUS.OVERDUE -> c.red
            }
            Row(
                modifier = GlanceModifier
                    .height(myWidgetItemCircleHeight)
                    .cornerRadius(99.dp)
                    .background(bgColor)
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = timeUi.text,
                    style = myWidgetButtonFontStyle,
                )
            }
            Box(GlanceModifier.padding(end = 6.dp)) {}
        }

        if (homeTaskUi.taskUi.tf.paused != null) {
            Box(
                modifier = GlanceModifier
                    .padding(end = 6.dp)
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(myWidgetItemCircleHeight)
                        .cornerRadius(99.dp)
                        .background(c.green),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.sf_pause_medium_black),
                        contentDescription = "Paused Task",
                        modifier = GlanceModifier
                            .size(10.dp),
                        colorFilter = ColorFilter.tint(c.white.toGlanceColorProvider()),
                    )
                }
            }
        }

        val activityUi: ActivityUi? =
            homeTaskUi.taskUi.activityUi
        if (activityUi != null) {
            val symbol: Symbol =
                activityUi.symbol
            Box(
                modifier = GlanceModifier
                    .width(20.dp),
            ) {
                val offsetX: Dp = remember(symbol) {
                    when (symbol) {
                        is Symbol.Letter -> 2.dp
                        is Symbol.Icon -> (-3).dp
                        is Symbol.Emoji -> (-3).dp
                    }
                }
                MyWidgetSymbolView(
                    symbol = symbol,
                    color = remember(activityUi.colorRgba) {
                        activityUi.colorRgba.toColor()
                    },
                    letterSize = myWidgetPrimaryFontSize,
                    iconSize = 17.dp,
                    emojiSize = myWidgetButtonFontSize,
                    glanceModifier = GlanceModifier
                        .padding(start = offsetX),
                )
            }
        }

        Text(
            text = homeTaskUi.text,
            modifier = GlanceModifier
                .padding(end = 4.dp)
                .defaultWeight(),
            style = TextStyle(
                fontSize = myWidgetPrimaryFontSize,
                color = c.white.toGlanceColorProvider(),
            ),
            maxLines = 1,
        )

        // todo if expanded?
        /*
        if (timeUi != null) {
            val noteColor: Color = when (timeUi.status) {
                TextFeatures.TimeData.STATUS.IN -> c.secondaryText
                TextFeatures.TimeData.STATUS.SOON -> c.blue
                TextFeatures.TimeData.STATUS.OVERDUE -> c.red
            }
            Text(
                text = timeUi.note,
                modifier = Modifier
                    .padding(end = homeTasksInnerHPadding),
                fontSize = HomeScreen__primaryFontSize,
                color = noteColor,
            )
        }
        */
    }
}
