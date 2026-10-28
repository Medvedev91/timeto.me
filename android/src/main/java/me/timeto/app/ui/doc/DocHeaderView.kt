package me.timeto.app.ui.doc

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.c
import me.timeto.app.ui.header.Header__titleFontSize
import me.timeto.app.ui.header.Header__titleFontWeight

@Composable
fun DocHeaderView(
    text: String,
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = H_PADDING)
            .padding(top = 48.dp, bottom = 8.dp),
        color = c.text,
        fontWeight = Header__titleFontWeight,
        fontSize = Header__titleFontSize,
    )
}
