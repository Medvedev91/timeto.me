package app.time_to.timeto.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import app.time_to.timeto.LocalWrapperViewLayers

object MyDialog {

    fun show(
        layers: MutableList<WrapperView__LayerData>,
        modifier: Modifier = Modifier,
        margin: PaddingValues = PaddingValues(horizontal = 20.dp),
        content: @Composable (WrapperView__LayerData) -> Unit
    ) {
        val isPresented = mutableStateOf(false)
        WrapperView__LayerData(
            layers = layers,
            isPresented = isPresented,
            enterAnimation = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
            exitAnimation = fadeOut(spring(stiffness = Spring.StiffnessMedium)),
            alignment = Alignment.Center,
            content = { layer ->
                Box(
                    modifier
                        .systemBarsPadding()
                        .imePadding()
                        .padding(margin)
                        .clip(MySquircleShape(80f))
                        .pointerInput(Unit) { }
                ) {
                    content(layer)
                }
            }
        ).showOneTime(layers)
    }
}

@Composable
fun MyDialog(
    isPresented: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 20.dp),
    marginValues: PaddingValues = PaddingValues(horizontal = 20.dp),
    backgroundColor: Color = c.background2,
    content: @Composable (WrapperView__LayerData) -> Unit
) {
    WrapperView__LayerView(
        prepMyDialogLayer(
            layers = LocalWrapperViewLayers.current,
            isPresented = isPresented,
            backgroundColor = backgroundColor,
            modifier = modifier,
            paddingValues = paddingValues,
            marginValues = marginValues,
            content = content
        )
    )
}

fun prepMyDialogLayer(
    layers: MutableList<WrapperView__LayerData>,
    isPresented: MutableState<Boolean>,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 20.dp),
    marginValues: PaddingValues = PaddingValues(horizontal = 20.dp),
    content: @Composable (WrapperView__LayerData) -> Unit
) = WrapperView__LayerData(
    layers = layers,
    isPresented = isPresented,
    enterAnimation = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
    exitAnimation = fadeOut(spring(stiffness = Spring.StiffnessMedium)),
    alignment = Alignment.Center,
    content = { layer ->
        Box(
            modifier
                .systemBarsPadding()
                .imePadding()
                .padding(marginValues)
                .clip(MySquircleShape(80f))
                .background(backgroundColor)
                .pointerInput(Unit) { }
                .padding(paddingValues)
        ) {
            content(layer)
        }
    }
)
