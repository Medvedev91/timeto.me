package me.timeto.app.ui.header.views

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.H_PADDING
import me.timeto.app.c
import me.timeto.app.ui.header.Header__titleFontSize
import me.timeto.app.ui.header.Header__titleFontWeight

@Composable
fun RowScope.HeaderTitleView(
    title: String,
) {
    Text(
        text = title,
        modifier = Modifier
            .padding(start = H_PADDING)
            .weight(1f),
        fontSize = Header__titleFontSize,
        fontWeight = Header__titleFontWeight,
        color = c.text,
    )
}
