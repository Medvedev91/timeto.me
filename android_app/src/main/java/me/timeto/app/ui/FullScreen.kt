package me.timeto.app.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

private val enterAnimation = fadeIn(spring(stiffness = Spring.StiffnessMedium))
private val exitAnimation = fadeOut(spring(stiffness = Spring.StiffnessMedium))

object FullScreen {

    fun show(
        content: @Composable (WrapperView.Layer) -> Unit,
    ) {

        WrapperView.Layer(
            enterAnimation = enterAnimation,
            exitAnimation = exitAnimation,
            alignment = Alignment.BottomCenter,
            onClose = {},
            content = { layer ->
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {}
                ) {
                    content(layer)
                }
            }
        ).show()
    }
}
