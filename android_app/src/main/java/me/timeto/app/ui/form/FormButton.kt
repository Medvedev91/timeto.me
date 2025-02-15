package me.timeto.app.ui.form

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.timeto.app.HStack
import me.timeto.app.c
import me.timeto.app.ui.form.views.FormButtonArrowView
import me.timeto.app.ui.form.views.FormButtonView

@Composable
fun FormButton(
    title: String,
    titleColor: Color? = null,
    isFirst: Boolean,
    isLast: Boolean,
    note: String? = null,
    noteColor: Color? = null,
    withArrow: Boolean = false,
    onClick: () -> Unit,
) {

    FormButtonView(
        title = title,
        titleColor = titleColor ?: c.text,
        isFirst = isFirst,
        isLast = isLast,
        modifier = Modifier,
        rightView = {

            HStack(
                verticalAlignment = Alignment.CenterVertically,
            ) {

                if (note != null) {
                    Text(
                        note,
                        modifier = Modifier
                            .padding(end = if (withArrow) 8.dp else 16.dp)
                            .offset(),
                        color = noteColor ?: c.tertiaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (withArrow) {
                    FormButtonArrowView()
                }
            }
        },
        onClick = onClick,
    )
}
