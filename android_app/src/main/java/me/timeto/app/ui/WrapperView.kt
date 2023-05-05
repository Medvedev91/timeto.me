package me.timeto.app.ui

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
import me.timeto.app.setFalse
import me.timeto.app.setTrue
import kotlinx.coroutines.delay
import timeto.shared.launchExDefault

object WrapperView {

    private val wrapperViewLayers = mutableStateListOf<Layer>()

    class Layer(
        val enterAnimation: EnterTransition,
        val exitAnimation: ExitTransition,
        val alignment: Alignment,
        val onClose: () -> Unit,
        val content: @Composable (Layer) -> Unit,
    ) {

        val isPresented = mutableStateOf(false)

        fun show() {
            wrapperViewLayers.add(this)
        }

        fun close() {
            onClose()
            isPresented.setFalse()
            launchExDefault {
                delay(500) // Waiting for animation
                wrapperViewLayers.remove(this@Layer)
            }
        }
    }

    @Composable
    fun LayoutView(
        content: @Composable () -> Unit
    ) {

        Box {

            content()

            wrapperViewLayers.forEach { layer ->

                key(layer) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = layer.alignment,
                    ) {

                        AnimatedVisibility(
                            layer.isPresented.value,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0x55000000))
                                    .clickable { layer.close() }
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
                            layer.content(layer)
                        }
                    }

                    LaunchedEffect(Unit) {
                        layer.isPresented.setTrue()
                    }
                }
            }
        }
    }
}
