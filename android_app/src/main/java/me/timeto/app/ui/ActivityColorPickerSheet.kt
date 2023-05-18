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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.R
import me.timeto.app.onePx
import me.timeto.app.rememberVM
import me.timeto.app.toColor
import me.timeto.shared.ColorRgba
import me.timeto.shared.GOLDEN_RATIO
import me.timeto.shared.vm.ActivityColorPickerSheetVM

private val circleSize = 40.dp
private val sheetHPaddings = 20.dp

private val circlePadding = 4.dp
private val dividerPadding = (sheetHPaddings.value / GOLDEN_RATIO).dp

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

        val circleScrollState = rememberLazyListState()
        val activitiesScrollState = rememberScrollState()

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

        DividerBg2(isVisible = circleScrollState.canScrollBackward || activitiesScrollState.canScrollBackward)

        Row(
            modifier = Modifier
                .weight(1f),
        ) {

            Column(
                modifier = Modifier
                    .verticalScroll(state = activitiesScrollState)
                    .padding(top = 4.dp)
                    .padding(start = sheetHPaddings, end = dividerPadding)
                    .weight(1f),
            ) {

                Text(
                    text = state.title,
                    modifier = Modifier
                        .clip(MySquircleShape())
                        .background(state.selectedColor.toColor())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    color = c.white,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = state.otherActivitiesTitle,
                    modifier = Modifier
                        .padding(start = 4.dp, top = 20.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = c.textSecondary,
                )

                state.allActivities.forEach { activityUI ->
                    Text(
                        text = activityUI.text,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clip(MySquircleShape(len = 40f))
                            .background(activityUI.colorRgba.toColor())
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Light,
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
                    .padding(vertical = circlePadding)
                    .fillMaxHeight()
                    .background(c.dividerBg2)
            )

            LazyColumn(
                state = circleScrollState,
                contentPadding = PaddingValues(
                    top = 4.dp,
                    start = dividerPadding - circlePadding,
                    end = sheetHPaddings - circlePadding,
                )
            ) {

                state.colorGroups.forEach { colors ->
                    item {
                        Row {
                            colors.forEach { colorItem ->
                                Row(
                                    modifier = Modifier
                                        .size(circleSize + circlePadding * 2),
                                    horizontalArrangement = Arrangement.Center,
                                ) {

                                    ActivityColorPickerSheet__CircleView(
                                        color = colorItem.colorRgba.toColor(),
                                        size = circleSize,
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
                }

                item {
                    Text(
                        text = "Custom",
                        modifier = Modifier
                            .padding(top = 2.dp)
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
        }

        Column {

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
                        .pointerInput(Unit) { }
                        .navigationBarsPadding(),
                ) {

                    DividerBg2()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 18.dp, bottom = 4.dp),
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
                            painterResource(R.drawable.ic_round_close_24),
                            "Hide",
                            tint = c.textSecondary,
                            modifier = Modifier
                                .alpha(0.7f)
                                .align(Alignment.CenterEnd)
                                .padding(end = sheetHPaddings)
                                .size(30.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(c.background2)
                                .clickable {
                                    vm.toggleIsRgbSlidersShowed()
                                }
                                .padding(4.dp)
                        )
                    }

                    ColorSlider(state.r, c.red, state.isRgbSlidersAnimated) { vm.upR(it) }
                    ColorSlider(state.g, c.green, state.isRgbSlidersAnimated) { vm.upG(it) }
                    ColorSlider(state.b, c.blue, state.isRgbSlidersAnimated) { vm.upB(it) }
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
            // TRICK clickable(false) overrides parent click
            .then(
                if (onClick != null)
                    Modifier.clickable { onClick() }
                else Modifier
            ),
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
        modifier = Modifier.padding(horizontal = sheetHPaddings - 4.dp),
        valueRange = 0f..255f,
        colors = SliderDefaults.colors(
            thumbColor = color,
            activeTrackColor = color,
        )
    )
}
