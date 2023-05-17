package me.timeto.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.R
import me.timeto.app.onePx
import me.timeto.app.rememberVM
import me.timeto.app.toColor
import me.timeto.shared.ColorRgba
import me.timeto.shared.vm.ActivityColorPickerSheetVM

private val circleSize = 40.dp
private val circlesListHPadding = 22.dp
private val circlePaddingValues = PaddingValues(vertical = 4.dp)

@Composable
fun ActivityColorPickerSheet(
    layer: WrapperView.Layer,
    initData: ActivityColorPickerSheetVM.InitData,
    onPick: (ColorRgba) -> Unit,
) {

    val (vm, state) = rememberVM { ActivityColorPickerSheetVM(initData) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.background2)
    ) {

        val scrollState = rememberLazyListState()

        Sheet.HeaderView(
            onCancel = { layer.close() },
            title = state.headerTitle,
            doneText = state.doneTitle,
            isDoneEnabled = true,
            scrollToHeader = 0,
            maxLines = 1,
        ) {
            onPick(state.selectedColor)
            layer.close()
        }

        Column {

            Column(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .padding(horizontal = circlesListHPadding),
            ) {
                state.menuButtonGroups.forEach { menuButtons ->
                    Row {
                        menuButtons.forEach { menuButton ->

                            if (menuButtons.first() != menuButton)
                                SpacerW1()

                            when (menuButton) {
                                is ActivityColorPickerSheetVM.MenuButton.Activity -> {
                                    ActivityColorPickerSheet__CircleView(
                                        color = menuButton.colorRgba.toColor(),
                                        size = circleSize,
                                        padding = circlePaddingValues,
                                        content = {
                                            Text(
                                                text = menuButton.emoji,
                                                fontSize = 16.sp,
                                            )
                                        },
                                    )
                                }
                                is ActivityColorPickerSheetVM.MenuButton.NewActivity -> {
                                    ActivityColorPickerSheet__CircleView(
                                        color = state.selectedColor.toColor(),
                                        size = circleSize,
                                        padding = circlePaddingValues,
                                        content = {
                                            Text(
                                                text = menuButton.emoji ?: "",
                                                fontSize = 16.sp,
                                            )
                                        },
                                    )
                                }
                                is ActivityColorPickerSheetVM.MenuButton.RgbSlider -> {
                                    Icon(
                                        painterResource(R.drawable.sf_slider_horizontal_3_medium_medium),
                                        contentDescription = "Rgb Slider",
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .size(circleSize)
                                            .clip(RoundedCornerShape(99.dp))
                                            .clickable {
                                                vm.toggleIsRgbSlidersShowed()
                                            }
                                            .padding(11.dp),
                                        tint = c.textSecondary,
                                    )
                                }
                            }
                        }

                        for (i in 0 until (state.circlesInRow - menuButtons.size)) {
                            SpacerW1()
                            Box(Modifier.width(circleSize))
                        }
                    }
                }

                AnimatedVisibility(
                    visible = state.isRgbSlidersShowed,
                    enter = expandVertically(spring(stiffness = Spring.StiffnessMedium)),
                    exit = shrinkVertically(spring(stiffness = Spring.StiffnessMedium)),
                ) {
                    Column {

                        Text(
                            text = state.rgbText,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 20.dp, bottom = 4.dp),
                            color = c.blue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                        )

                        ColorSlider(state.r, c.red, state.isRgbSlidersAnimated) { vm.upR(it) }
                        ColorSlider(state.g, c.green, state.isRgbSlidersAnimated) { vm.upG(it) }
                        ColorSlider(state.b, c.blue, state.isRgbSlidersAnimated) { vm.upB(it) }
                    }
                }
            }

            Divider(color = c.dividerBackground2)

            LazyColumn(
                modifier = Modifier
                    .weight(1f),
                state = scrollState,
                contentPadding = PaddingValues(horizontal = circlesListHPadding, vertical = 8.dp)
            ) {

                state.colorGroups.forEach { colors ->
                    item {
                        Row {
                            colors.forEach { colorItem ->

                                if (colors.first() != colorItem)
                                    SpacerW1()

                                ActivityColorPickerSheet__CircleView(
                                    color = colorItem.colorRgba.toColor(),
                                    size = circleSize,
                                    padding = circlePaddingValues,
                                    content = {
                                        AnimatedVisibility(
                                            visible = colorItem.isSelected,
                                            enter = fadeIn(),
                                            exit = fadeOut(),
                                        ) {
                                            Icon(
                                                Icons.Rounded.Done,
                                                contentDescription = "Selected",
                                                modifier = Modifier
                                                    .size(24.dp),
                                                tint = c.white,
                                            )
                                        }
                                    },
                                    onClick = {
                                        vm.upColorRgba(colorItem.colorRgba)
                                    },
                                )
                            }
                        }
                    }
                }

                item {
                    Box(Modifier.navigationBarsPadding())
                }
            }

        }
    }
}

@Composable
fun ActivityColorPickerSheet__CircleView(
    color: Color,
    size: Dp,
    padding: PaddingValues = PaddingValues(),
    content: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Box(
        Modifier
            .padding(padding)
            .size(size)
            .border(onePx, c.text.copy(0.1f), RoundedCornerShape(99.dp))
            .clip(RoundedCornerShape(99.dp))
            .background(color)
            .clickable(onClick != null) {
                onClick?.invoke()
            },
        contentAlignment = Alignment.Center,
    ) {
        content?.invoke()
    }
}

@Composable
private fun ColorSlider(
    value: Float,
    color: Color,
    isAnimated: Boolean,
    onChange: (Float) -> Unit,
) {
    val animatedValue = animateFloatAsState(value)
    Slider(
        // Animation works bad with manual slide
        value = if (isAnimated) animatedValue.value else value,
        onValueChange = { onChange(it) },
        valueRange = 0f..255f,
        colors = SliderDefaults.colors(
            thumbColor = color,
            activeTrackColor = color,
        )
    )
}
