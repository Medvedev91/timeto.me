package me.timeto.app.ui.form.button

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.H_PADDING
import me.timeto.app.R
import me.timeto.app.ui.c
import me.timeto.app.ui.halfDpFloor

@Composable
fun FormButtonArrowView(
    color: Color? = null,
) {
    Icon(
        painter = painterResource(id = R.drawable.sf_chevron_right_medium_medium),
        contentDescription = "Expand",
        tint = color ?: c.tertiaryText,
        modifier = Modifier
            .offset(y = -halfDpFloor)
            .padding(end = H_PADDING - 2.dp - halfDpFloor)
            .size(12.dp),
    )
}
