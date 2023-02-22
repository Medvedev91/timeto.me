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
import androidx.compose.ui.graphics.Shape

private val LocalLayers = compositionLocalOf<MutableList<UIWrapper.LayerData>> { mutableListOf() }

object UIWrapper {

    data class LayerData(
        val isPresented: Boolean,
        val onClose: () -> Unit,
        val shape: Shape,
        val enterAnimation: EnterTransition,
        val exitAnimation: ExitTransition,
        val content: @Composable () -> Unit,
    )

    ///
    /// Composable

    @Composable
    fun Layout(
        content: @Composable () -> Unit
    ) {

        val items = remember { mutableStateListOf<LayerData>() }

        CompositionLocalProvider(
            LocalLayers provides items,
        ) {

            Box {

                content()

                items.forEach { layer ->

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomCenter,
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
    }

    @Composable
    fun LayerView(data: LayerData) {
        val layers = LocalLayers.current
        DisposableEffect(data) {
            layers.add(data)
            onDispose {
                layers.remove(data)
            }
        }
    }
}
