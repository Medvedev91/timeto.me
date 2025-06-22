package me.timeto.app.ui.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.toColor
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.H_PADDING_HALF
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.activities.ActivityPickerFs
import me.timeto.app.ui.footer.Footer
import me.timeto.app.ui.footer.FooterAddButton
import me.timeto.app.ui.goals.form.GoalFormFs
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.roundedShape
import me.timeto.shared.vm.goals.form.GoalFormStrategy
import me.timeto.shared.vm.home.settings.buttons.HomeSettingsButtonUi
import me.timeto.shared.vm.home.settings.buttons.HomeSettingsButtonsVm

private val rowHeight: Dp = 26.dp
private val barHeight: Dp = 24.dp
private val spacing: Dp = 10.dp

private val buttonsHPadding: Dp = H_PADDING

@Composable
fun HomeSettingsButtonsFs() {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val configuration = LocalConfiguration.current

    val (vm, state) = rememberVm {
        HomeSettingsButtonsVm(
            spacing = spacing.value,
            rowHeight = rowHeight.value,
            width = (configuration.screenWidthDp.dp.value - (buttonsHPadding.value * 2)),
        )
    }

    Screen {

        Header(
            title = state.title,
            scrollState = null,
            actionButton = HeaderActionButton(
                text = "Save",
                isEnabled = true,
                onClick = {
                    vm.save()
                    navigationLayer.close()
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            ),
        )

        SpacerW1()

        ZStack(
            modifier = Modifier
                .padding(horizontal = buttonsHPadding)
                .fillMaxWidth()
                .height(state.height.dp),
        ) {

            state.buttonsData.emptyButtonsUi.forEach { buttonUi ->
                ButtonView(
                    buttonUi = buttonUi,
                    extraLeftWidth = 0f,
                    extraRightWidth = 0f,
                    content = {},
                )
            }
        }

        Footer(
            scrollState = null,
            contentModifier = Modifier
                .padding(horizontal = H_PADDING_HALF),
        ) {
            FooterAddButton(
                text = state.newGoalText,
                onClick = {
                    navigationFs.push {
                        ActivityPickerFs(
                            initActivityDb = null,
                            onDone = { activityDb ->
                                navigationFs.push {
                                    GoalFormFs(
                                        strategy = GoalFormStrategy.NewGoal(
                                            activityDb = activityDb,
                                            onCreate = { goalDb ->
                                                vm.addGoalButton(goalDb)
                                            }
                                        )
                                    )
                                }
                            }
                        )
                    }
                },
            )
            SpacerW1()
        }
    }
}

@Composable
private fun ButtonView(
    buttonUi: HomeSettingsButtonUi,
    extraLeftWidth: Float,
    extraRightWidth: Float,
    content: @Composable () -> Unit,
) {

    ZStack(
        modifier = Modifier
            .size(
                width = (buttonUi.fullWidth + extraLeftWidth + extraRightWidth).dp,
                height = rowHeight,
            )
            .height(barHeight)
            .offset(
                x = (buttonUi.offsetX - extraLeftWidth).dp,
                y = buttonUi.offsetY.dp,
            )
            .clip(roundedShape)
            .background(buttonUi.colorRgba.toColor())
    ) {
        content()
    }
}
