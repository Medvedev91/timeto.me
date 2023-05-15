package me.timeto.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.rememberVM
import me.timeto.app.toColor
import me.timeto.shared.ColorRgba
import me.timeto.shared.vm.ColorPickerSheetVM

@Composable
fun ColorPickerSheet(
    layer: WrapperView.Layer,
    selectedColor: ColorRgba,
    onPick: (ColorRgba) -> Unit,
) {

    val (vm, state) = rememberVM { ColorPickerSheetVM(selectedColor) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.background2)
    ) {

        Sheet.HeaderView(
            onCancel = { layer.close() },
            title = state.headerTitle,
            doneText = state.doneTitle,
            isDoneEnabled = true,
            scrollToHeader = 99,
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
                contentPadding = PaddingValues(horizontal = circlesListHPadding, vertical = 8.dp)
            ) {
                state.colorGroups.forEach { colors ->
                    item {
                        Row {
                            colors.forEach { colorRgba ->
                                Box(
                                    Modifier
                                        .weight(1f),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Box(
                                        Modifier
                                            .padding(vertical = 4.dp)
                                            .size(40.dp)
                                            .border(1.dp, c.text.copy(0.1f), RoundedCornerShape(99.dp))
                                            .clip(RoundedCornerShape(99.dp))
                                            // todo remember map colorGroups for color.toColor() ?
                                            .background(colorRgba.toColor())
                                            .clickable {
                                                vm.upColorRgba(colorRgba)
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Divider()

            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
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
                                        .border(1.dp, c.text.copy(0.1f), RoundedCornerShape(99.dp))
                                        .clip(RoundedCornerShape(99.dp))
                                        // todo remember map colorGroups for color.toColor() ?
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

            Row(
                modifier = Modifier
                    .padding(horizontal = 22.dp, vertical = 8.dp)
                    .clip(MySquircleShape())
                    .fillMaxWidth()
                    .background(Color(state.r.toInt(), state.g.toInt(), state.b.toInt()))
                    .clickable {
                        vm.toggleIsRgbSlidersShowed()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {

                Text(
                    text = state.rgbText,
                    color = c.white,
                    fontSize = 15.sp,
                )
            }

            AnimatedVisibility(
                visible = state.isRgbSlidersShowed,
            ) {
                Column(
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    ColorSlider(state.r, c.red) { vm.upR(it) }
                    ColorSlider(state.g, c.green) { vm.upG(it) }
                    ColorSlider(state.b, c.blue) { vm.upB(it) }
                }
            }
        }
    }
}

@Composable
private fun ColorSlider(
    value: Float,
    color: Color,
    onChange: (Float) -> Unit,
) {
    Slider(
        value = value,
        onValueChange = { onChange(it) },
        modifier = Modifier.padding(horizontal = 16.dp),
        valueRange = 0f..255f,
        colors = SliderDefaults.colors(
            thumbColor = color,
            activeTrackColor = color,
        )
    )
}
