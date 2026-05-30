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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
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
                        append("The first you have to do is ")
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
                        append(" will be marked as complete:")
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
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_morning_form,
                    fraction = 0.7f,
                )

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("1. Goal Type")
                        append(" as ")
                        appendGreenSemiBold("Complete Checklist")
                        append(" marks activity as completed after completing the checklist;")
                        appendGreenSemiBold("\n2. Timer Type")
                        append(" as ")
                        appendGreenSemiBold("Fixed Timer")
                        append(" -> ")
                        appendGreenSemiBold("2h")
                        append(" set the default timer.")
                    }
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
                        append(" marked ")
                        appendGreenSemiBold("AS COMPLETED")
                        append(" even if a checklist ")
                        appendRedSemiBold("IS NOT")
                        append(" completed;")
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
                        append(" value. I exercise to stay healthy. ")
                        append("I have to find a way to exercise ")
                        appendGreenSemiBold("EVERY DAY.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("We know, the most difficult thing is getting started. ")
                        append("I just tap ")
                        appendGreenSemiBold("Workout")
                        append(" (feels like I've done the first step), then commute to the place, do my workout, come back, take a shower, and have dinner.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Usually, it takes up to 4 hours. ")
                        appendRedSemiBold("I DO NOT")
                        append(" care about tracking every single step, but ")
                        appendGreenSemiBold("I CARE")
                        append(" I do workout every day.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendRedSemiBold("I DO NOT FORCE MYSELF")
                        append(" completing checklists, setting timer, etc.")
                        append(" Only this way works best for me for ")
                        appendGreenSemiBold("Workout.")
                    }
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        append("Let's see the ")
                        appendGreenSemiBold("Workout's")
                        append(" settings:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_workout_form,
                    fraction = 0.7f,
                )
            }

            item {

                HeaderView("Small Tasks")

                PTextView(
                    buildAnnotatedString {
                        append("We all have plenty of non-urgent tasks that we constantly postpone.")
                        append(" It could be personal matters, housework, etc.")
                        append(" Every day, ")
                        appendGreenSemiBold("I FORCE MYSELF")
                        append(" to spend 30 minutes for that.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("I just tap ")
                        appendGreenSemiBold("Small Tasks")
                        append(" and do these tasks.")
                        append(" After 30 minutes, the activity will be marked as complete.")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_small_tasks_progress,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        append("Settings:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_small_tasks_form,
                    fraction = 0.7f,
                )
            }

            item {

                HeaderView("timeto.me")

                PTextView(
                    buildAnnotatedString {
                        append("As a ")
                        appendGreenSemiBold("timeto.me")
                        append(" developer, I dedicate all the time I can to the project.")
                        append(" Let me share how I manage that.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Tapping the ")
                        appendGreenSemiBold("timeto.me,")
                        append(" I see this:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_timetome_start,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        append("We see")
                        appendGreenSemiBold(" TIMER, CHECKLIST,")
                        append(" and ")
                        appendGreenSemiBold("TASKS.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("I will talk about ")
                        appendGreenSemiBold("TASKS")
                        append(" later. Let's see ")
                        appendGreenSemiBold("CHECKLIST")
                        append(" and ")
                        appendGreenSemiBold("TIMER")
                        append(" for now.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("CHECKLIST.")
                        append(" Every day, I start by answering user questions.")
                        append(" Then, I make a plan for the ")
                        appendGreenSemiBold("NEXT DAY.")
                        append(" Then mark the checklist ")
                        appendGreenSemiBold("AS COMPLETED.")
                        append(" And only then start working on ")
                        appendGreenSemiBold("TODAY'S")
                        append(" tasks.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("I mean, I mark ")
                        appendGreenSemiBold("timeto.me")
                        append(" as completed, and only then start working.")
                        append(" This case may seem ")
                        appendRedSemiBold("ILLOGICAL")
                        append(" but works in ")
                        appendGreenSemiBold("REAL-LIFE.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("It works because, on one hand, I don't forget the essential tasks thanks the checklist, on the other, I always have a list of tasks for today.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("This way, I can constantly follow my long-term plans as a ")
                        appendGreenSemiBold("timeto.me")
                        append(" developer, without overwhelming by \"Task Management\" rituals.")
                    }
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("TIMER.")
                        append(" I use a ")
                        appendGreenSemiBold("POMODORO-LIKE")
                        append(" technique. I set the timer for 45 minutes, take a break, and set the timer again.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("After the timer ends, it turns red and display the overdue time:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_timetome_overdue,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        append("It ")
                        appendRedSemiBold("DOES NOT")
                        append(" mean I'm taking a break immediately.")
                        append(" Sometimes I want to continue working. ")
                        appendGreenSemiBold("KEEP IN MIND:")
                        append(" the most important is ")
                        appendGreenSemiBold("PRACTICAL VALUE.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("You can tap the timer to start a ")
                        appendGreenSemiBold("BREAK")
                        append(" timer:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_timetome_break,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        append("But ")
                        appendGreenSemiBold("HONESTLY,")
                        append(" I don't use this feature. After the break, I just tap ")
                        appendGreenSemiBold("timeto.me")
                        append(" again to start a new 45 min timer.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Settings:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_timetome_form,
                    fraction = 0.7f,
                )
            }

            item {

                HeaderView("Option1")

                PTextView(
                    buildAnnotatedString {
                        withLink(LinkAnnotation.Url(url = "https://option1.io")) {
                            appendBlueSemiBold("option1.io")
                        }
                        append(" is also my personal project.")
                        append(" Here, I'm building a pragmatic window manager for macOS.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Settings are the same as for ")
                        appendGreenSemiBold("timeto.me.")
                    }
                )
            }

            item {

                HeaderView("Work")

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("Work")
                        append(" is a special case.")
                        append(" Working as a developer, I have to track my working hours, there are a set of features for that.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Let's see the settings:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_work_form,
                    fraction = 0.7f,
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("Timer Type")
                        append(" as ")
                        appendGreenSemiBold("Total Stopwatch")
                        append(" makes the timer display ")
                        appendGreenSemiBold("TOTAL TIME")
                        append(" spent on work ")
                        appendGreenSemiBold("FOR TODAY.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("In other words, it's a regular stopwatch ")
                        appendGreenSemiBold("(COUNT UP),")
                        append(" but it ")
                        appendRedSemiBold("DOES NOT")
                        append(" start from ")
                        appendRedSemiBold("00:00,")
                        append(" it continues for the activity.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("This way, I can always see how much time I've spent on work today.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Another feature is adding notes to the current task:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_work_note,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        append("Tap the edit icon to make a note about the task you're working on.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Then, in the history, you can see how much time you spent on each task:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_work_history,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        append("If you're a ")
                        appendGreenSemiBold("NIGHT OWL")
                        append(" and work after ")
                        appendGreenSemiBold("12:00 AM,")
                        append(" you can set a ")
                        appendGreenSemiBold("DAY START TIME")
                        append(" setting, to track the working time right.")
                    }
                )
            }

            item {

                HeaderView("Reading")

                PTextView(
                    buildAnnotatedString {
                        append("I don't know how it works, but I see that constant reading ")
                        appendGreenSemiBold("MAKES PEOPLE BETTER.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Some people set a goal to read for a hour a day. It ")
                        appendRedSemiBold("DOES NOT")
                        append(" work for me.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("I like to read a fixed number of chapters per day. In the book I'm reading now, I read ")
                        appendGreenSemiBold("FIVE")
                        append(" chapters a day.")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_reading_progress,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )

                PTextView(
                    buildAnnotatedString {
                        append("Technically, it works like a counter. In practice, I tap ")
                        appendGreenSemiBold("Reading")
                        append(" and start reading. Then I count how many chapters I've read.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Settings:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_reading_form,
                    fraction = 0.7f,
                )

                PTextView(
                    buildAnnotatedString {
                        append("Timer ")
                        appendRedSemiBold("DOES NOT")
                        append(" matter.")
                    }
                )
            }

            item {

                HeaderView("Music")

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("Music")
                        append(" is my hobby. I try to play the piano twice a day.")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_music_progress,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )

                PTextView(
                    buildAnnotatedString {
                        append("Settings are very similar to ")
                        appendGreenSemiBold("Reading:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_music_form,
                    fraction = 0.7f,
                )

                PTextView(
                    buildAnnotatedString {
                        append("Timer ")
                        appendRedSemiBold("DOES NOT")
                        append(" matter.")
                    }
                )
            }

            item {

                HeaderView("Free Time")

                PTextView(
                    buildAnnotatedString {
                        append("I use ")
                        appendGreenSemiBold("Free Time")
                        append(" for activities I don't need to track, like eating, walking, meeting, etc.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("I would like to highlight ")
                        appendGreenSemiBold("THREE")
                        append(" key points:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_free_time_start,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("1. ALWAYS COMPLETED.")
                        append(" No sense in setting goals.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("2. STOPWATCH (COUNT UP FROM 00:00).")
                        append(" Helps me control the time I spend on different tasks.")
                        append(" Sometimes it's useful to notice that I spend too much time on something.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("3. NESTED CHECKLISTS.")
                        append(" The ")
                        appendGreenSemiBold("Free Time")
                        append(" checklist contains all sorts of things.")
                        append(" For example, the ")
                        appendGreenSemiBold("Shopping")
                        append(" item contains a nested checklist with a list of goods I have to buy.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Settings:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_free_time_form,
                    fraction = 0.7f,
                )
            }

            item {

                // todo If you have any questions please ask me.

                PTextView(
                    buildAnnotatedString {
                        append("Best regards,\n")
                        withLink(LinkAnnotation.Url(url = "https://github.com/Medvedev91")) {
                            appendBlueSemiBold("Ivan")
                        }
                    },
                    modifier = Modifier
                        .padding(top = 80.dp, bottom = 20.dp),
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
            .padding(top = 48.dp, bottom = 8.dp),
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
