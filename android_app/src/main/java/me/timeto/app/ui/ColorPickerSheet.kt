package me.timeto.app.ui

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

            LazyColumn(
                modifier = Modifier
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 8.dp)
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

            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 8.dp)
                    .clip(MySquircleShape())
                    .fillMaxWidth()
                    .background(Color(state.r.toInt(), state.g.toInt(), state.b.toInt()))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {

                Text(
                    text = state.rgbText,
                    color = c.white,
                    fontSize = 15.sp,
                )
            }

            ColorSlider(state.r, c.red) { vm.upR(it) }
            ColorSlider(state.g, c.green) { vm.upG(it) }
            ColorSlider(state.b, c.blue) { vm.upB(it) }
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
        modifier = Modifier.padding(horizontal = 2.dp),
        valueRange = 0f..255f,
        colors = SliderDefaults.colors(
            thumbColor = color,
            activeTrackColor = color,
        )
    )
}
