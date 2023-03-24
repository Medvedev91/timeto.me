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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import timeto.shared.FullScreenUI
import timeto.shared.onEachExIn
import timeto.shared.vm.FullScreenVM
import timeto.shared.vm.ui.ChecklistUI

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
    val (vm, state) = rememberVM { FullScreenVM() }

    Box {

        Column(
            modifier = Modifier
                .pointerInput(Unit) { }
                .fillMaxSize()
                .background(c.black)
                .padding(top = statusBarHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                text = state.title,
                modifier = Modifier
                    .padding(top = 8.dp, start = 30.dp, end = 30.dp)
                    .offset(y = 6.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = c.white,
                textAlign = TextAlign.Center,
            )

            TextFeaturesTriggersView(
                triggers = state.triggers,
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

                Column(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .weight(1f)
                ) {

                    val checklistVContentPadding = 12.dp
                    val checklistScrollState = rememberLazyListState()
                    val checklistDividerColor = c.white.copy(0.4f)

                    Divider(
                        color = animateColorAsState(
                            if (checklistScrollState.canScrollBackward) checklistDividerColor else c.transparent,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                        ).value
                    )

                    Row(
                        modifier = Modifier
                            .padding(start = 50.dp, end = 50.dp)
                            .weight(1f),
                    ) {

                        val checkboxSize = 18.dp
                        val checklistItemMinHeight = 40.dp
                        val checklistDividerPadding = 14.dp

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            contentPadding = PaddingValues(vertical = checklistVContentPadding),
                            state = checklistScrollState,
                        ) {

                            checklistUI.itemsUI.forEach { itemUI ->

                                item {

                                    Row(
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = checklistItemMinHeight)
                                            .fillMaxWidth()
                                            .clip(MySquircleShape())
                                            .clickable {
                                                itemUI.toggle()
                                            }
                                            .padding(start = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Start,
                                    ) {

                                        Icon(
                                            painterResource(
                                                id = if (itemUI.item.isChecked)
                                                    R.drawable.sf_checkmark_square_fill_medium_regular
                                                else
                                                    R.drawable.sf_square_medium_regular
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
                                                .padding(vertical = 4.dp)
                                                .padding(start = checklistDividerPadding),
                                            textAlign = TextAlign.Start,
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .padding(top = checklistVContentPadding)
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

                                val completionState = checklistUI.completionState
                                val checklistMenuInnerIconPadding = (checklistItemMinHeight - checkboxSize) / 2
                                val checklistMenuStartIconPadding = 4.dp
                                Icon(
                                    painterResource(
                                        id = when (completionState) {
                                            is ChecklistUI.CompletionState.Completed -> R.drawable.sf_checkmark_square_fill_medium_regular
                                            is ChecklistUI.CompletionState.Empty -> R.drawable.sf_square_medium_regular
                                            is ChecklistUI.CompletionState.Partial -> R.drawable.sf_minus_square_fill_medium_medium
                                        }
                                    ),
                                    contentDescription = completionState.actionDesc,
                                    tint = c.white,
                                    modifier = Modifier
                                        .padding(start = checklistMenuStartIconPadding)
                                        .size(checklistItemMinHeight)
                                        .clip(RoundedCornerShape(99.dp))
                                        .clickable {
                                            completionState.onClick()
                                        }
                                        .padding(checklistMenuInnerIconPadding),
                                )
                            }
                        }
                    }

                    Divider(
                        color = animateColorAsState(
                            if (checklistScrollState.canScrollForward) checklistDividerColor else c.transparent,
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                        ).value
                    )
                }
            } else {
                SpacerW1()
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                val menuIconSize = 58.dp
                val menuIconAlpha = 0.5f
                val menuIconPadding = 15.dp

                Icon(
                    painterResource(id = R.drawable.sf_pencil_circle_medimu_thin),
                    contentDescription = "Menu",
                    tint = c.white,
                    modifier = Modifier
                        .weight(1f)
                        .alpha(menuIconAlpha)
                        .clip(MySquircleShape())
                        .size(menuIconSize)
                        .clickable {
                            // todo
                        }
                        .padding(menuIconPadding),
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    val batteryBackground = state.batteryBackground
                    val batteryBackgroundAnimation = animateColorAsState(
                        batteryBackground?.toColor() ?: c.black
                    )

                    Text(
                        text = state.timeOfTheDay,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .alpha(menuIconAlpha),
                        color = c.white,
                        fontSize = 14.sp,
                    )

                    Row(
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .clip(RoundedCornerShape(99.dp))
                            // First alpha for background, then for content
                            .background(
                                batteryBackgroundAnimation.value.copy(
                                    alpha = if (batteryBackground != null) 0.8f else menuIconAlpha
                                )
                            )
                            .alpha(if (batteryBackground != null) 0.9f else menuIconAlpha)
                            //
                            .padding(start = 4.dp, end = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {

                        Icon(
                            painterResource(id = R.drawable.sf_bolt_fill_medium_light),
                            contentDescription = "Battery",
                            tint = c.white,
                            modifier = Modifier
                                .size(10.dp)
                        )

                        Text(
                            text = state.battery,
                            modifier = Modifier,
                            color = c.white,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                        )
                    }
                }

                Icon(
                    painterResource(id = R.drawable.sf_xmark_circle_medium_thin),
                    contentDescription = "Close",
                    tint = c.white,
                    modifier = Modifier
                        .weight(1f)
                        .alpha(menuIconAlpha)
                        .clip(MySquircleShape())
                        .size(menuIconSize)
                        .clickable {
                            layer.close()
                        }
                        .padding(menuIconPadding),
                )
            }
        }
    }
}
