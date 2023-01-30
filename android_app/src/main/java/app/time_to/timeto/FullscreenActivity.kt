package app.time_to.timeto

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.time_to.timeto.ui.*
import timeto.shared.ColorNative
import timeto.shared.vm.FullscreenVM

class FullscreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://stackoverflow.com/a/14926037/5169420
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {

            MyLocalProvider {

                val (vm, state) = rememberVM { FullscreenVM(ColorNative.white) }
                val timerData = state.timerData

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(c.black)
                ) {

                    // todo copy-paste
                    val localTriggersDialogManager = LocalTriggersDialogManager.current
                    localTriggersDialogManager.checklist.value?.let { checklist ->
                        ChecklistDialog(checklist, localTriggersDialogManager.checklistIsPresented)
                    }
                    ListenNewIntervalForTriggers()
                    ///

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
                                finishWithAnimation()
                            },
                        tint = c.white.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }

    private fun finishWithAnimation() {
        finish()
        overridePendingTransition(R.anim.fullscreen_in, R.anim.fullscreen_out)
    }
}
