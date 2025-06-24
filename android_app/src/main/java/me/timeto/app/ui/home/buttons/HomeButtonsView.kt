package me.timeto.app.ui.home.buttons

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.rememberVm
import me.timeto.shared.vm.home.buttons.HomeButtonType
import me.timeto.shared.vm.home.buttons.HomeButtonsVm

private val rowHeight: Dp = HomeScreen__itemHeight
private val spacing: Dp = 8.dp

private val buttonsHPadding: Dp = H_PADDING

@Composable
fun HomeButtonsView() {

    val configuration = LocalConfiguration.current

    val (_, state) = rememberVm {
        HomeButtonsVm(
            width = (configuration.screenWidthDp.dp.value - (buttonsHPadding.value * 2)),
            rowHeight = rowHeight.value,
            spacing = spacing.value,
        )
    }

    ZStack(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = buttonsHPadding)
            .height(state.height.dp),
    ) {
        state.buttonsUi.forEach { buttonUi ->
            key(buttonUi.id) {
                ZStack(
                    modifier = Modifier
                        .size(width = buttonUi.width.dp, height = rowHeight)
                        .offset(x = buttonUi.offsetX.dp, y = buttonUi.offsetY.dp),
                ) {
                    when (val type = buttonUi.type) {
                        is HomeButtonType.Goal ->
                            HomeButtonGoalView(goal = type)
                    }
                }
            }
        }
    }
}
