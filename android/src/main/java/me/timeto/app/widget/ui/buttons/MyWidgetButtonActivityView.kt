package me.timeto.app.widget.ui.buttons

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import me.timeto.app.R
import me.timeto.app.toColor
import me.timeto.app.toGlanceColorProvider
import me.timeto.app.ui.c
import me.timeto.app.ui.onePx
import me.timeto.app.widget.ui.symbol.MyWidgetSymbolView
import me.timeto.app.widget.widgetButtonsItemCircleHPadding
import me.timeto.app.widget.widgetButtonsItemCircleHeight
import me.timeto.app.widget.widgetButtonsRowHeight
import me.timeto.shared.Symbol
import me.timeto.shared.vm.home.buttons.HomeButtonType

@Composable
fun MyWidgetButtonActivityView(
    width: Dp,
    activity: HomeButtonType.Activity,
) {

    val context: Context = LocalContext.current

    Row(
        modifier = GlanceModifier
            .height(widgetButtonsRowHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Box(
            modifier = GlanceModifier
                .height(widgetButtonsItemCircleHeight)
                .fillMaxWidth()
                .cornerRadius(99.dp)
                .background(c.homeFg)
                .clickable {
                    val isStarted = activity.onBarPressedOrNeedTimerPicker()
                    // todo
                    // В данный момент просто запуск приложения,
                    // а надо показывать выбор времени.
                    if (!isStarted) {
                        context.startActivity(
                            context.packageManager.getLaunchIntentForPackage(context.packageName)
                        )
                    }
                },
        ) {

            Box(
                modifier = GlanceModifier
                    .fillMaxHeight()
                    .width(width * activity.progressRatio)
                    .background(remember(activity.bgColor) { activity.bgColor.toColor() }),
            ) {}

            if (activity.sort.size == 1) {
                Box(
                    modifier = GlanceModifier
                        .height(widgetButtonsItemCircleHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {

                    val symbol: Symbol = remember(activity.activityDb) {
                        activity.activityDb.symbolOrDefault()
                    }

                    Row(
                        modifier = GlanceModifier
                            .height(widgetButtonsItemCircleHeight)
                            .padding(
                                start = when (symbol) {
                                    is Symbol.Letter -> 7.dp
                                    is Symbol.Icon -> 5.dp
                                    is Symbol.Emoji -> widgetButtonsItemCircleHPadding
                                },
                            )
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        MyWidgetSymbolView(
                            symbol = symbol,
                            color = c.white,
                            letterSize = myWidgetButtonFontSize,
                            iconSize = 13.dp,
                            emojiSize = myWidgetButtonFontSize,
                            glanceModifier = GlanceModifier
                                .padding(top = onePx),
                        )

                        Box(GlanceModifier.defaultWeight()) {}
                    }

                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth(),
                    ) {
                        Box(GlanceModifier.defaultWeight()) {}
                        RightBarView(activity = activity)
                    }
                }
            } else {
                Row(
                    modifier = GlanceModifier
                        .height(widgetButtonsItemCircleHeight)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    Text(
                        text = activity.leftText,
                        style = myWidgetButtonFontStyle,
                        modifier = GlanceModifier
                            .padding(start = widgetButtonsItemCircleHPadding, top = onePx)
                            .defaultWeight(),
                        maxLines = 1,
                    )

                    RightBarView(activity = activity)
                }
            }
        }
    }
}

private val rightBarCheckedPadding = 2.5.dp

@Composable
private fun RightBarView(
    activity: HomeButtonType.Activity,
) {
    if (activity.isCompleted) {
        Box(
            modifier = GlanceModifier
                .padding(end = rightBarCheckedPadding),
        ) {
            Box(
                modifier = GlanceModifier
                    .size(widgetButtonsItemCircleHeight - rightBarCheckedPadding * 2)
                    .cornerRadius(99.dp)
                    .background(c.white),
                contentAlignment = Alignment.Center,
            ) {
                val tint = remember(activity.bgColor) {
                    activity.bgColor.toColor().toGlanceColorProvider()
                }
                Image(
                    provider = ImageProvider(R.drawable.sf_checkmark_medium_semibold),
                    contentDescription = "Checklist completed",
                    modifier = GlanceModifier
                        .size(8.dp),
                    colorFilter = ColorFilter.tint(tint),
                )
            }
        }
    } else {
        Text(
            text = activity.rightText,
            style = myWidgetButtonFontStyle,
            modifier = GlanceModifier
                .padding(end = widgetButtonsItemCircleHPadding, top = onePx),
        )
    }
}
