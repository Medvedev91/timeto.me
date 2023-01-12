package app.time_to.timeto.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object MyList {

    val HEADER_BOTTOM_PADDING = 10.dp
    private val PADDING_H = 20.dp

    val SECTION_ITEM_BUTTON_H_PADDING = 18.dp
    val SECTION_ITEM_BUTTON_V_PADDING = 12.dp

    @Composable
    fun Header(
        title: String,
        rightView: (@Composable () -> Unit)? = null
    ) {
        Row(
            Modifier.padding(top = 30.dp, start = PADDING_H * 2 - 1.dp, end = PADDING_H * 2)
        ) {
            Text(
                title,
                modifier = Modifier
                    .alpha(0.8f)
                    .weight(1f),
                color = c.textSecondary,
                fontSize = 12.sp
            )
            rightView?.invoke()
        }
    }

    @Composable
    fun Header__RightIcon(
        @DrawableRes iconId: Int,
        contentDescription: String,
        onClick: () -> Unit
    ) {
        Icon(
            painterResource(id = iconId),
            contentDescription,
            tint = c.blue,
            modifier = Modifier
                .padding(start = 10.dp)
                .size(26.dp)
                .offset(y = (-4).dp, x = 4.dp)
                .alpha(0.8f)
                .clip(RoundedCornerShape(99.dp))
                .clickable {
                    onClick()
                }
                .padding(2.5.dp)
        )
    }

    @Composable
    fun SectionItem(
        isFirst: Boolean,
        isLast: Boolean,
        paddingTop: Dp = 0.dp,
        content: @Composable () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = paddingTop)
                .padding(horizontal = PADDING_H)
                .clip(MySquircleShape(angles = listOf(isFirst, isFirst, isLast, isLast)))
                .background(c.background2)
        ) {
            content()
        }
    }

    @Composable
    fun SectionItem_Button(
        text: String,
        withDivider: Boolean,
        paddings: PaddingValues = PaddingValues(
            horizontal = SECTION_ITEM_BUTTON_H_PADDING,
            vertical = SECTION_ITEM_BUTTON_V_PADDING
        ),
        rightView: (@Composable () -> Unit)? = null,
        action: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(c.background2)
                .clickable { action() },
            contentAlignment = Alignment.TopCenter // For divider
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text,
                    modifier = Modifier
                        .padding(paddings),
                    color = c.text,
                )

                SpacerW1()

                rightView?.invoke()
            }

            if (withDivider)
                Divider(
                    color = c.dividerBackground2,
                    modifier = Modifier.padding(start = 18.dp),
                    thickness = 0.5.dp
                )
        }
    }
}
