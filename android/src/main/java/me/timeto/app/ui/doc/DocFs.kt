package me.timeto.app.ui.doc

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.R
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SquircleShape
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.Header__titleFontSize
import me.timeto.app.ui.header.Header__titleFontWeight

private val pTextLineHeight = 23.sp

@Composable
fun DocFs() {

    val scrollState = rememberLazyListState()

    Screen {

        Header(
            title = "How to Use the App",
            scrollState = scrollState,
            actionButton = null,
            // todo if not force
            cancelButton = null,
        )

        // todo banner if force
        // todo handle back if force

        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .weight(1f),
        ) {

            item {

                PTextView(
                    buildAnnotatedString {
                        append("I built this app to manage my productivity. Here, I will ")
                        appendGreenSemiBold("SHARE")
                        append(" my productivity system and how I use the app.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("My system ")
                        appendRedSemiBold("IS NOT")
                        append(" about time tracking, ")
                        appendRedSemiBold("IS NOT")
                        append(" about getting nice activity charts, ")
                        appendRedSemiBold("IS NOT")
                        append(" about reducing wasted time.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("My system ")
                        appendGreenSemiBold("IS ALL ABOUT")
                        append(" achieving my ")
                        appendGreenSemiBold("REAL-LIFE")
                        append(" goals.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("For example, ")
                        appendRedSemiBold("I DO NOT")
                        append(" care how much time I waste, but ")
                        appendGreenSemiBold("I CARE")
                        append(" if I read a book every day, ")
                        appendGreenSemiBold("I CARE")
                        append(" if I exercise every day, ")
                        appendGreenSemiBold("I CARE")
                        append(" if I don't forget anything, ")
                        appendGreenSemiBold("I CARE")
                        append(" if I constantly follow my long-term goals.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Now I will show ")
                        appendGreenSemiBold("MY PERSONAL")
                        append(" app setup with ")
                        appendGreenSemiBold("REAL-LIFE")
                        append(" scenarios.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendBlueSemiBold("IMPORTANT!")
                        append(" Life is hard, life is tricky. No way to have a perfect app or system.")
                        append(" Some solutions seem strange, but they work, they help me achieve my ")
                        appendGreenSemiBold("REAL-LIFE")
                        append(" goals.")
                    }
                )
            }

            item {

                HeaderView("Activities")

                PTextView(
                    buildAnnotatedString {
                        append("The first thing you have to do is ")
                        appendGreenSemiBold("SET UP ACTIVITIES.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("This is how ")
                        appendGreenSemiBold("MY ACTIVITIES")
                        append(" look in the morning, right after I wake up:")
                    }
                )

                ScreenshotView(R.drawable.doc_activities_morning)
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        append("During the day, I have to turn it into this:")
                    }
                )

                ScreenshotView(R.drawable.doc_activities_evening)
            }

            item {
                PTextView(
                    buildAnnotatedString {
                        append("I ")
                        appendGreenSemiBold("ONLY")
                        append(" create activities to follow my ")
                        appendGreenSemiBold("REAL-LIFE")
                        append(" goals. I ")
                        appendRedSemiBold("DO NOT")
                        append(" create activities just to track, like commute, eating, etc.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Every activity has ")
                        appendGreenSemiBold("PRACTICAL")
                        append(" value. Now I'll show how I set up and use each activity.")
                    }
                )
            }

            item {

                HeaderView("Morning")

                PTextView(
                    buildAnnotatedString {
                        append("Right after waking up, I tap the ")
                        appendGreenSemiBold("Morning")
                        append(" activity. This is what I see:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_morning_start,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        append("There are two important things: ")
                        appendGreenSemiBold("timer")
                        append(" and ")
                        appendGreenSemiBold("checklist.")
                    }
                )
                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("Timer")
                        append(" helps me limit my morning routine time. I set 2 hours, it's enough to do everything smoothly, but I don't have to spend more time.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("Checklist")
                        append(" helps me make sure I don't forget anything. I'm just doing step by step.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Once I finish the checklist, ")
                        appendGreenSemiBold("Morning")
                        append(" will be marked as complete. Like this:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_morning_completed,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        append("To make ")
                        appendGreenSemiBold("Morning")
                        append(" works this way, you have to set up two options:")
                        appendGreenSemiBold("\n1. Goal Type")
                        append(" as ")
                        appendGreenSemiBold("Complete Checklist")
                        append(" marks activity as completed after completing the checklist;")
                        appendGreenSemiBold("\n2. Timer Type")
                        append(" as ")
                        appendGreenSemiBold("Fixed Timer")
                        append(" set the default timer.")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_morning_form,
                    fraction = 0.7f,
                )
            }

            item {

                HeaderView("Workout")

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("Workout")
                        append(" works absolutely ")
                        appendGreenSemiBold("DIFFERENT.")
                        append(" Just after tapping ")
                        appendGreenSemiBold("Workout,")
                        append(" I see this:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_workout_start,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        append("There are two differences:")
                        appendGreenSemiBold("\n1. Workout")
                        append(" are immediately marked ")
                        appendGreenSemiBold("AS COMPLETED")
                        append(" even if checklist ")
                        appendRedSemiBold("IS NOT")
                        append(" completed yet;")
                        appendGreenSemiBold("\n2.")
                        append(" Instead of timer ")
                        appendRedSemiBold("(COUNT DOWN)")
                        append(" we see a stopwatch ")
                        appendGreenSemiBold("(COUNT UP FROM 00:00).")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Why it works this way? As I said, I focus on ")
                        appendGreenSemiBold("PRACTICAL")
                        append(" value. I exercise every day to stay healthy. ")
                        appendRedSemiBold("I DO NOT")
                        append(" care about sports results, but ")
                        appendGreenSemiBold("I CARE")
                        append(" I do it ")
                        appendGreenSemiBold("CONSTANTLY.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("The most difficult thing is getting started. ")
                        append("That's why ")
                        appendRedSemiBold("I DO NOT FORCE MYSELF")
                        append(" to complete checklists, set a timer, etc. I just tap ")
                        appendGreenSemiBold("Workout,")
                        append(" then get ready, then commute to the place, do my workout, come back, take a shower, and have dinner.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Usually, it takes up to 4 hours. ")
                        appendRedSemiBold("I DO NOT")
                        append(" care about tracking every single step, but ")
                        appendGreenSemiBold("I CARE")
                        append(" I did the workout today.")
                        append(" This way works for me very well.")
                    }
                )
            }

            item {

                // todo If you have any questions please ask me.

                PTextView(
                    buildAnnotatedString {
                        // todo link
                        append("Best regards,\nIvan")
                    },
                    modifier = Modifier
                        .padding(top = 80.dp),
                )
            }

            // todo close button if force

            item {
                ZStack(Modifier.navigationBarsPadding())
            }
        }
    }
}

private fun AnnotatedString.Builder.appendRedSemiBold(text: String) {
    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = c.red)) {
        append(text)
    }
}

private fun AnnotatedString.Builder.appendGreenSemiBold(text: String) {
    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = c.green)) {
        append(text)
    }
}

private fun AnnotatedString.Builder.appendBlue(text: String) {
    withStyle(style = SpanStyle(color = c.blue)) {
        append(text)
    }
}

private fun AnnotatedString.Builder.appendBlueSemiBold(text: String) {
    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = c.blue)) {
        append(text)
    }
}

@Composable
private fun HeaderView(
    text: String,
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = H_PADDING)
            .padding(top = 40.dp, bottom = 8.dp),
        color = c.text,
        fontWeight = Header__titleFontWeight,
        fontSize = Header__titleFontSize,
    )
}

@Composable
private fun PTextView(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = H_PADDING)
            .padding(vertical = 8.dp),
        color = c.text,
        lineHeight = pTextLineHeight,
        fontWeight = FontWeight.Normal,
    )
}

///

private val screenshotBorderColor: Color = c.gray5
private val screenshotSliderShape = SquircleShape(24.dp)

@Composable
private fun ScreenshotView(
    @DrawableRes resId: Int,
    fraction: Float = 1f,
    innerPadding: Dp = 0.dp,
) {
    Image(
        painter = painterResource(id = resId),
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .fillMaxWidth(fraction = fraction)
            .clip(screenshotSliderShape)
            .border(1.dp, screenshotBorderColor, shape = screenshotSliderShape)
            .padding(innerPadding),
        contentDescription = "Screenshot",
        contentScale = ContentScale.Fit,
    )
}

