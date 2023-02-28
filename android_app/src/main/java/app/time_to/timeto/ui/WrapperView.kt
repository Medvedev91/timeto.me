package app.time_to.timeto.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.time_to.timeto.LocalWrapperViewLayers

data class WrapperView__LayerData(
    val isPresented: Boolean,
    val onClose: () -> Unit,
    val enterAnimation: EnterTransition,
    val exitAnimation: ExitTransition,
    val alignment: Alignment,
    val content: @Composable () -> Unit,
)

@Composable
fun WrapperView__LayerView(
    layer: WrapperView__LayerData
) {
    val layers = LocalWrapperViewLayers.current
    DisposableEffect(layer) {
        layers.add(layer)
        onDispose {
            layers.remove(layer)
        }
    }
}

@Composable
fun WrapperView(
    content: @Composable () -> Unit
) {

    val layers = LocalWrapperViewLayers.current

    Box {

        content()

        layers.forEach { layer ->

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = layer.alignment,
            ) {

                AnimatedVisibility(
                    layer.isPresented,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x55000000))
                            .clickable { layer.onClose() }
                    )
                }

                AnimatedVisibility(
                    layer.isPresented,
                    enter = layer.enterAnimation,
                    exit = layer.exitAnimation,
                ) {
                    BackHandler(layer.isPresented) {
                        layer.onClose()
                    }
                    layer.content()
                }
            }
        }
    }
}
