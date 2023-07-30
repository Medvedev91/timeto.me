package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.VStack
import me.timeto.app.c

@Composable
fun ReadmeSheet(
    layer: WrapperView.Layer,
) {

    VStack(
        modifier = Modifier
            .background(c.bg)
    ) {

        VStack(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(vertical = 24.dp),
        ) {

            Text(
                "Set a timer for each task to stay focused.",
                style = prepTextStyle(fontWeight = FontWeight.Bold)
            )

            Text(
                "No \"stop\" option is the main feature of this app. Once you have completed one activity, you have to set a timer for the next one, even if it's a \"sleeping\" activity.",
                modifier = Modifier.padding(top = 8.dp),
                style = prepTextStyle()
            )

            val s8 = buildAnnotatedString {
                append("This time-tracking approach provides real 24/7 data on how long everything takes. You can see it on the ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("Chart")
                }
                append(". ")
            }

            Text(
                text = s8,
                modifier = Modifier.padding(top = 8.dp),
                style = prepTextStyle(),
            )
        }

        Sheet__BottomViewClose {
            layer.close()
        }
    }
}

@Composable
private fun prepTextStyle(
    fontWeight: FontWeight = FontWeight.Normal
) = LocalTextStyle.current.merge(
    TextStyle(
        color = c.textSecondary.copy(alpha = 0.6f),
        fontSize = 14.sp,
        fontWeight = fontWeight,
        lineHeight = 18.sp,
    )
)
