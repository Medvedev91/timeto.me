package me.timeto.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.*
import me.timeto.shared.vm.ActivityColorSheetVm

private val circleSize = 40.dp
private val circlePadding = 4.dp
private val dividerPadding = H_PADDING.goldenRatioDown()

private val bgColor = c.sheetBg
private val dividerColor = c.sheetDividerBg

@Composable
fun ActivityColorSheet(
    layer: WrapperView.Layer,
    initData: ActivityColorSheetVm.InitData,
    onPick: (ColorRgba) -> Unit,
) {

    val (vm, state) = rememberVm { ActivityColorSheetVm(initData) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(bgColor)
    ) {

        val circleScrollState = rememberScrollState()
        val activitiesScrollState = rememberScrollState()

        Sheet__HeaderView(
            title = state.headerTitle,
            scrollState = null,
            bgColor = bgColor,
        )

        val alphaAnimate = animateFloatAsState(remember {
            derivedStateOf {
                if (circleScrollState.canScrollBackward ||
                    activitiesScrollState.canScrollBackward
                ) 1f else 0f
            }
        }.value)

        ZStack(
            modifier = Modifier
                .height(onePx)
                .fillMaxWidth()
                .drawBehind {
                    drawRect(color = dividerColor.copy(alpha = alphaAnimate.value))
                },
        )

        Row(
            modifier = Modifier
                .weight(1f),
        ) {

            Row(
                modifier = Modifier
                    .verticalScroll(state = activitiesScrollState)
                    .padding(top = 4.dp)
                    .padding(start = H_PADDING)
                    .height(IntrinsicSize.Max)
                    .weight(1f),
            ) {

                val activitiesBottomPadding = 16.dp

                Column(
                    modifier = Modifier
                        .padding(end = dividerPadding, bottom = activitiesBottomPadding)
                        .weight(1f)
                ) {

                    Text(
                        text = state.title,
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .height(circleSize - 2.dp)
                            .clip(squircleShape)
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
                        color = state.otherActivitiesTitleColor.toColor(),
                    )

                    state.allActivities.forEach { activityUI ->
                        Text(
                            text = activityUI.text,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(squircleShape)
                                .clickable {
                                    vm.upColorRgba(activityUI.colorRgba)
                                }
                                .background(activityUI.colorRgba.toColor())
                                .padding(start = 7.dp, end = 8.dp, top = 4.dp, bottom = 4.5.dp),
                            fontSize = 13.sp,
                            color = c.white,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Box(
                    Modifier
                        .width(onePx)
                        .padding(top = circlePadding, bottom = activitiesBottomPadding)
                        .fillMaxHeight()
                        .background(dividerColor)
                )
            }

            Column(
                modifier = Modifier
                    .verticalScroll(state = circleScrollState)
                    .padding(
                        start = dividerPadding - circlePadding,
                        end = H_PADDING - circlePadding,
                        bottom = 20.dp,
                    ),
            ) {

                state.colorGroups.forEach { colors ->
                    Row {
                        colors.forEach { colorItem ->
                            Row(
                                modifier = Modifier
                                    .size(circleSize + (circlePadding * 2))
                                    .padding(circlePadding)
                                    .clip(roundedShape)
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
            }
        }

        val rgbButtonColor = animateColorAsState(if (state.isRgbSlidersShowed) c.blue else c.transparent)

        Sheet__BottomViewDefault(
            primaryText = state.doneTitle,
            primaryAction = {
                onPick(state.selectedColor)
                layer.close()
            },
            secondaryText = "Cancel",
            secondaryAction = {
                layer.close()
            },
            topContent = {

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
                                .background(c.bg)
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
                                        .clip(squircleShape)
                                        .background(state.selectedColor.toColor())
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                    color = c.white,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Light,
                                )
                            }

                            ColorSliderView(state.r, c.red, state.isRgbSlidersAnimated) { vm.upR(it) }
                            ColorSliderView(state.g, c.green, state.isRgbSlidersAnimated) { vm.upG(it) }
                            ColorSliderView(state.b, c.blue, state.isRgbSlidersAnimated) { vm.upB(it) }
                        }
                    }
                }
            },
            startContent = {
                Icon(
                    if (state.isRgbSlidersShowed) Icons.Rounded.ExpandMore else Icons.Rounded.Tune,
                    "RGB Picker",
                    tint = state.rgbSlidersBtnColor.toColor(),
                    modifier = Modifier
                        .padding(start = H_PADDING - 2.dp)
                        .size(33.dp)
                        .clip(roundedShape)
                        .drawBehind {
                            drawCircle(rgbButtonColor.value)
                        }
                        .clickable {
                            vm.toggleIsRgbSlidersShowed()
                        }
                        .padding(4.dp)
                )
            }
        )
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
        modifier = Modifier.padding(horizontal = H_PADDING - 6.dp),
        valueRange = 0f..255f,
        colors = SliderDefaults.colors(
            thumbColor = color,
            activeTrackColor = color,
        )
    )
}
