package app.time_to.timeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MyDialog(
    isPresented: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 20.dp),
    marginValues: PaddingValues = PaddingValues(horizontal = 20.dp),
    backgroundColor: Color = c.background2,
    onDismiss: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    if (!isPresented.value)
        return

    Dialog(
        onDismissRequest = {
            if (onDismiss != null)
                onDismiss()
            isPresented.value = false
        },
        /**
         * Otherwise, for unknown reasons on some phones, such as OPPO, in some
         * cases, the height limit. Faced while creating a calendar dialog.
         */
        properties = DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Box(
                modifier
                    .padding(marginValues)
                    .clip(MySquircleShape(80f))
                    .background(backgroundColor)
                    .padding(paddingValues)
            ) {
                content()
            }
        }
    )
}

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
