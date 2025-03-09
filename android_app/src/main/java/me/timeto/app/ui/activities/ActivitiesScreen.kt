package me.timeto.app.ui.activities

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.HStack
import me.timeto.app.MainActivity
import me.timeto.app.R
import me.timeto.app.VStack
import me.timeto.app.c
import me.timeto.app.onePx
import me.timeto.app.squircleShape
import me.timeto.app.ui.EditActivitiesSheet
import me.timeto.app.ui.HistoryDialogView
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.SummarySheet
import me.timeto.app.ui.navigation.LocalNavigationFs

@Composable
fun ActivitiesScreen(
    onClose: () -> Unit,
) {

    val mainActivity = LocalContext.current as MainActivity

    BackHandler {
        onClose()
    }

    Screen {
        VStack(
            modifier = Modifier
                .padding(top = mainActivity.statusBarHeightDp),
        ) {
            ActivitiesView(
                modifier = Modifier
                    .weight(1f),
            )
            BottomMenu()
        }
    }
}

///

@Composable
private fun BottomMenu() {

    val navigationFs = LocalNavigationFs.current

    HStack(
        modifier = Modifier
            .height(ActivitiesView__listItemHeight)
            .padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        MenuIconButton(
            text = "Summary",
            iconResId = R.drawable.sf_chart_pie_small_thin,
            iconSize = 17.dp,
            onClick = {
                navigationFs.push {
                    SummarySheet()
                }
            }
        )

        MenuIconButton(
            text = "History",
            iconResId = R.drawable.sf_list_bullet_rectangle_small_thin,
            iconSize = 19.dp,
            extraIconPadding = onePx,
            onClick = {
                navigationFs.push {
                    HistoryDialogView()
                }
            },
        )

        SpacerW1()

        Text(
            text = "Edit",
            modifier = Modifier
                .padding(end = ActivitiesView__listEndPadding)
                .clip(squircleShape)
                .clickable {
                    navigationFs.push {
                        EditActivitiesSheet()
                    }
                }
                .padding(horizontal = ActivitiesView__timerHintHPadding, vertical = 4.dp),
            color = c.blue,
        )
    }
}

@Composable
private fun MenuIconButton(
    text: String,
    @DrawableRes iconResId: Int,
    iconSize: Dp,
    extraIconPadding: Dp = 0.dp,
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
            painter = painterResource(id = iconResId),
            contentDescription = text,
            tint = c.blue,
            modifier = Modifier
                .padding(end = 5.dp + extraIconPadding)
                .size(iconSize)
        )

        Text(
            text = text,
            color = c.blue,
        )
    }
}
