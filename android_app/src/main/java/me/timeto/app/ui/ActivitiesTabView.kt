package me.timeto.app.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack
import me.timeto.app.R
import me.timeto.app.c
import me.timeto.app.squircleShape

private val menuFontSize = 16.sp

@Composable
fun ActivitiesTabView() {

    ActivitiesListView(
        modifier = Modifier,
        onTaskStarted = {},
        bottomView = {
            HStack(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 12.dp, start = 10.dp),
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
                        .padding(horizontal = ActivitiesListView__END_H_PADDING, vertical = 4.dp),
                    color = c.blue,
                    fontSize = menuFontSize,
                    fontWeight = FontWeight.Light,
                )
            }
        },
    )
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
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
            fontSize = menuFontSize,
            fontWeight = FontWeight.Light,
        )
    }
}
