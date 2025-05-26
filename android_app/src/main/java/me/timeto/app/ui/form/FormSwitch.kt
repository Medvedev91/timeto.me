package me.timeto.app.ui.form

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.timeto.app.c
import me.timeto.app.ui.halfDpFloor
import me.timeto.app.ui.form.button.FormButtonView

@Composable
fun FormSwitch(
    title: String,
    isEnabled: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    onChange: (Boolean) -> Unit,
) {
    FormButtonView(
        title = title,
        titleColor = c.text,
        isFirst = isFirst,
        isLast = isLast,
        modifier = modifier,
        rightView = {
            Switch(
                checked = isEnabled,
                onCheckedChange = { newValue ->
                    onChange(newValue)
                },
                modifier = Modifier
                    .padding(end = 9.dp, bottom = halfDpFloor),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = c.blue,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.Gray,
                ),
            )
        },
        onClick = {
            onChange(!isEnabled)
        },
        onLongClick = null,
    )
}
