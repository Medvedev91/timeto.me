package me.timeto.app.ui.form.button

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.HStack

val FormButtonEmoji__fontSize: TextUnit = 20.sp

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
                    fontSize = FormButtonEmoji__fontSize,
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
