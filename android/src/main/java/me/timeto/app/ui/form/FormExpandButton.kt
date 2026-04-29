package me.timeto.app.ui.form

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.roundedShape

@Composable
fun FormExpandButton(
    text: String,
    onClick: () -> Unit,
) {
    ZStack(
        modifier = Modifier
            .height(form__itemMinHeight),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .padding(start = H_PADDING)
                .clip(roundedShape)
                .clickable {
                    onClick()
                }
                .padding(horizontal = H_PADDING, vertical = 4.dp),
            color = c.blue,
        )
    }
}
