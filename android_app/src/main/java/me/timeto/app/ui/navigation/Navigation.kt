package me.timeto.app.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import me.timeto.app.ui.H_PADDING
import me.timeto.app.c
import me.timeto.app.ui.navigation.picker.NavigationPicker
import me.timeto.app.ui.navigation.picker.NavigationPickerItem
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

    fun dialog(
        innerPadding: PaddingValues = PaddingValues(H_PADDING),
        content: @Composable ColumnScope.(layer: NavigationLayer) -> Unit,
    ) {
        push { layer ->
            NavigationDialog(
                layer = layer,
                innerPadding = innerPadding,
                content = content,
            )
        }
    }

    fun <T> picker(
        items: List<NavigationPickerItem<T>>,
        onDone: (item: NavigationPickerItem<T>) -> Unit,
    ) {
        dialog(
            innerPadding = PaddingValues(),
        ) {
            NavigationPicker(
                items = items,
                onDone = onDone,
            )
        }
    }

    // DialogsManger

    override fun alert(
        message: String,
    ) {
        dialog { layer ->
            NavigationAlert(
                message = message,
                withCancelButton = false,
                buttonText = "Ok",
                buttonColor = c.blue,
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
        dialog { layer ->
            NavigationAlert(
                message = message,
                withCancelButton = true,
                buttonText = buttonText,
                buttonColor = c.red,
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
