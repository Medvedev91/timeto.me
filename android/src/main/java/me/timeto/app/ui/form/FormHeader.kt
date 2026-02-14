package me.timeto.app.ui.form

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.c

@Composable
fun FormHeader(
    title: String,
) {
    Text(
        text = title,
        modifier = Modifier
            .padding(horizontal = H_PADDING + H_PADDING)
            .alpha(0.8f),
        color = c.secondaryText,
        fontSize = 12.sp,
    )
}
