package me.timeto.app.ui.color

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.HStack
import me.timeto.app.H_PADDING
import me.timeto.app.H_PADDING_HALF
import me.timeto.app.R
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.c
import me.timeto.app.goldenRatioDown
import me.timeto.app.onePx
import me.timeto.app.ui.rememberVm
import me.timeto.app.roundedShape
import me.timeto.app.squircleShape
import me.timeto.app.toColor
import me.timeto.app.ui.Divider
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.footer.Footer
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.ColorRgba
import me.timeto.shared.ui.color.ColorPickerExamplesUi
import me.timeto.shared.ui.color.ColorPickerVm

private val circleSize = 40.dp
private val circlePadding = 4.dp
private val dividerPadding: Dp = H_PADDING.goldenRatioDown()

@Composable
fun ColorPickerFs(
    title: String,
    examplesUi: ColorPickerExamplesUi,
    onDone: (ColorRgba) -> Unit,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ColorPickerVm(
            examplesUi = examplesUi,
        )
    }

    Screen {

        val circlesScrollState = rememberScrollState()
        val activitiesScrollState = rememberScrollState()

        Header(
            title = title,
            scrollState = null,
            actionButton = HeaderActionButton(
                text = state.saveText,
                isEnabled = true,
                onClick = {
                    onDone(state.colorRgba)
                    navigationLayer.close()
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            )
        )

        // todo show divider on scroll (x2)

        HStack(
            modifier = Modifier
                .weight(1f),
        ) {

            HStack(
                modifier = Modifier
                    .verticalScroll(state = activitiesScrollState)
                    .padding(top = 4.dp)
                    .padding(start = H_PADDING)
                    .height(IntrinsicSize.Max)
                    .weight(1f),
            ) {

                val activitiesBottomPadding = 16.dp

                VStack(
                    modifier = Modifier
                        .padding(end = dividerPadding, bottom = activitiesBottomPadding)
                        .weight(1f)
                ) {

                    Text(
                        text = examplesUi.mainExampleUi.title,
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .height(circleSize - 2.dp)
                            .clip(squircleShape)
                            .background(state.colorRgba.toColor())
                            .wrapContentHeight(Alignment.CenterVertically)
                            .padding(start = 11.dp, end = 13.dp),
                        color = c.white,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = examplesUi.secondaryHeader,
                        modifier = Modifier
                            .padding(start = 4.dp, top = 24.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = c.textSecondary,
                    )

                    examplesUi.secondaryExamplesUi.forEach { exampleUi ->
                        Text(
                            text = exampleUi.title,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(squircleShape)
                                .clickable {
                                    vm.setColorRgba(exampleUi.colorRgba)
                                }
                                .background(exampleUi.colorRgba.toColor())
                                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                            fontSize = 13.sp,
                            color = c.white,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                ZStack(
                    Modifier
                        .width(onePx)
                        .padding(top = circlePadding, bottom = activitiesBottomPadding)
                        .fillMaxHeight()
                        .background(c.divider),
                )
            }

            VStack(
                modifier = Modifier
                    .verticalScroll(state = circlesScrollState)
                    .padding(
                        start = dividerPadding - circlePadding,
                        end = H_PADDING - circlePadding,
                        bottom = 20.dp,
                    ),
            ) {
                state.colorGroups.forEach { colors ->
                    HStack {
                        colors.forEach { colorItem ->
                            HStack(
                                modifier = Modifier
                                    .size(circleSize + (circlePadding * 2))
                                    .padding(circlePadding)
                                    .clip(roundedShape)
                                    .background(colorItem.colorRgba.toColor())
                                    .clickable {
                                        vm.setColorRgba(colorItem.colorRgba)
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
                                        painter = painterResource(id = R.drawable.sf_checkmark_medium_medium),
                                        contentDescription = "Selected",
                                        modifier = Modifier
                                            .size(16.dp),
                                        tint = c.white,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // todo hide on scroll (two scrolls)
        Divider()

        Footer(
            scrollState = null,
            contentModifier = Modifier,
            content = {
                SpacerW1()
                Text(
                    text = "Custom Color",
                    modifier = Modifier
                        .padding(end = H_PADDING_HALF)
                        .clip(squircleShape)
                        .clickable {
                            navigationFs.push {
                                ColorPickerCustomSheet(
                                    initColorRgba = state.colorRgba,
                                    onDone = { newColorRgba ->
                                        vm.setColorRgba(colorRgba = newColorRgba)
                                    },
                                )
                            }
                        }
                        .padding(horizontal = H_PADDING_HALF, vertical = 4.dp),
                    color = c.blue,
                )
            },
        )
    }
}
