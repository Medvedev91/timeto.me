package me.timeto.app.ui.doc

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.c

@Composable
fun DocForceTextView(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .padding(horizontal = H_PADDING, vertical = 8.dp),
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = c.green,
    )
}
