package me.timeto.app.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf

class Navigation {

    val layers = mutableStateListOf<NavigationLayer>()

    fun push(
        content: @Composable (layer: NavigationLayer) -> Unit,
    ) {
        val layer = NavigationLayer(
            enterAnimation = enterAnimation,
            exitAnimation = exitAnimation,
            onClose = { layer ->
                layers.remove(layer)
            },
            content = content,
        )
        layers.add(layer)
    }
}

///

private val enterAnimation = fadeIn(spring(stiffness = Spring.StiffnessMedium))
private val exitAnimation = fadeOut(spring(stiffness = Spring.StiffnessMedium))
