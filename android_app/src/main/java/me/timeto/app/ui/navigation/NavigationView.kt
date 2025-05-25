package me.timeto.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import me.timeto.app.ui.ZStack

@Composable
fun NavigationView(
    navigation: Navigation,
    content: @Composable () -> Unit,
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    ZStack {

        content()

        navigation.layers.forEach { layer ->

            key(layer) {

                ZStack(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {

                    AnimatedVisibility(
                        layer.isPresented.value,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        ZStack(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(shadeColor)
                                .clickable {
                                    layer.close()
                                },
                        )
                    }

                    AnimatedVisibility(
                        layer.isPresented.value,
                        enter = layer.enterAnimation,
                        exit = layer.exitAnimation,
                    ) {
                        BackHandler(layer.isPresented.value) {
                            layer.close()
                        }
                        CompositionLocalProvider(
                            LocalNavigationLayer provides layer,
                        ) {
                            layer.content(layer)
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    keyboardController?.hide()
                    layer.isPresented.value = true
                }
            }
        }
    }
}

///

private val shadeColor = Color(0x99000000)
