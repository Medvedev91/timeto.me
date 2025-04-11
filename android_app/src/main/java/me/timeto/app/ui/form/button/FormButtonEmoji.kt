package me.timeto.app.ui.form.button

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack

@Composable
fun FormButtonEmoji(
    title: String,
    emoji: String,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    FormButtonView(
        title = title,
        titleColor = null,
        isFirst = isFirst,
        isLast = isLast,
        modifier = Modifier,
        rightView = {
            HStack(
                verticalAlignment = CenterVertically,
            ) {
                Text(
                    text = emoji,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(end = 7.dp)
                )
                FormButtonArrowView()
            }
        },
        onClick = {
            onClick()
        },
        onLongClick = null,
    )
}
