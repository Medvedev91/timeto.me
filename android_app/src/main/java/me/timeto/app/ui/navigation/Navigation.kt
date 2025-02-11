package me.timeto.app.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import me.timeto.shared.ui.DialogsManager

class Navigation : DialogsManager {

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

    // DialogsManger

    override fun alert(
        message: String,
    ) {
        push { layer ->
            NavigationAlert(
                message = message,
                withCancelButton = false,
                buttonText = "Ok",
                onButtonClick = {
                    layer.close()
                },
            )
        }
    }

    override fun confirmation(
        message: String,
        buttonText: String,
        onConfirm: () -> Unit,
    ) {
        push { layer ->
            NavigationAlert(
                message = message,
                withCancelButton = true,
                buttonText = buttonText,
                onButtonClick = {
                    onConfirm()
                    layer.close()
                },
            )
        }
    }
}

///

private val enterAnimation = fadeIn(spring(stiffness = Spring.StiffnessMedium))
private val exitAnimation = fadeOut(spring(stiffness = Spring.StiffnessMedium))
