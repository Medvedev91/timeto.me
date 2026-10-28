package me.timeto.app.ui.doc

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.c

@Composable
fun DocTextView(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = H_PADDING)
            .padding(vertical = 8.dp),
        color = c.text,
        lineHeight = 23.sp,
        fontWeight = FontWeight.Normal,
    )
}
