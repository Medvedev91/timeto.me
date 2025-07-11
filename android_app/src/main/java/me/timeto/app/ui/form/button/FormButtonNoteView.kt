package me.timeto.app.ui.form.button

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.c

@Composable
fun FormButtonNoteView(
    note: String,
    color: Color?,
    withArrow: Boolean,
) {
    Text(
        text = note,
        modifier = Modifier
            .padding(end = if (withArrow) 8.dp else 16.dp)
            .offset(),
        color = color ?: c.secondaryText,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
