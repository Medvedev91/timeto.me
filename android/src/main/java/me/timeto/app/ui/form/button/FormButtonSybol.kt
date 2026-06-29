package me.timeto.app.ui.form.button

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.HStack
import me.timeto.app.ui.c
import me.timeto.app.ui.symbol.SymbolView
import me.timeto.shared.Symbol

@Composable
fun FormButtonSymbol(
    title: String,
    symbol: Symbol,
    color: Color,
    withArrow: Boolean,
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

                SymbolView(
                    symbol = symbol,
                    color = color,
                    letterSize = 23.sp,
                    iconSize = 22.dp,
                    emojiSize = 20.sp,
                    modifier = Modifier
                        .padding(end = if (withArrow) 7.dp else 10.dp),
                )

                if (withArrow) {
                    FormButtonArrowView()
                }
            }
        },
        onClick = {
            onClick()
        },
        onLongClick = null,
    )
}
