package me.timeto.app.ui.form.plain

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING
import me.timeto.app.R
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.Divider
import me.timeto.app.ui.form.form__itemMinHeight

private val deleteIconSize: Dp = 20.dp
private val deleteIconTapAreaPadding: Dp = 4.dp
private val deleteIconHPadding: Dp = H_PADDING - deleteIconTapAreaPadding
private val deleteDividerPadding: Dp =
    deleteIconSize +
    deleteIconTapAreaPadding * 2 +
    deleteIconHPadding * 2 +
    1.dp

@Composable
fun FormPlainButtonDeletion(
    title: String,
    isFirst: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {

    ZStack(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
    ) {

        HStack(
            modifier = Modifier
                .defaultMinSize(minHeight = form__itemMinHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            ZStack(
                modifier = Modifier
                    .padding(start = deleteIconHPadding, end = deleteIconHPadding)
                    .clip(roundedShape)
                    .clickable {
                        onDelete()
                    }
                    .padding(deleteIconTapAreaPadding),
                contentAlignment = Alignment.Center,
            ) {

                ZStack(
                    modifier = Modifier
                        .size(deleteIconSize - 2.dp)
                        .clip(roundedShape)
                        .background(c.white),
                )

                Icon(
                    painter = painterResource(id = R.drawable.sf_minus_circle_fill_medium_regular),
                    contentDescription = "Delete",
                    tint = c.red,
                    modifier = Modifier
                        .size(deleteIconSize),
                )
            }

            Text(
                text = title,
                modifier = Modifier
                    .padding(vertical = 4.dp),
                color = c.text,
            )
        }

        if (!isFirst) {
            Divider(
                modifier = Modifier
                    .padding(start = deleteDividerPadding)
                    .align(Alignment.TopEnd)
            )
        }
    }
}
