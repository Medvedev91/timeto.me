package app.time_to.timeto.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import timeto.shared.vm.FullscreenVM

@Composable
fun FullScreenView(
    isPresented: MutableState<Boolean>
) {
    val activity = LocalMainActivity.current
    val isPresentedValue = isPresented.value
    // https://developer.android.com/develop/ui/views/layout/immersive#kotlin
    LaunchedEffect(isPresentedValue) {
        val window = activity.window
        val barTypes = WindowInsetsCompat.Type.systemBars()
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        val flagKeepScreenOn = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        if (isPresentedValue) {
            controller.hide(barTypes)
            window.addFlags(flagKeepScreenOn)
        } else {
            controller.show(barTypes)
            window.clearFlags(flagKeepScreenOn)
        }
    }

    UIWrapper.LayerView(
        UIWrapper.LayerData(
            isPresented = isPresented,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            content = {
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) { }
                        .fillMaxSize()
                        .background(c.black)
                        // todo test if the bars are showed
                        .padding(top = 14.dp, bottom = 20.dp)
                ) {

                    val (vm, state) = rememberVM { FullscreenVM(ColorNative.white) }
                    val timerData = state.timerData

                    Column(
                        modifier = Modifier.align(Alignment.TopCenter)
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

                        TriggersView__ListView(
                            triggers = state.triggers,
                            withOnClick = true,
                            modifier = Modifier.padding(top = 10.dp),
                            contentPadding = PaddingValues(horizontal = 50.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.align(Alignment.Center),
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

                        Text(
                            text = timerData.timer,
                            fontSize = 69.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = timerData.color.toColor(),
                            modifier = Modifier.padding(top = 15.dp, bottom = 30.dp),
                        )

                        Text(
                            text = "Restart",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier
                                .alpha(if (timerData.title != null) 1f else 0f)
                                .clickable(timerData.title != null) {
                                    vm.restart()
                                },
                            color = c.white,
                            letterSpacing = 2.sp,
                        )
                    }

                    Icon(
                        painterResource(id = R.drawable.sf_xmark_large_light),
                        contentDescription = "Close",
                        modifier = Modifier
                            .padding(bottom = 34.dp)
                            .size(20.dp, 20.dp)
                            .align(Alignment.BottomCenter)
                            .clickable {
                                isPresented.setFalse()
                            },
                        tint = c.white.copy(alpha = 0.9f)
                    )
                }
            }
        )
    )
}
