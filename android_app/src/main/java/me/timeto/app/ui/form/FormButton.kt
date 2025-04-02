package me.timeto.app.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.timeto.app.HStack
import me.timeto.app.ui.form.views.FormButtonArrowView
import me.timeto.app.ui.form.views.FormButtonNoteView
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
    onLongClick: (() -> Unit)? = null,
) {

    FormButtonView(
        title = title,
        titleColor = titleColor,
        isFirst = isFirst,
        isLast = isLast,
        modifier = Modifier,
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
                    FormButtonArrowView()
                }
            }
        },
        onClick = onClick,
        onLongClick = onLongClick,
    )
}
