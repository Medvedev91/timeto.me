package me.timeto.app.ui.readme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.R
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.SquircleShape
import me.timeto.app.ui.VStack
import me.timeto.app.ui.c
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.navigation.LocalNavigationLayer

private val pTextLineHeight = 23.sp

@Composable
fun Readme2Fs() {
    val navigationLayer = LocalNavigationLayer.current

    VStack(
        modifier = Modifier
            .background(c.bg),
    ) {

        val scrollState = rememberScrollState()

        Header(
            title = "How to Use the App",
            scrollState = scrollState,
            actionButton = null,
            cancelButton = null,
        )

        VStack(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .weight(1f),
        ) {

            PTextView(
                buildAnnotatedString {
                    append("The main feature of this app is ")
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("goals")
                    }
                    append(":")
                }
            )

            ScreenshotView(resId = R.drawable.readme_goals)

            PTextView(
                buildAnnotatedString {
                    append("Tap a goal to start a timer with the remaining time for that goal:")
                }
            )

            ScreenshotView(resId = R.drawable.readme_timer)

            PTextView(
                buildAnnotatedString {
                    append("Timer is running ")
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("all the time")
                    }
                    append(", 24/7, even for sleep or breakfast. ")
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append("There is no stop option!")
                    }
                    append(" To stop the current goal, you have to start the next one.")
                }
            )

            PTextView(
                buildAnnotatedString {
                    append("You can add a checklist for goals. Useful for morning/evening routines, work, exercises, etc. Like this:")
                }
            )

            ScreenshotView(resId = R.drawable.readme_checklist)

            PTextView(
                buildAnnotatedString {
                    append("This way I control my time and don't forget anything.")
                }
            )

            PTextView(
                buildAnnotatedString {
                    append("Try to adapt it to your life.")
                }
            )

            PTextView(
                buildAnnotatedString {
                    append("Best regards,\nIvan")
                }
            )

            HStack(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {}
        }
    }
}

@Composable
private fun PTextView(
    text: AnnotatedString,
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = H_PADDING)
            .padding(vertical = 8.dp),
        color = c.text,
        lineHeight = pTextLineHeight,
        fontWeight = FontWeight.Normal,
    )
}

private val screenshotBorderColor = c.gray5
private val screenshotSliderShape = SquircleShape(24.dp)

@Composable
private fun ScreenshotView(
    @DrawableRes resId: Int
) {
    Image(
        painter = painterResource(resId),
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxHeight()
            .clip(screenshotSliderShape)
            .border(1.dp, screenshotBorderColor, shape = screenshotSliderShape),
        contentDescription = "Screenshot",
        contentScale = ContentScale.Fit,
    )
}
