package me.timeto.app.ui.home.buttons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import me.timeto.app.ui.goals.form.Goal2FormFs
import me.timeto.app.ui.home.HomeScreen__itemCircleFontSize
import me.timeto.app.ui.home.HomeScreen__itemCircleFontWeight
import me.timeto.app.ui.home.HomeScreen__itemCircleHPadding
import me.timeto.app.ui.home.HomeScreen__itemCircleHeight
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.home.settings.HomeSettingsButtonsFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.picker.NavigationPickerItem
import me.timeto.app.ui.roundedShape
import me.timeto.shared.vm.home.buttons.HomeButtonType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeButtonGoalView(
    goal: HomeButtonType.Goal,
) {

    val navigationFs = LocalNavigationFs.current

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
                .combinedClickable(
                    onClick = {
                        goal.startInterval()
                    },
                    onLongClick = {
                        navigationFs.picker(
                            title = goal.fullText,
                            items = contextPickerItems,
                            onDone = { pickerItem ->
                                navigationFs.push {
                                    when (pickerItem.item) {
                                        ContextPickerItemType.EditGoal -> {
                                            Goal2FormFs(
                                                goalDb = goal.goalDb,
                                            )
                                        }
                                        ContextPickerItemType.HomeScreenSettings -> {
                                            HomeSettingsButtonsFs()
                                        }
                                    }
                                }
                            },
                        )
                    },
                ),
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
                    .padding(start = HomeScreen__itemCircleHPadding)
                    .align(Alignment.CenterStart),
                color = c.white,
                fontSize = HomeScreen__itemCircleFontSize,
                fontWeight = HomeScreen__itemCircleFontWeight,
                lineHeight = 18.sp,
            )

            Text(
                text = goal.rightText,
                modifier = Modifier
                    .padding(end = HomeScreen__itemCircleHPadding)
                    .align(Alignment.CenterEnd),
                color = c.white,
                fontSize = HomeScreen__itemCircleFontSize,
                fontWeight = HomeScreen__itemCircleFontWeight,
                lineHeight = 18.sp,
            )
        }
    }
}

private val contextPickerItems = listOf(
    NavigationPickerItem(
        title = "Edit",
        isSelected = false,
        item = ContextPickerItemType.EditGoal,
    ),
    NavigationPickerItem(
        title = "Edit Home Screen",
        isSelected = false,
        item = ContextPickerItemType.HomeScreenSettings,
    ),
)

private enum class ContextPickerItemType {
    EditGoal,
    HomeScreenSettings,
}
