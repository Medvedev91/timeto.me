package me.timeto.app.ui.form.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.timeto.app.HStack
import me.timeto.app.H_PADDING
import me.timeto.app.ui.form.Form__itemMinHeight

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FormButtonView(
    title: String,
    titleColor: Color,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier,
    rightView: @Composable () -> Unit,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
) {

    FormItemView(
        isFirst = isFirst,
        isLast = isLast,
        modifier = modifier,
        content = {

            HStack(
                modifier = Modifier
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick,
                    )
                    .sizeIn(minHeight = Form__itemMinHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Text(
                    title,
                    modifier = Modifier
                        .padding(start = H_PADDING, end = 10.dp)
                        .weight(1f),
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                rightView()
            }
        },
    )
}
