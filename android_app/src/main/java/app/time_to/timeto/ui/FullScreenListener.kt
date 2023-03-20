package app.time_to.timeto.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
    Column(
        modifier = Modifier
            .pointerInput(Unit) { }
            .fillMaxSize()
            .background(c.black)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val (vm, state) = rememberVM { FullscreenVM(ColorNative.white) }

        Text(
            text = state.title,
            modifier = Modifier
                .padding(top = 44.dp, start = 30.dp, end = 30.dp),
            fontSize = 19.sp,
            fontWeight = FontWeight.Normal,
            color = c.white,
            textAlign = TextAlign.Center
        )

        TextFeaturesTriggersView(
            textFeatures = state.textFeatures,
            modifier = Modifier.padding(top = 10.dp),
            contentPadding = PaddingValues(horizontal = 50.dp)
        )

        val timerData = state.timerData
        AnimatedVisibility(
            timerData.title != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {

            Text(
                text = timerData.title ?: "",
                fontSize = 21.sp,
                modifier = Modifier
                    .padding(top = 36.dp)
                    .offset(y = 3.dp),
                fontWeight = FontWeight.ExtraBold,
                color = timerData.color.toColor(),
                letterSpacing = 3.sp,
            )
        }

        Text(
            text = timerData.timer,
            fontSize = if (timerData.isCompact) 60.sp else 70.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = timerData.color.toColor(),
        )

        AnimatedVisibility(
            timerData.title != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Text(
                text = "Restart",
                modifier = Modifier
                    .clickable {
                        vm.restart()
                    },
                fontSize = 19.sp,
                fontWeight = FontWeight.Normal,
                color = c.white,
                textAlign = TextAlign.Center
            )
        }

        val checklistUI = state.checklistUI
        if (checklistUI != null) {

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 20.dp, start = 80.dp, end = 80.dp),
            ) {

                val checkboxSize = 18.dp
                val checklistItemMinHeight = 38.dp
                val checklistDividerPadding = 16.dp

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(bottom = 20.dp),
                ) {

                    checklistUI.itemsUI.forEach { itemUI ->

                        item {

                            Row(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = checklistItemMinHeight)
                                    .fillMaxWidth()
                                    .clickable {
                                        itemUI.toggle()
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                            ) {

                                Icon(
                                    painterResource(
                                        id = if (itemUI.item.isChecked)
                                            R.drawable.sf_square_medium_regular
                                        else
                                            R.drawable.sf_checkmark_square_fill_medium_regular
                                    ),
                                    contentDescription = "Checkbox",
                                    tint = c.white,
                                    modifier = Modifier
                                        .size(checkboxSize),
                                )

                                Text(
                                    text = itemUI.item.text,
                                    color = c.white,
                                    modifier = Modifier
                                        .padding(horizontal = checklistDividerPadding, vertical = 4.dp),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Max)
                ) {

                    Box(
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .alpha(.5f)
                            .background(c.white)
                            .width(1.dp)
                            .fillMaxHeight(),
                    )

                    Column {

                        Icon(
                            painterResource(id = R.drawable.sf_square_medium_regular),
                            contentDescription = "Uncheck All",
                            tint = c.white,
                            modifier = Modifier
                                .defaultMinSize(minHeight = checklistItemMinHeight)
                                .clickable {
                                }
                                .padding(start = checklistDividerPadding)
                                .size(checkboxSize),
                        )

                        Icon(
                            painterResource(id = R.drawable.sf_checkmark_square_fill_medium_regular),
                            contentDescription = "Check All",
                            tint = c.white,
                            modifier = Modifier
                                .defaultMinSize(minHeight = checklistItemMinHeight)
                                .clickable {
                                }
                                .padding(start = checklistDividerPadding)
                                .size(checkboxSize),
                        )
                    }
                }
            }
        } else {
            SpacerW1()
        }

        Row(Modifier.padding(horizontal = 55.dp)) {

            Icon(
                painterResource(id = R.drawable.sf_gearshape_medium_thin),
                contentDescription = "Settings",
                tint = c.white,
                modifier = Modifier
                    .alpha(0.5f)
                    .padding(bottom = 24.dp)
                    .size(32.dp)
                    .padding(2.dp)
                    .clickable {
                        // todo
                    },
            )

            SpacerW1()

            Icon(
                painterResource(id = R.drawable.sf_xmark_circle_medium_thin),
                contentDescription = "Close",
                tint = c.white,
                modifier = Modifier
                    .alpha(0.5f)
                    .padding(bottom = 24.dp)
                    .size(33.dp)
                    .padding(2.dp)
                    .clickable {
                        layer.close()
                    },
            )
        }
    }
}
