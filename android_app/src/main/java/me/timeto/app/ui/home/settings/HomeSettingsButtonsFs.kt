package me.timeto.app.ui.home.settings

import android.view.MotionEvent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.timeto.app.Haptic
import me.timeto.app.MainActivity
import me.timeto.app.toColor
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.H_PADDING_HALF
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.dpToPx
import me.timeto.app.ui.footer.Footer
import me.timeto.app.ui.footer.FooterPlainButton
import me.timeto.app.ui.goals.form.Goal2FormFs
import me.timeto.app.ui.home.HomeScreen__itemCircleFontSize
import me.timeto.app.ui.home.HomeScreen__itemCircleFontWeight
import me.timeto.app.ui.home.HomeScreen__itemCircleHeight
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.pxToDp
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.roundedShape
import me.timeto.shared.vm.home.settings.buttons.HomeSettingsButtonType
import me.timeto.shared.vm.home.settings.buttons.HomeSettingsButtonUi
import me.timeto.shared.vm.home.settings.buttons.HomeSettingsButtonsVm
import kotlin.math.roundToInt

private val rowHeight: Dp = HomeScreen__itemHeight
private val barHeight: Dp = HomeScreen__itemCircleHeight
private val spacing: Dp = 10.dp

private val resizeButtonViewArcRadiusDp: Dp = barHeight / 2
private val resizeButtonViewArcLineWidthDp: Dp = 6.dp
private val resizeButtonArcHeight: Dp = barHeight - (resizeButtonViewArcLineWidthDp)

private val buttonsHPadding: Dp = H_PADDING

@Composable
fun HomeSettingsButtonsFs() {

    val mainActivity = LocalActivity.current as MainActivity

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current

    val (vm, state) = rememberVm {
        HomeSettingsButtonsVm(
            spacing = spacing.value,
            rowHeight = rowHeight.value,
            width = (configuration.screenWidthDp.dp.value - (buttonsHPadding.value * 2)),
        )
    }

    val hoverButtonsUi = remember {
        mutableStateOf(listOf<HomeSettingsButtonUi>())
    }

    val isFirstRun = remember { mutableStateOf(true) }
    val ignoreNextHaptic = remember { mutableStateOf(true) }
    LaunchedEffect(hoverButtonsUi.value) {
        if (isFirstRun.value) {
            isFirstRun.value = false
            return@LaunchedEffect
        }
        if (ignoreNextHaptic.value) {
            ignoreNextHaptic.value = false
            return@LaunchedEffect
        }
        Haptic.shot()
    }

    Screen {

        ZStack(
            modifier = Modifier
                .padding(horizontal = buttonsHPadding)
                .padding(top = mainActivity.statusBarHeightDp)
                .fillMaxWidth()
                .height(state.height.dp),
        ) {

            state.buttonsData.emptyButtonsUi.forEach { buttonUi ->
                key(buttonUi.id) {
                    ButtonView(
                        buttonUi = buttonUi,
                        zIndex = 1f,
                        offsetXY = XY(0f, 0f),
                        extraLeftWidth = 0f,
                        extraRightWidth = 0f,
                        content = {},
                    )
                }
            }

            state.buttonsData.headersUi.forEach { headerUi ->
                ZStack(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                        .offset(y = headerUi.offsetY.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = headerUi.title,
                        color = c.white,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            state.buttonsData.dataButtonsUi.forEach { buttonUi ->
                key(buttonUi.id) {
                    DragButtonView(
                        buttonUi = buttonUi,
                        onDrag = { xy ->
                            hoverButtonsUi.value = vm.getHoverButtonsUiOnDrag(
                                buttonUi = buttonUi,
                                x = xy.x,
                                y = xy.y,
                            )
                        },
                        onDragEnd = { xy ->
                            hoverButtonsUi.value = emptyList()
                            scope.launch {
                                // To run change for hoverButtonsUi before this
                                delay(500)
                                ignoreNextHaptic.value = true
                            }
                            vm.onButtonDragEnd(
                                buttonUi = buttonUi,
                                x = xy.x,
                                y = xy.y,
                            )
                        },
                        onResize = { left, right ->
                            hoverButtonsUi.value = vm.getHoverButtonsUiOnResize(
                                buttonUi = buttonUi,
                                left = left,
                                right = right,
                            )
                        },
                        onResizeEnd = { left, right ->
                            hoverButtonsUi.value = emptyList()
                            scope.launch {
                                // To run change for hoverButtonsUi before this
                                delay(500)
                                ignoreNextHaptic.value = true
                            }
                            vm.onButtonResizeEnd(
                                buttonUi = buttonUi,
                                left = left,
                                right = right,
                            )
                        }
                    )
                }
            }

            hoverButtonsUi.value.forEach { buttonUi ->
                key(buttonUi.id) {
                    ButtonView(
                        buttonUi = buttonUi,
                        zIndex = 2f,
                        offsetXY = XY(0f, 0f),
                        extraLeftWidth = 0f,
                        extraRightWidth = 0f,
                        content = {},
                    )
                }
            }
        }

        SpacerW1()

        Footer(
            scrollState = null,
            contentModifier = Modifier
                .padding(horizontal = H_PADDING_HALF),
        ) {

            FooterPlainButton(
                text = state.newGoalText,
                color = c.blue,
                fontWeight = FontWeight.SemiBold,
                onClick = {
                    navigationFs.push {
                        Goal2FormFs(
                            goalDb = null,
                        )
                    }
                },
            )

            SpacerW1()

            FooterPlainButton(
                text = "Done",
                color = c.blue,
                fontWeight = FontWeight.SemiBold,
                onClick = {
                    navigationLayer.close()
                },
            )
        }
    }
}

@Composable
private fun ButtonView(
    buttonUi: HomeSettingsButtonUi,
    zIndex: Float,
    offsetXY: XY,
    extraLeftWidth: Float,
    extraRightWidth: Float,
    content: @Composable () -> Unit,
) {

    ZStack(
        modifier = Modifier
            .zIndex(zIndex)
            .size(
                width = (buttonUi.fullWidth + extraLeftWidth + extraRightWidth).dp,
                height = rowHeight,
            )
            .offset(
                x = (buttonUi.offsetX - extraLeftWidth).dp + offsetXY.x.dp,
                y = buttonUi.offsetY.dp + offsetXY.y.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {

        ZStack(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(roundedShape)
                .background(buttonUi.colorRgba.toColor()),
            contentAlignment = Alignment.Center,
        ) {}

        val buttonType = buttonUi.type
        if (buttonType is HomeSettingsButtonType.Goal) {
            Text(
                text = buttonType.note,
                modifier = Modifier
                    .padding(horizontal = 12.dp),
                color = c.white,
                maxLines = 1,
                textAlign = TextAlign.Center,
                fontSize = HomeScreen__itemCircleFontSize,
                fontWeight = HomeScreen__itemCircleFontWeight,
            )
        }

        content()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DragButtonView(
    buttonUi: HomeSettingsButtonUi,
    onDrag: (XY) -> Unit,
    onDragEnd: (XY) -> Boolean,
    onResize: (left: Float, right: Float) -> Unit,
    onResizeEnd: (left: Float, right: Float) -> Boolean,
) {
    val zIndex = remember { mutableFloatStateOf(3f) }
    val dragLocalXY = remember { mutableStateOf(XY(0f, 0f)) }
    fun buildGlobalXY() = XY(
        dragLocalXY.value.x + buttonUi.offsetX,
        dragLocalXY.value.y + buttonUi.offsetY,
    )

    val resizeOffsetLeft = remember { mutableFloatStateOf(0f) }
    val resizeOffsetRight = remember { mutableFloatStateOf(0f) }

    ButtonView(
        buttonUi = buttonUi,
        zIndex = zIndex.floatValue,
        offsetXY = dragLocalXY.value,
        extraLeftWidth = resizeOffsetLeft.floatValue,
        extraRightWidth = resizeOffsetRight.floatValue,
        content = {
            ZStack(
                modifier = Modifier
                    .fillMaxSize()
                    .motionEventSpy { event ->
                        if (event.action == MotionEvent.ACTION_DOWN)
                            Haptic.shot()
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                zIndex.floatValue = 3f
                                onDragEnd(buildGlobalXY())
                            },
                            onDrag = { change, dragAmount ->
                                zIndex.floatValue = 4f
                                change.consume()
                                val xDp = pxToDp(dragAmount.x.roundToInt())
                                val yDp = pxToDp(dragAmount.y.roundToInt())
                                val newLocalXY = XY(dragLocalXY.value.x + xDp, dragLocalXY.value.y + yDp)
                                dragLocalXY.value = newLocalXY
                                onDrag(buildGlobalXY())
                            },
                        )
                    },
            ) {
                HStack(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ResizeButtonView(
                        modifier = Modifier,
                        onResize = { delta ->
                            resizeOffsetLeft.floatValue -= pxToDp(delta.roundToInt())
                            onResize(resizeOffsetLeft.floatValue, resizeOffsetRight.floatValue)
                        },
                        onResizeEnd = { delta ->
                            if (!onResizeEnd(resizeOffsetLeft.floatValue, resizeOffsetRight.floatValue))
                                resizeOffsetLeft.floatValue = 0f
                        },
                    )
                    SpacerW1()
                    ResizeButtonView(
                        modifier = Modifier
                            .rotate(180f),
                        onResize = { delta ->
                            resizeOffsetRight.floatValue -= pxToDp(delta.roundToInt())
                            onResize(resizeOffsetLeft.floatValue, resizeOffsetRight.floatValue)
                        },
                        onResizeEnd = { delta ->
                            if (!onResizeEnd(resizeOffsetLeft.floatValue, resizeOffsetRight.floatValue))
                                resizeOffsetRight.floatValue = 0f
                        },
                    )
                }
            }
        },
    )
}

@Composable
private fun ResizeButtonView(
    modifier: Modifier,
    onResize: (Float) -> Unit,
    onResizeEnd: (Float) -> Unit,
) {
    Canvas(
        modifier = modifier
            .size(
                width = resizeButtonViewArcRadiusDp,
                height = resizeButtonArcHeight,
            )
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    onResize(delta)
                },
                onDragStopped = { delta ->
                    onResizeEnd(delta)
                },
            ),
    ) {
        drawArc(
            color = c.white,
            startAngle = 110f,
            sweepAngle = 140f,
            useCenter = false,
            style = Stroke(
                width = dpToPx(resizeButtonViewArcLineWidthDp.value).toFloat(),
                cap = StrokeCap.Round,
            ),
            size = Size(resizeButtonArcHeight.toPx(), resizeButtonArcHeight.toPx()),
        )
    }
}

private data class XY(
    val x: Float,
    val y: Float,
)
