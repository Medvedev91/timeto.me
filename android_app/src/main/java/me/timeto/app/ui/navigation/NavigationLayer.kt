package me.timeto.app.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay
import me.timeto.app.setFalse
import me.timeto.shared.launchExIo

val LocalNavigationLayer = compositionLocalOf<NavigationLayer> {
    throw Exception("LocalNavigationLayer")
}

class NavigationLayer(
    val enterAnimation: EnterTransition,
    val exitAnimation: ExitTransition,
    val onClose: (layer: NavigationLayer) -> Unit,
    val content: @Composable (layer: NavigationLayer) -> Unit,
) {

    val isPresented = mutableStateOf(false)

    fun close() {
        isPresented.setFalse()
        launchExIo {
            delay(500) // Waiting for animation
            onClose(this@NavigationLayer)
        }
    }
}
