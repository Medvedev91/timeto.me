package app.time_to.timeto.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import app.time_to.timeto.setFalse

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
            isPresented = isPresented,
            onClose = { isPresented.setFalse() },
            backgroundColor = backgroundColor,
            modifier = modifier,
            paddingValues = paddingValues,
            marginValues = marginValues,
            content = content
        )
    )
}

private fun prepMyDialogLayer(
    isPresented: MutableState<Boolean>,
    onClose: (WrapperView__LayerData) -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 20.dp),
    marginValues: PaddingValues = PaddingValues(horizontal = 20.dp),
    content: @Composable (WrapperView__LayerData) -> Unit
) = WrapperView__LayerData(
    isPresented = isPresented,
    onClose = { onClose(it) },
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

@Composable
fun MyDialog__Confirmation(
    isPresented: MutableState<Boolean>,
    text: @Composable () -> Unit,
    buttonText: String,
    buttonColor: Color,
    onConfirm: () -> Unit
) {
    MyDialog(isPresented = isPresented) {

        Column {

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(all = 5.dp)
            ) {
                text()
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Text(
                    "Cancel",
                    color = c.textSecondary,
                    modifier = Modifier
                        .padding(end = 11.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isPresented.value = false }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                MyButton(buttonText, true, buttonColor) {
                    isPresented.value = false
                    onConfirm()
                }
            }
        }
    }
}
