package me.timeto.app.ui.form.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.timeto.app.ui.HStack

@Composable
fun FormButton(
    title: String,
    titleColor: Color? = null,
    isFirst: Boolean,
    isLast: Boolean,
    note: String? = null,
    noteColor: Color? = null,
    withArrow: Boolean = false,
    arrowColor: Color? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {

    FormButtonView(
        title = title,
        titleColor = titleColor,
        isFirst = isFirst,
        isLast = isLast,
        modifier = modifier,
        rightView = {

            HStack(
                verticalAlignment = Alignment.CenterVertically,
            ) {

                if (note != null) {
                    FormButtonNoteView(
                        note = note,
                        color = noteColor,
                        withArrow = withArrow,
                    )
                }

                if (withArrow) {
                    FormButtonArrowView(color = arrowColor)
                }
            }
        },
        onClick = onClick,
        onLongClick = onLongClick,
    )
}
