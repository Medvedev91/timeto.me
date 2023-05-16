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
    onPick: (ColorRgba) -> Unit,
) {

    val (vm, state) = rememberVM { ColorPickerSheetVM(selectedColor) }

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

        Column {

            val circlesListHPadding = 22.dp

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

                                Column(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .size(40.dp)
                                        .border(onePx, c.text.copy(0.1f), RoundedCornerShape(99.dp))
                                        .clip(RoundedCornerShape(99.dp))
                                        .background(colorItem.colorRgba.toColor())
                                        .clickable {
                                            vm.upColorRgba(colorItem.colorRgba)
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                ) {
                                    AnimatedVisibility(
                                        visible = colorItem.isSelected,
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                    ) {
                                        IconSelected()
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
                    .navigationBarsPadding()
                    .padding(top = 4.dp)
                    .padding(horizontal = circlesListHPadding),
            ) {
                state.colorHintGroups.forEach { colorHints ->
                    Row {
                        colorHints.forEach { colorHint ->

                            if (colorHints.first() != colorHint)
                                SpacerW1()

                            Box(
                                Modifier
                                    .padding(vertical = 4.dp)
                                    .size(40.dp)
                                    .border(onePx, c.text.copy(0.1f), RoundedCornerShape(99.dp))
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(colorHint.colorRgba.toColor())
                                    .clickable(colorHint.emoji == null) {
                                        vm.toggleIsRgbSlidersShowed()
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                val emoji = colorHint.emoji
                                if (emoji != null)
                                    Text(
                                        text = emoji,
                                        fontSize = 16.sp,
                                    )
                                else
                                    IconSelected()
                            }
                        }

                        for (i in 0 until (state.circlesInRow - colorHints.size)) {
                            SpacerW1()
                            Box(Modifier.width(40.dp))
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
        }
    }
}

@Composable
private fun IconSelected() {
    Icon(
        Icons.Rounded.Done,
        contentDescription = "Selected",
        modifier = Modifier
            .size(24.dp),
        tint = c.white,
    )
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
