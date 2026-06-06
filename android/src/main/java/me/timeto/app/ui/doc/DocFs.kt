package me.timeto.app.ui.doc

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import me.timeto.app.askAQuestion
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SquircleShape
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.Header__titleFontSize
import me.timeto.app.ui.header.Header__titleFontWeight
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.roundedShape
import me.timeto.shared.vm.doc.DocVm

private val pTextLineHeight = 23.sp

@Composable
fun DocFs() {

    val navigationLayer = LocalNavigationLayer.current
    val scrollState = rememberLazyListState()

    val (vm, state) = rememberVm {
        DocVm()
    }

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
                        appendGreenSemiBold("TIMER")
                        append(" and ")
                        appendGreenSemiBold("CHECKLIST.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("TIMER")
                        append(" helps me limit my morning routine time. I set 2 hours, it's enough to do everything smoothly, but I don't have to spend more time.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("CHECKLIST")
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
                        append("Two differences:")
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
                        append(" Here's how I manage that.")
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
                        append(" later. Let's see the ")
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
                        append(" Then mark the checklist ")
                        appendGreenSemiBold("AS COMPLETED.")
                        append(" And only then start working.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("It may seem ")
                        appendRedSemiBold("ILLOGICAL")
                        append(" that I mark ")
                        appendGreenSemiBold("timeto.me")
                        append(" as completed and only then start working on it.")
                        append(" But it works in ")
                        appendGreenSemiBold("REAL-LIFE.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("It works because, I don't know how much work I'll be able to get done today,")
                        append(" and it's really frustrating that one of the activities will always remain uncompleted.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("I don't forget the essential tasks thanks the checklist, then I work whatever hours I can.")
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
                        append(" technique. I set the timer for 45 minutes, then take a break, and set the timer again.")
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
                        append(" I suggest set it for 2 hours before you wake up to refresh activities while you sleep.")
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

                HeaderView("Sleep")

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("Sleep")
                        append(" is another special case:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_sleep_start,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("1. ALWAYS COMPLETED.")
                        append(" Since during the day I try to mark all activities as completed, it’s really frustrating that one of them will always remain uncompleted.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("That is why, despite the checklist, it's better when ")
                        appendGreenSemiBold("Sleep")
                        append(" is always completed.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("2. STOPWATCH (COUNT UP FROM 00:00).")
                        append(" Some people prefer to set a timer for sleep, for example, for 7 hours.")
                        append(" That doesn't work for me.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("I sleep as much as I feel I need to today.")
                        append(" I prefer to use a stopwatch and check in the morning how long I slept.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Settings:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_sleep_form,
                    fraction = 0.7f,
                )
            }

            item {

                HeaderView("Conclusion")

                PTextView(
                    buildAnnotatedString {
                        append("That is all the activities I use.")
                        append(" By default, the app comes with almost the same activities and settings.")
                        append(" You can use this setup.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("I want to give you ")
                        appendGreenSemiBold("THREE TIPS")
                        append(" dealing with activities that are extremely important to me: ")
                        appendGreenSemiBold("PROCRASTINATION, ")
                        appendGreenSemiBold("PRIORITIES,")
                        append(" and ")
                        appendGreenSemiBold("FLEXIBILITY.")
                    }
                )
            }

            item {

                HeaderView("Procrastination")

                PTextView(
                    buildAnnotatedString {
                        append("All of us have been there - when it's crystal clear what we should do, but we just don't do it.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("I solve it simply: I open the app, see uncompleted activity, and tap on it without thinking.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendRedSemiBold("DO NOT THINK!")
                        appendGreenSemiBold(" JUST TAP THE ACTIVITY IMMEDIATELY!")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendBlueSemiBold("ONCE AGAIN:")
                        appendGreenSemiBold(" OPEN THE APP AND TAP ON UNCOMPLETED ACTIVITY WITHOUT THINKING!")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("The most difficult is to get started.")
                        append(" Tapping the activity feels like you've made the first step.")
                        append(" If you start thinking, you will continue procrastinating.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("It always works for me. For example, I open the app, see an uncompleted ")
                        appendGreenSemiBold("Piano,")
                        append(" tap it immediately, make some tea, and start practicing.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("I hope you get the idea. Just tap ")
                        appendGreenSemiBold("WITHOUT THINKING.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendBlueSemiBold("IMPORTANT:")
                        append(" You should trust your activities. Only important things should be here. Otherwise, you will be overwhelmed and fail.")
                    }
                )
            }

            item {

                HeaderView("Priorities")

                PTextView(
                    buildAnnotatedString {
                        append("We usually start the day with the most urgent tasks and sacrifice long-term goals, because long-term goals are not usually urgent.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("No matter what happens, I try to start my day focusing only on what really matters to me.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("My perfect day: after the morning routine, I read, then practice the piano, then work on my personal projects, and only then I get to work.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Only this way I'm able to keep developing this app for years.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("It's very difficult, but you have to remember what's really important to you.")
                    }
                )
            }

            item {

                HeaderView("Flexibility")

                PTextView(
                    buildAnnotatedString {
                        append("Unexpected things happen every day. We have to accept this fact.")
                        append(" It's ")
                        appendGreenSemiBold("ABSOLUTELY OKAY")
                        append(" if we can't do some activity today.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("For example, I have a meeting today, so I don't have time to ")
                        appendGreenSemiBold("Workout.")
                        append(" It's absolutely okay. ")
                        appendGreenSemiBold("I JUST MARK WORKOUT AS COMPLETED")
                        append(" and move to other activities.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("It may seem strange that I mark ")
                        appendGreenSemiBold("Workout")
                        append(" as completed even if I haven't done it, but I just don't want to get distracted by uncompleted activity.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Another case: You may notice that I have the same activities list for every day.")
                        append(" But I don't need the ")
                        appendGreenSemiBold("Work")
                        append(" activity on weekends.")
                        append(" There's an option to hide activities on selected days, but ")
                        appendGreenSemiBold("HONESTLY,")
                        append(" I don't use it.")
                        append(" On weekends, every morning, while planning my day, I just mark ")
                        appendGreenSemiBold("Work")
                        append(" as complete and focus on the remaining activities.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendBlueSemiBold("KEEP IN MIND:")
                        append(" the most important is ")
                        appendGreenSemiBold("REAL-LIFE")
                        append(" and ")
                        appendGreenSemiBold("PRACTICAL VALUE.")
                    }
                )
            }

            item {

                HeaderView("DO NOT RUSH")

                PTextView(
                    buildAnnotatedString {
                        append("I believe we do more when we don't rush.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("In the ")
                        appendGreenSemiBold("Procrastination")
                        append(" section, I suggest to start an activity without thinking, it's right, but it doesn't mean you have to immediately jump into action.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("For example, I open the app, see an uncompleted ")
                        appendGreenSemiBold("Reading,")
                        append(" and tap it immediately.")
                        append(" Then I go to the park with a book and start reading there.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("Stay calm and start slowly.")
                    }
                )
            }

            item {

                SeparatorView()

                PTextView(
                    buildAnnotatedString {
                        append("That's all for ")
                        appendGreenSemiBold("Activities.")
                        append(" Let's move to other features.")
                    }
                )
            }

            item {

                HeaderView("Timer")

                PTextView(
                    buildAnnotatedString {
                        append("You may notice that every screenshot has a timer.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Timer is running all the time.")
                        append(" There is NO stop option! To stop the current activity, you have to start the next one.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("This way I always remember what I have to do. Also, it provides 24/7 data on how long everything takes:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_timer_summary,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                HeaderView("Tasks")

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("TASKS")
                        append(" is a big part of the app.")
                        append(" Let's create a task:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_tasks_field,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )

                PTextView(
                    buildAnnotatedString {
                        append("You can select an activity for this task.")
                        append(" But ")
                        appendGreenSemiBold("HONESTLY,")
                        append(" I always keep the default ")
                        appendGreenSemiBold("Free Time:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_tasks_form,
                    fraction = 0.7f,
                )

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("Nice!")
                        append(" Now you will not forget to buy fruits:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_tasks_example1,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )

                PTextView(
                    buildAnnotatedString {
                        append("You can tap it to start a stopwatch:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_tasks_started,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )

                PTextView(
                    buildAnnotatedString {
                        append("But ")
                        appendGreenSemiBold("HONESTLY,")
                        append(" I newer tap on tasks.")
                        append(" I prefer to complete the task first, then just delete it by swiping left:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_tasks_delete,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                HeaderView("Task Folders")

                ScreenshotView(
                    resId = R.drawable.doc_folders_example,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("TODAY:")
                        append(" Tasks you need to do today.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("TOMORROW:")
                        append(" Tasks that will be moved to ")
                        appendGreenSemiBold("TODAY")
                        append(" folder tomorrow.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Let's schedule a call with Ann for tomorrow.")
                        append(" Just tap the folder and add the task:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_folders_tomorrow,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )

                PTextView(
                    buildAnnotatedString {
                        append("If you want to move it to another folder, like ")
                        appendGreenSemiBold("TODAY,")
                        append(" swipe right and tap the folder you need:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_folders_swipe,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("CUSTOM FOLDERS:")
                        append(" I use a few folders:")
                        append("\n- tasks and ideas for timeto.me;")
                        append("\n- tasks and ideas for Option1;")
                        append("\n- interesting quotes from books I've read.")
                        append("\nNote that the last one is not actually \"tasks\", but it’s very convenient to store them this way.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Sometimes I create temporary folders. For example, while I was writing this guide, I created a folder to store the ideas.")
                    }
                )
            }

            item {

                HeaderView("Conclusion")

                PTextView(
                    buildAnnotatedString {
                        append("I want to give you ")
                        appendGreenSemiBold("TWO TIPS")
                        append(" dealing with ")
                        appendGreenSemiBold("TASKS")
                        append(" that are extremely important to me:")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendRedSemiBold("NEVER KEEP ANYTHING IN MIND!")
                        append(" As soon as a task or idea comes to mind, leave it to the list.")
                        append(" We get really tired when we try to keep everything in mind. ")
                        appendGreenSemiBold("TRY TO EXPERIENCE")
                        append(" the feeling when you don't need to remember anything. ")
                        appendGreenSemiBold("EVERYTHING")
                        append(" in the task list.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        appendGreenSemiBold("ADD NEW TASKS ONLY TO THE TOMORROW FOLDER.")
                        append(" If I add this to ")
                        appendGreenSemiBold("TODAY,")
                        append(" it breaks my plans and overwhelms me.")
                    }
                )
            }

            item {

                HeaderView("Repeating Tasks")

                PTextView(
                    buildAnnotatedString {
                        append("There are many repeating tasks or events that we have to remember.")
                        append(" Like birthdays, recurring payments, special dates, etc.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("I have about 30, and have no idea how to keep them all in mind. Everything in the app.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("You can create any kind of repeating task:")
                        append("\n- Every Day;")
                        append("\n- Every N Days;")
                        append("\n- Days of the Week;")
                        append("\n- Days of the Month;")
                        append("\n- Days of the Year.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("Let's create a birthday reminder:")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_repeating_form_1,
                    fraction = 0.7f,
                )
            }

            item {

                PTextView(
                    buildAnnotatedString {
                        append("Now, on ")
                        appendGreenSemiBold("MARCH 30,")
                        append(" this task will appear in ")
                        appendGreenSemiBold("TODAY")
                        append(" folder, so you can't miss it.")
                    }
                )

                PTextView(
                    buildAnnotatedString {
                        append("One more example: paying for internet service at the ")
                        appendGreenSemiBold("END OF THE MONTH.")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_repeating_form_2,
                    fraction = 0.7f,
                )

                PTextView(
                    buildAnnotatedString {
                        append("Also, these tasks will appear on the ")
                        appendGreenSemiBold("CALENDAR.")
                    }
                )
            }

            item {

                HeaderView("Calendar")

                ScreenshotView(
                    resId = R.drawable.doc_calendar_button,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )

                PTextView(
                    buildAnnotatedString {
                        append("A regular calendar where you can schedule tasks. As I mentioned, repeating tasks are also here.")
                    }
                )

                ScreenshotView(
                    resId = R.drawable.doc_calendar_screen,
                    fraction = 0.7f,
                    innerPadding = 4.dp,
                )
            }

            item {

                SeparatorView()

                HeaderView("Let's Go")

                PTextView(
                    buildAnnotatedString {
                        append("Right now, I suggest you set up and use activities. It will be enough to get started.")
                    },
                )

                PTextView(
                    buildAnnotatedString {
                        append("I hope this app will lead you to what matters to you the most in your life, as it leads me.")
                    },
                )

                PTextView(
                    buildAnnotatedString {
                        append("If you have any questions, please feel free to ask.")
                    },
                )

                Text(
                    text = "Ask a Question",
                    modifier = Modifier
                        .padding(start = H_PADDING - 1.dp)
                        .clip(roundedShape)
                        .background(c.blue)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clickable {
                            askAQuestion("Documentation")
                        },
                    color = c.white,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    text = "Go to the App",
                    modifier = Modifier
                        .padding(start = H_PADDING - 1.dp, top = 12.dp)
                        .clip(roundedShape)
                        .background(c.blue)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clickable {
                            vm.onRead()
                            navigationLayer.close()
                        },
                    color = c.white,
                    fontWeight = FontWeight.SemiBold,
                )

                PTextView(
                    buildAnnotatedString {
                        append("Best regards,\n")
                        withLink(LinkAnnotation.Url(url = "https://github.com/Medvedev91")) {
                            appendBlueSemiBold("Ivan")
                        }
                    },
                    modifier = Modifier
                        .padding(top = 40.dp, bottom = 20.dp),
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
private fun SeparatorView() {
    ZStack(
        modifier = Modifier
            .padding(horizontal = H_PADDING, vertical = 20.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(c.divider),
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
