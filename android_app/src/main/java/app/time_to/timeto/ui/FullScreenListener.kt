package app.time_to.timeto.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.time_to.timeto.*
import app.time_to.timeto.R
import timeto.shared.ColorNative
import timeto.shared.FullScreenUI
import timeto.shared.onEachExIn
import timeto.shared.vm.FullscreenVM

@Composable
fun FullScreenListener(
    activity: Activity,
    onClose: () -> Unit,
) {
    LaunchedEffect(Unit) {

        FullScreenUI.state.onEachExIn(this) { toOpenOrClose ->

            /**
             * https://developer.android.com/develop/ui/views/layout/immersive#kotlin
             *
             * No systemBars(), because on Redmi the first touch opens navbar.
             *
             * Needs "android:windowLayoutInDisplayCutoutMode shortEdges" in manifest
             * to hide dark space on the top while WindowInsetsCompat.Type.statusBars()
             * like https://stackoverflow.com/q/72179274 in "2. Completely black...".
             * https://developer.android.com/develop/ui/views/layout/display-cutout
             */
            val barTypes = WindowInsetsCompat.Type.statusBars()
            val window = activity.window
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            val flagKeepScreenOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

            ///
            /// Open / Close

            if (!toOpenOrClose) {
                controller.show(barTypes)
                window.clearFlags(flagKeepScreenOn)
                onClose()
                return@onEachExIn
            }

            controller.hide(barTypes)
            window.addFlags(flagKeepScreenOn)
            window.navigationBarColor = Color(0x01000000).toArgb()
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false

            //////

            WrapperView.Layer(
                enterAnimation = fadeIn(spring(stiffness = Spring.StiffnessHigh)),
                exitAnimation = fadeOut(spring(stiffness = Spring.StiffnessHigh)),
                alignment = Alignment.Center,
                onClose = { FullScreenUI.close() },
                content = { layer ->
                    FullScreenView(layer)
                }
            ).show()
        }
    }
}

@Composable
private fun FullScreenView(
    layer: WrapperView.Layer,
) {
    Box(
        modifier = Modifier
            .pointerInput(Unit) { }
            .fillMaxSize()
            .background(c.black)
            .navigationBarsPadding()
            .padding(top = 20.dp)
    ) {

        val (vm, state) = rememberVM { FullscreenVM(ColorNative.white) }

        val checklistUI = state.checklistUI
        if (checklistUI != null) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
            ) {

                FullScreenView__HeaderView(
                    state = state,
                    modifier = Modifier,
                )

                FullScreenView__TimerView(
                    vm = vm,
                    state = state,
                    layer = layer,
                    isCompact = true,
                    modifier = Modifier.padding(top = 20.dp),
                )

                LazyColumn(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 20.dp),
                ) {
                    checklistUI.itemsUI.forEach { itemUI ->
                        item {
                            Text(
                                text = itemUI.item.text + if (itemUI.item.isChecked()) "  ✅" else "",
                                color = c.white,
                                modifier = Modifier
                                    .clickable {
                                        itemUI.toggle()
                                    }
                                    .padding(horizontal = 20.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        } else {

            FullScreenView__HeaderView(
                state = state,
                modifier = Modifier.align(Alignment.TopCenter),
            )

            FullScreenView__TimerView(
                vm = vm,
                state = state,
                layer = layer,
                isCompact = false,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 50.dp),
            )
        }
    }
}

@Composable
private fun FullScreenView__HeaderView(
    state: FullscreenVM.State,
    modifier: Modifier,
) {
    Column(
        modifier = modifier
    ) {

        Text(
            text = state.title,
            modifier = Modifier
                .padding(top = 30.dp, start = 30.dp, end = 30.dp),
            fontSize = 22.sp,
            fontWeight = FontWeight.Light,
            color = c.white,
            textAlign = TextAlign.Center
        )

        TextFeaturesTriggersView(
            textFeatures = state.textFeatures,
            modifier = Modifier.padding(top = 10.dp),
            contentPadding = PaddingValues(horizontal = 50.dp)
        )
    }
}

@Composable
private fun FullScreenView__TimerView(
    vm: FullscreenVM,
    state: FullscreenVM.State,
    layer: WrapperView.Layer,
    isCompact: Boolean,
    modifier: Modifier,
) {
    val timerData = state.timerData
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = timerData.title ?: "0",
            fontSize = 30.sp,
            modifier = Modifier
                .alpha(if (timerData.title != null) 1f else 0f),
            fontWeight = FontWeight.ExtraBold,
            color = timerData.color.toColor(),
            letterSpacing = 5.sp
        )

        val paddings: PaddingValues = if (isCompact)
            PaddingValues(top = 5.dp, bottom = 15.dp)
        else PaddingValues(top = 10.dp, bottom = 20.dp)

        Text(
            text = timerData.timer,
            fontSize = 69.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            color = timerData.color.toColor(),
            modifier = Modifier.padding(paddings),
        )

        Row {

            Icon(
                painterResource(id = R.drawable.sf_arrow_counterclockwise_medium_regular),
                contentDescription = "Restart",
                tint = c.white,
                modifier = Modifier
                    .padding(end = 40.dp)
                    .size(30.dp)
                    .clickable {
                        vm.restart()
                    },
            )

            Icon(
                painterResource(id = R.drawable.sf_xmark_large_light),
                contentDescription = "Close",
                tint = c.white,
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        layer.close()
                    },
            )
        }
    }
}
