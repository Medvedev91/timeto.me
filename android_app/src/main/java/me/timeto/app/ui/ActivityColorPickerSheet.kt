package me.timeto.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.ExpandCircleDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.goldenRatioDown
import me.timeto.app.onePx
import me.timeto.app.rememberVM
import me.timeto.app.toColor
import me.timeto.shared.ColorRgba
import me.timeto.shared.vm.ActivityColorPickerSheetVM

private val circleSize = 40.dp
private val circlePadding = 4.dp
private val sheetHPadding = MyListView.PADDING_OUTER_HORIZONTAL
private val dividerPadding = sheetHPadding.goldenRatioDown()

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
            .navigationBarsPadding()
    ) {

        val circleScrollState = rememberScrollState()
        val activitiesScrollState = rememberScrollState()

        Sheet.HeaderView(
            onCancel = { layer.close() },
            title = state.headerTitle,
            doneText = state.doneTitle,
            isDoneEnabled = true,
            scrollToHeader = (circleScrollState.value + activitiesScrollState.value) * 4,
            bgColor = c.background2,
            dividerColor = c.dividerBg2,
            maxLines = 1,
        ) {
            onPick(state.selectedColor)
            layer.close()
        }

        Row(
            modifier = Modifier
                .weight(1f),
        ) {

            Row(
                modifier = Modifier
                    .verticalScroll(state = activitiesScrollState)
                    .padding(top = circlePadding)
                    .padding(start = sheetHPadding)
                    .height(IntrinsicSize.Max)
                    .weight(1f),
            ) {

                Column(
                    modifier = Modifier
                        .padding(end = dividerPadding)
                        .weight(1f)
                ) {

                    Text(
                        text = state.title,
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .height(circleSize - 2.dp)
                            .clip(MySquircleShape(len = 70f))
                            .background(state.selectedColor.toColor())
                            .wrapContentHeight(Alignment.CenterVertically)
                            .padding(start = 11.dp, end = 13.dp),
                        color = c.white,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = state.otherActivitiesTitle,
                        modifier = Modifier
                            .padding(start = 4.dp, top = 24.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = c.gray1,
                    )

                    state.allActivities.forEach { activityUI ->
                        Text(
                            text = activityUI.text,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(MySquircleShape())
                                .background(activityUI.colorRgba.toColor())
                                .padding(start = 7.dp, end = 8.dp, top = 4.dp, bottom = 4.5.dp),
                            fontSize = 13.sp,
                            color = c.white,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Box(Modifier.navigationBarsPadding())
                }

                Box(
                    Modifier
                        .width(onePx)
                        .padding(top = circlePadding)
                        .navigationBarsPadding()
                        .fillMaxHeight()
                        .background(c.dividerBg2)
                )
            }

            Column(
                modifier = Modifier
                    .verticalScroll(state = circleScrollState)
                    .padding(
                        start = dividerPadding - circlePadding,
                        end = sheetHPadding - circlePadding,
                    ),
            ) {

                state.colorGroups.forEach { colors ->
                    Row {
                        colors.forEach { colorItem ->
                            Row(
                                modifier = Modifier
                                    .size(circleSize + (circlePadding * 2))
                                    .padding(circlePadding)
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(colorItem.colorRgba.toColor())
                                    .clickable {
                                        vm.upColorRgba(colorItem.colorRgba)
                                    },
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
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
                            }
                        }
                    }
                }

                Text(
                    text = "Custom",
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .navigationBarsPadding()
                        .clip(MySquircleShape())
                        .clickable {
                            vm.toggleIsRgbSlidersShowed()
                        }
                        .padding(horizontal = circlePadding, vertical = 2.dp),
                    color = c.blue,
                    fontSize = 14.sp,
                )
            }
        }

        DividerBg2()

        Column {

            BackHandler(state.isRgbSlidersShowed) {
                vm.toggleIsRgbSlidersShowed()
            }

            AnimatedVisibility(
                visible = state.isRgbSlidersShowed,
                enter = expandVertically(spring(stiffness = Spring.StiffnessMedium))
                        + fadeIn(spring(stiffness = Spring.StiffnessMedium)),
                exit = shrinkVertically(spring(stiffness = Spring.StiffnessMedium))
                       + fadeOut(spring(stiffness = Spring.StiffnessHigh)),
            ) {
                Column(
                    modifier = Modifier
                        .background(c.background2)
                        .pointerInput(Unit) { },
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                    ) {

                        Text(
                            text = state.rgbText,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(MySquircleShape())
                                .background(state.selectedColor.toColor())
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            color = c.white,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                        )

                        Icon(
                            Icons.Rounded.ExpandCircleDown,
                            "Hide",
                            tint = c.iconButtonBg1,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = sheetHPadding)
                                .size(30.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(c.background2)
                                .clickable {
                                    vm.toggleIsRgbSlidersShowed()
                                }
                        )
                    }

                    ColorSliderView(state.r, c.red, state.isRgbSlidersAnimated) { vm.upR(it) }
                    ColorSliderView(state.g, c.green, state.isRgbSlidersAnimated) { vm.upG(it) }
                    ColorSliderView(state.b, c.blue, state.isRgbSlidersAnimated) { vm.upB(it) }
                }
            }
        }
    }
}

@Composable
private fun ColorSliderView(
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
        modifier = Modifier.padding(horizontal = sheetHPadding - 4.dp),
        valueRange = 0f..255f,
        colors = SliderDefaults.colors(
            thumbColor = color,
            activeTrackColor = color,
        )
    )
}
