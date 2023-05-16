package me.timeto.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.onePx
import me.timeto.app.rememberVM
import me.timeto.app.toColor
import me.timeto.shared.ColorRgba
import me.timeto.shared.vm.ColorPickerSheetVM

@Composable
fun ColorPickerSheet(
    layer: WrapperView.Layer,
    selectedColor: ColorRgba,
    text: String,
    onPick: (ColorRgba) -> Unit,
) {

    val (vm, state) = rememberVM { ColorPickerSheetVM(selectedColor, text) }

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
            scrollToHeader = if (scrollState.canScrollBackward) 99 else 0,
            maxLines = 1,
        ) {
            onPick(state.getSelectedColor())
            layer.close()
        }

        Column(
            modifier = Modifier
                .navigationBarsPadding()
        ) {

            val circlesListHPadding = 15.dp

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
                                Box(
                                    Modifier
                                        .weight(1f),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        Modifier
                                            .padding(vertical = 4.dp)
                                            .size(40.dp)
                                            .border(onePx, c.text.copy(0.1f), RoundedCornerShape(99.dp))
                                            .clip(RoundedCornerShape(99.dp))
                                            // todo remember map colorGroups for color.toColor() ?
                                            .background(colorItem.colorRgba.toColor())
                                            .clickable {
                                                vm.upColorRgba(colorItem.colorRgba)
                                            }
                                    ) {

                                        if (colorItem.isSelected)
                                            Icon(
                                                Icons.Rounded.Done,
                                                contentDescription = "Selected",
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .size(24.dp, 24.dp),
                                                tint = c.white,
                                            )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Divider(color = c.dividerBackground2)

            Column(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .padding(horizontal = circlesListHPadding),
            ) {
                state.activityUIGroups.forEach { activitiesUI ->
                    Row {
                        activitiesUI.forEach { activityUI ->
                            Box(
                                Modifier
                                    .weight(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    Modifier
                                        .padding(vertical = 4.dp)
                                        .size(40.dp)
                                        .border(onePx, c.text.copy(0.1f), RoundedCornerShape(99.dp))
                                        .clip(RoundedCornerShape(99.dp))
                                        .background(activityUI.colorRgba.toColor())
                                        .clickable {
                                            vm.upColorRgba(activityUI.colorRgba)
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = activityUI.emoji,
                                        fontSize = 18.sp,
                                    )
                                }
                            }
                        }

                        val emptyCircles = state.circlesInRow - activitiesUI.size
                        if (emptyCircles > 0)
                            Box(Modifier.weight(emptyCircles.toFloat()))
                    }
                }
            }

            Text(
                text = state.text,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp, bottom = 2.dp)
                    .border(onePx, c.text.copy(0.1f), RoundedCornerShape(99.dp))
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(state.r.toInt(), state.g.toInt(), state.b.toInt()))
                    .clickable {
                        vm.toggleIsRgbSlidersShowed()
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = state.textColor.toColor(),
                fontSize = 15.sp,
            )

            Text(
                text = state.rgbText,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(MySquircleShape())
                    .clickable {
                        vm.toggleIsRgbSlidersShowed()
                    }
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                color = c.blue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
            )

            AnimatedVisibility(
                visible = state.isRgbSlidersShowed,
                enter = expandVertically(spring(stiffness = Spring.StiffnessMedium)),
                exit = shrinkVertically(spring(stiffness = Spring.StiffnessMedium)),
            ) {
                Column(
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    ColorSlider(state.r, c.red, state.isRgbSlidersAnimated) { vm.upR(it) }
                    ColorSlider(state.g, c.green, state.isRgbSlidersAnimated) { vm.upG(it) }
                    ColorSlider(state.b, c.blue, state.isRgbSlidersAnimated) { vm.upB(it) }
                }
            }
        }
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
        modifier = Modifier.padding(horizontal = 16.dp),
        valueRange = 0f..255f,
        colors = SliderDefaults.colors(
            thumbColor = color,
            activeTrackColor = color,
        )
    )
}
