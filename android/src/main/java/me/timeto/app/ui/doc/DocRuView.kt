package me.timeto.app.ui.doc

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import me.timeto.app.R
import me.timeto.app.askAQuestion
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.roundedShape
import me.timeto.shared.vm.doc.DocVm

fun LazyListScope.DocRuView(
    vm: DocVm,
    state: DocVm.State,
    forceRead: Boolean,
) {

    if (forceRead) {
        item {

            DocForceTextView(
                text = "I force you to read this guide.",
                modifier = Modifier
                    .padding(top = 20.dp),
            )

            DocForceTextView(
                text = "Please DO NOT SKIP!",
            )

            DocForceTextView(
                text = "I did my best to help you get started.",
            )

            DocSeparatorView()
        }
    }

    item {

        DocTextView(
            buildAnnotatedString {
                append("I built this app to manage my productivity.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Here, I will ")
                docAppendGreenSemiBold("SHARE")
                append(" my productivity system and how I use the app.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("My system ")
                docAppendRedSemiBold("IS NOT")
                append(" about time tracking, ")
                docAppendRedSemiBold("IS NOT")
                append(" about getting nice activity charts, ")
                docAppendRedSemiBold("IS NOT")
                append(" about reducing wasted time.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("My system ")
                docAppendGreenSemiBold("IS ALL ABOUT")
                append(" achieving my ")
                docAppendGreenSemiBold("REAL-LIFE")
                append(" goals.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("For example, ")
                docAppendRedSemiBold("I DO NOT")
                append(" care how much time I waste, but ")
                docAppendGreenSemiBold("I CARE")
                append(" if I read a book every day, ")
                docAppendGreenSemiBold("I CARE")
                append(" if I exercise every day, ")
                docAppendGreenSemiBold("I CARE")
                append(" if I don't forget anything, ")
                docAppendGreenSemiBold("I CARE")
                append(" if I constantly follow my long-term goals.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Now I will show ")
                docAppendGreenSemiBold("MY PERSONAL")
                append(" app setup with ")
                docAppendGreenSemiBold("REAL-LIFE")
                append(" scenarios.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendBlueSemiBold("IMPORTANT!")
                append(" Life is hard, life is tricky. No way to have a perfect app or system.")
                append(" Some solutions seem strange, but they work, they help me achieve my ")
                docAppendGreenSemiBold("REAL-LIFE")
                append(" goals.")
            }
        )
    }

    item {

        DocHeaderView("Activities")

        DocTextView(
            buildAnnotatedString {
                append("The first thing you have to do is ")
                docAppendGreenSemiBold("SET UP ACTIVITIES.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("This is how ")
                docAppendGreenSemiBold("MY ACTIVITIES")
                append(" look in the morning, right after I wake up:")
            }
        )

        DocScreenshotView(R.drawable.doc_activities_morning)
    }

    item {

        DocTextView(
            buildAnnotatedString {
                append("During the day, I have to turn it into this:")
            }
        )

        DocScreenshotView(R.drawable.doc_activities_evening)
    }

    item {
        DocTextView(
            buildAnnotatedString {
                append("I ")
                docAppendGreenSemiBold("ONLY")
                append(" create activities to follow my ")
                docAppendGreenSemiBold("REAL-LIFE")
                append(" goals. I ")
                docAppendRedSemiBold("DO NOT")
                append(" create activities just to track, like commute, eating, etc.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Every activity has ")
                docAppendGreenSemiBold("PRACTICAL")
                append(" value. Now I'll show how I use each activity.")
            }
        )
    }

    item {

        DocHeaderView("Morning")

        DocTextView(
            buildAnnotatedString {
                append("Right after waking up, I tap the ")
                docAppendGreenSemiBold("Morning")
                append(" activity. This is what I see:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_morning_start,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                append("Two important things: ")
                docAppendGreenSemiBold("TIMER")
                append(" and ")
                docAppendGreenSemiBold("CHECKLIST.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("TIMER")
                append(" helps me limit my morning routine time. I set 2 hours, it's enough to do everything smoothly, but I don't have to spend more time.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("CHECKLIST")
                append(" helps me make sure I don't forget anything. I'm just doing step by step.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Once I finish the checklist, ")
                docAppendGreenSemiBold("Morning")
                append(" will be marked as complete:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_morning_completed,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                append("To make ")
                docAppendGreenSemiBold("Morning")
                append(" works this way, you have to set up two options:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_morning_form,
            fraction = 0.7f,
        )
    }

    item {

        DocHeaderView("Workout")

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("Workout")
                append(" works absolutely ")
                docAppendGreenSemiBold("DIFFERENT.")
                append(" Just after tapping ")
                docAppendGreenSemiBold("Workout,")
                append(" I see this:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_workout_start,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                append("Two differences:")
                docAppendGreenSemiBold("\n1. Workout")
                append(" marked ")
                docAppendGreenSemiBold("AS COMPLETED")
                append(" even if a checklist ")
                docAppendRedSemiBold("IS NOT")
                append(" completed;")
                docAppendGreenSemiBold("\n2.")
                append(" Instead of timer ")
                docAppendRedSemiBold("(COUNT DOWN)")
                append(" we see a stopwatch ")
                docAppendGreenSemiBold("(COUNT UP FROM 00:00).")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Why it works this way? As I said, I focus on ")
                docAppendGreenSemiBold("PRACTICAL")
                append(" value. I exercise to stay healthy. ")
                append("I have to find a way to exercise ")
                docAppendGreenSemiBold("EVERY DAY.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("We know, the most difficult thing is getting started. ")
                append("I just tap ")
                docAppendGreenSemiBold("Workout")
                append(" (feels like I've done the first step), then commute to the place, do my workout, come back, take a shower, and have dinner.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Usually, it takes up to 4 hours. ")
                docAppendRedSemiBold("I DO NOT")
                append(" care about tracking every single step, but ")
                docAppendGreenSemiBold("I CARE")
                append(" I do workout every day.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendRedSemiBold("I DO NOT FORCE MYSELF")
                append(" completing checklists, setting timer, etc.")
                append(" Only this way works best for me for ")
                docAppendGreenSemiBold("Workout.")
            }
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                append("Let's see the ")
                docAppendGreenSemiBold("Workout's")
                append(" settings:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_workout_form,
            fraction = 0.7f,
        )
    }

    item {

        DocHeaderView("Small Tasks")

        DocTextView(
            buildAnnotatedString {
                append("We all have plenty of non-urgent tasks that we constantly postpone.")
                append(" It could be personal matters, housework, etc.")
                append(" Every day, ")
                docAppendGreenSemiBold("I FORCE MYSELF")
                append(" to spend 30 minutes for that.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("I just tap ")
                docAppendGreenSemiBold("Small Tasks")
                append(" and do these tasks.")
                append(" After 30 minutes, the activity will be marked as complete.")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_small_tasks_progress,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                append("Settings:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_small_tasks_form,
            fraction = 0.7f,
        )
    }

    item {

        DocHeaderView("timeto.me")

        DocTextView(
            buildAnnotatedString {
                append("As a ")
                docAppendGreenSemiBold("timeto.me")
                append(" developer, I dedicate all the time I can to the project.")
                append(" Here's how I manage that.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Tapping the ")
                docAppendGreenSemiBold("timeto.me,")
                append(" I see this:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_timetome_start,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                append("We see")
                docAppendGreenSemiBold(" TIMER, CHECKLIST,")
                append(" and ")
                docAppendGreenSemiBold("TASKS.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("I will talk about ")
                docAppendGreenSemiBold("TASKS")
                append(" later. Let's see the ")
                docAppendGreenSemiBold("CHECKLIST")
                append(" and ")
                docAppendGreenSemiBold("TIMER")
                append(" for now.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("CHECKLIST.")
                append(" Every day, I start by answering user questions.")
                append(" Then mark the checklist ")
                docAppendGreenSemiBold("AS COMPLETED.")
                append(" And only then start working.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("It may seem ")
                docAppendRedSemiBold("ILLOGICAL")
                append(" that I mark ")
                docAppendGreenSemiBold("timeto.me")
                append(" as completed and only then start working on it.")
                append(" But it works in ")
                docAppendGreenSemiBold("REAL-LIFE.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("It works because, I don't know how much work I'll be able to get done today,")
                append(" and it's really frustrating that one of the activities will always remain uncompleted.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("I don't forget the essential tasks thanks the checklist, then I work whatever hours I can.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("This way, I can constantly follow my long-term plans as a ")
                docAppendGreenSemiBold("timeto.me")
                append(" developer, ")
                docAppendRedSemiBold("WITHOUT")
                append(" overwhelming by \"Task Management\" rituals.")
            }
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("TIMER.")
                append(" I use a ")
                docAppendGreenSemiBold("POMODORO-LIKE")
                append(" technique. I set the timer for 45 minutes, then take a break, and set the timer again.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("After the timer ends, it turns red and display the overdue time:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_timetome_overdue,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                append("It ")
                docAppendRedSemiBold("DOES NOT")
                append(" mean I'm taking a break immediately.")
                append(" Sometimes I want to continue working. ")
                docAppendGreenSemiBold("KEEP IN MIND:")
                append(" the most important is ")
                docAppendGreenSemiBold("PRACTICAL VALUE.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("You can tap the timer to start a ")
                docAppendGreenSemiBold("BREAK")
                append(" timer:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_timetome_break,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                append("But ")
                docAppendGreenSemiBold("HONESTLY,")
                append(" I don't use this feature. After the break, I just tap ")
                docAppendGreenSemiBold("timeto.me")
                append(" again to start a new 45 min timer.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Settings:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_timetome_form,
            fraction = 0.7f,
        )
    }

    item {

        DocHeaderView("Option1")

        DocTextView(
            buildAnnotatedString {
                withLink(LinkAnnotation.Url(url = "https://option1.io")) {
                    docAppendBlueSemiBold("option1.io")
                }
                append(" is also my personal project.")
                append(" Here, I'm building a pragmatic window manager for macOS.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Settings are the same as for ")
                docAppendGreenSemiBold("timeto.me.")
            }
        )
    }

    item {

        DocHeaderView("Work")

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("Work")
                append(" is a special case.")
                append(" Working as a developer, I have to track my working hours, there are a set of features for that.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Let's see the settings:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_work_form,
            fraction = 0.7f,
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("Total Stopwatch")
                append(" makes the timer display ")
                docAppendGreenSemiBold("TOTAL TIME")
                append(" spent on work ")
                docAppendGreenSemiBold("FOR TODAY.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("In other words, it's a regular stopwatch ")
                docAppendGreenSemiBold("(COUNT UP),")
                append(" but it ")
                docAppendRedSemiBold("DOES NOT")
                append(" start from ")
                docAppendRedSemiBold("00:00,")
                append(" it continues for the activity.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("This way, I can always see how much time I've spent on work today.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Another feature is adding notes to the current task:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_work_note,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                append("Tap the edit icon to make a note about the task you're working on.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Then, in the history, you can see how much time you spent on each task:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_work_history,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                append("If you're a ")
                docAppendGreenSemiBold("NIGHT OWL")
                append(" and work after ")
                docAppendGreenSemiBold("12:00 AM,")
                append(" you can set a ")
                docAppendGreenSemiBold("DAY START TIME")
                append(" setting, to track the working time right.")
                append(" I suggest set it for 2 hours before you wake up to refresh activities while you sleep.")
            }
        )
    }

    item {

        DocHeaderView("Reading")

        DocTextView(
            buildAnnotatedString {
                append("I don't know how it works, but I see that constant reading ")
                docAppendGreenSemiBold("MAKES PEOPLE BETTER.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Some people set a goal to read for a hour a day. It ")
                docAppendRedSemiBold("DOES NOT")
                append(" work for me.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("I like to read a fixed number of chapters per day. In the book I'm reading now, I read ")
                docAppendGreenSemiBold("FIVE")
                append(" chapters a day.")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_reading_progress,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )

        DocTextView(
            buildAnnotatedString {
                append("Technically, it works like a counter. In practice, I tap ")
                docAppendGreenSemiBold("Reading")
                append(" and start reading. Then I count how many chapters I've read.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Settings:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_reading_form,
            fraction = 0.7f,
        )

        DocTextView(
            buildAnnotatedString {
                append("Timer ")
                docAppendRedSemiBold("DOES NOT")
                append(" matter.")
            }
        )
    }

    item {

        DocHeaderView("Music")

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("Music")
                append(" is my hobby. I try to play the piano twice a day.")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_music_progress,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )

        DocTextView(
            buildAnnotatedString {
                append("Settings are very similar to ")
                docAppendGreenSemiBold("Reading:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_music_form,
            fraction = 0.7f,
        )

        DocTextView(
            buildAnnotatedString {
                append("Timer ")
                docAppendRedSemiBold("DOES NOT")
                append(" matter.")
            }
        )
    }

    item {

        DocHeaderView("Free Time")

        DocTextView(
            buildAnnotatedString {
                append("I use ")
                docAppendGreenSemiBold("Free Time")
                append(" for activities I don't need to track, like eating, walking, meeting, etc.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("I would like to highlight ")
                docAppendGreenSemiBold("THREE")
                append(" key points:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_free_time_start,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("1. ALWAYS COMPLETED.")
                append(" No sense in setting goals.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("2. STOPWATCH (COUNT UP FROM 00:00).")
                append(" Helps me control the time I spend on different tasks.")
                append(" Sometimes it's useful to notice that I spend too much time on something.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("3. NESTED CHECKLISTS.")
                append(" The ")
                docAppendGreenSemiBold("Free Time")
                append(" checklist contains all sorts of things.")
                append(" For example, the ")
                docAppendGreenSemiBold("Shopping")
                append(" item contains a nested checklist with a list of goods I have to buy.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Settings:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_free_time_form,
            fraction = 0.7f,
        )
    }

    item {

        DocHeaderView("Sleep")

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("Sleep")
                append(" is another special case:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_sleep_start,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("1. ALWAYS COMPLETED.")
                append(" Since during the day I try to mark all activities as completed, it’s really frustrating that one of them will always remain uncompleted.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("That is why, despite the checklist, it's better when ")
                docAppendGreenSemiBold("Sleep")
                append(" is always completed.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("2. STOPWATCH (COUNT UP FROM 00:00).")
                append(" Some people prefer to set a timer for sleep, for example, for 7 hours.")
                append(" That doesn't work for me.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("I sleep as much as I feel I need to today.")
                append(" I prefer to use a stopwatch to check in the morning how long I slept.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Settings:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_sleep_form,
            fraction = 0.7f,
        )
    }

    item {

        DocHeaderView("Conclusion")

        DocTextView(
            buildAnnotatedString {
                append("That is all the activities I use.")
                append(" By default, the app comes with almost the same activities and settings.")
                docAppendGreenSemiBold(" You can use this setup.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("I want to give you ")
                docAppendGreenSemiBold("THREE TIPS")
                append(" dealing with activities that are extremely important to me: ")
                docAppendGreenSemiBold("PROCRASTINATION, ")
                docAppendGreenSemiBold("PRIORITIES,")
                append(" and ")
                docAppendGreenSemiBold("FLEXIBILITY.")
            }
        )
    }

    item {

        DocHeaderView("Procrastination")

        DocTextView(
            buildAnnotatedString {
                append("All of us have been there - when it's crystal clear what we should do, but we just don't do it.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("I solve it simply: I open the app, see uncompleted activity, and tap on it without thinking.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendRedSemiBold("DO NOT THINK!")
                docAppendGreenSemiBold(" JUST TAP THE ACTIVITY IMMEDIATELY!")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendBlueSemiBold("ONCE AGAIN:")
                docAppendGreenSemiBold(" OPEN THE APP AND TAP ON UNCOMPLETED ACTIVITY WITHOUT THINKING!")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("The most difficult is to get started.")
                append(" Tapping the activity feels like you've made the first step.")
                append(" If you start thinking, you will continue procrastinating.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("It always works for me. For example, I open the app, see an uncompleted ")
                docAppendGreenSemiBold("Piano,")
                append(" tap it immediately, make some tea, and start practicing.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("I hope you get the idea. Just tap ")
                docAppendGreenSemiBold("WITHOUT THINKING.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendBlueSemiBold("IMPORTANT:")
                append(" You should trust your activities. Only important things should be here. Otherwise, you will be overwhelmed and fail.")
            }
        )
    }

    item {

        DocHeaderView("Priorities")

        DocTextView(
            buildAnnotatedString {
                append("We usually start the day with the most urgent tasks and ")
                docAppendRedSemiBold("SACRIFICE")
                append(" long-term goals, because long-term goals are not usually urgent.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("NO MATTER")
                append(" what happens, I try to start my day focusing only on what really ")
                docAppendGreenSemiBold("MATTERS TO ME.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("My perfect day: after the morning routine, I read, then practice the piano, then work on my personal projects, and only then I get to work.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Only this way I'm able to keep developing this app for years.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("It's very difficult, but you have to remember what is really ")
                docAppendGreenSemiBold("IMPORTANT TO YOU.")
            }
        )
    }

    item {

        DocHeaderView("Flexibility")

        DocTextView(
            buildAnnotatedString {
                append("Unexpected things happen every day. We have to accept this fact.")
                append(" It's ")
                docAppendGreenSemiBold("ABSOLUTELY OKAY")
                append(" if we can't do some activity today.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("For example, I have a meeting today, so I don't have time to ")
                docAppendGreenSemiBold("Workout.")
                append(" It's absolutely okay. ")
                docAppendGreenSemiBold("I JUST MARK WORKOUT AS COMPLETED")
                append(" and move to other activities.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("It may seem strange that I mark ")
                docAppendGreenSemiBold("Workout")
                append(" as completed even if ")
                docAppendRedSemiBold("I HAVEN'T")
                append(" done it, but I just don't want to get distracted by uncompleted activity.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendBlueSemiBold("NOTE: ")
                append("I have the same activities for every day, even if ")
                docAppendRedSemiBold("I DON'T")
                append(" need ")
                docAppendGreenSemiBold("Work")
                append(" activity on weekends.")
                append(" There is an option to hide activities on selected days, but ")
                docAppendGreenSemiBold("HONESTLY,")
                append(" I don't use it.")
                append(" On weekends, every morning, while planning my day, I just mark ")
                docAppendGreenSemiBold("Work")
                append(" as complete and focus on the remaining activities.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendBlueSemiBold("KEEP IN MIND:")
                append(" the most important is ")
                docAppendGreenSemiBold("REAL-LIFE")
                append(" and ")
                docAppendGreenSemiBold("PRACTICAL VALUE.")
            }
        )
    }

    item {

        DocHeaderView("DO NOT RUSH")

        DocTextView(
            buildAnnotatedString {
                append("I believe we do more when we don't rush.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("In the ")
                docAppendGreenSemiBold("Procrastination")
                append(" section, I suggest to start an activity without thinking, it's right, but it doesn't mean you have to immediately jump into action.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("For example, I open the app, see an uncompleted ")
                docAppendGreenSemiBold("Reading,")
                append(" and tap it immediately.")
                append(" Then I go to the park with a book and start reading there.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("Stay calm and start slowly.")
            }
        )
    }

    item {

        DocSeparatorView()

        DocTextView(
            buildAnnotatedString {
                append("That's all for ")
                docAppendGreenSemiBold("Activities.")
                append(" Let's move to other features.")
            }
        )
    }

    item {

        DocHeaderView("Timer")

        DocTextView(
            buildAnnotatedString {
                append("You may notice that every screenshot has a timer.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Timer is running ")
                docAppendGreenSemiBold("ALL THE TIME.")
                append(" There is ")
                docAppendRedSemiBold("NO STOP OPTION!")
                append(" To stop the current activity, you have to start the next one.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("This way I always remember what I have to do. Also, it provides 24/7 data on how long everything takes:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_timer_summary,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocHeaderView("Tasks")

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("TASKS")
                append(" is a big part of the app.")
                append(" Let's create a task:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_tasks_field,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )

        DocTextView(
            buildAnnotatedString {
                append("You can select an activity for this task.")
                append(" But ")
                docAppendGreenSemiBold("HONESTLY,")
                append(" I always keep the default ")
                docAppendGreenSemiBold("Free Time:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_tasks_form,
            fraction = 0.7f,
        )

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("Nice!")
                append(" Now you will not forget to buy fruits:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_tasks_example1,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )

        DocTextView(
            buildAnnotatedString {
                append("You can tap it to start a stopwatch:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_tasks_started,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )

        DocTextView(
            buildAnnotatedString {
                append("But ")
                docAppendGreenSemiBold("HONESTLY,")
                append(" I never tap on tasks.")
                append(" I prefer to complete the task first, then just delete it by swiping left:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_tasks_delete,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocHeaderView("Task Folders")

        DocScreenshotView(
            resId = R.drawable.doc_folders_example,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("TODAY:")
                append(" Tasks you need to do today.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("TOMORROW:")
                append(" Tasks that will be moved to ")
                docAppendGreenSemiBold("TODAY")
                append(" folder tomorrow.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Let's schedule a call with Ann for tomorrow.")
                append(" Just tap the folder and add the task:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_folders_tomorrow,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )

        DocTextView(
            buildAnnotatedString {
                append("If you want to move it to another folder, like ")
                docAppendGreenSemiBold("TODAY,")
                append(" swipe right and tap the folder you need:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_folders_swipe,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("CUSTOM FOLDERS:")
                append(" I use a few folders:")
                append("\n- tasks and ideas for ")
                docAppendGreenSemiBold("timeto.me;")
                append("\n- other tasks and ideas.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Sometimes I create temporary folders. For example, while I was writing this guide, I created a folder to store the ideas.")
            }
        )
    }

    item {

        DocHeaderView("Conclusion")

        DocTextView(
            buildAnnotatedString {
                append("I want to give you ")
                docAppendGreenSemiBold("TWO TIPS")
                append(" dealing with ")
                docAppendGreenSemiBold("TASKS")
                append(" that are extremely important to me:")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendRedSemiBold("NEVER KEEP ANYTHING IN MIND!")
                append(" As soon as a task or idea comes to mind, leave it to the list.")
                append(" We get really tired when we try to keep everything in mind. ")
                docAppendGreenSemiBold("TRY TO EXPERIENCE")
                append(" the feeling when you don't need to remember anything. ")
                docAppendGreenSemiBold("EVERYTHING")
                append(" in the task list.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                docAppendGreenSemiBold("ADD NEW TASKS ONLY TO THE TOMORROW FOLDER.")
                append(" If I add this to ")
                docAppendGreenSemiBold("TODAY,")
                append(" it breaks my plans and overwhelms me.")
            }
        )
    }

    item {

        DocHeaderView("Notes")

        DocScreenshotView(
            resId = R.drawable.doc_notes_button,
            fraction = 0.7f,
        )

        DocTextView(
            buildAnnotatedString {
                append("I use notes to keep interesting quotes from ")
                docAppendGreenSemiBold("books")
                append(" I've read.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("You can create as many note folders as you like.")
            }
        )
    }

    item {

        DocHeaderView("Repeating Tasks")

        DocTextView(
            buildAnnotatedString {
                append("There are many repeating tasks or events that we have to remember.")
                append(" Like birthdays, recurring payments, special dates, etc.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("I have about 30, and have no idea how to keep them all in mind. ")
                docAppendGreenSemiBold("EVERYTHING IN THE APP.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("You can create any kind of repeating task:")
                append("\n- Every Day;")
                append("\n- Every N Days;")
                append("\n- Days of the Week;")
                append("\n- Days of the Month;")
                append("\n- Days of the Year.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("Let's create a birthday reminder:")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_repeating_form_1,
            fraction = 0.7f,
        )
    }

    item {

        DocTextView(
            buildAnnotatedString {
                append("Now, on ")
                docAppendGreenSemiBold("MARCH 30,")
                append(" this task will appear in ")
                docAppendGreenSemiBold("TODAY")
                append(" folder, so you can't miss it.")
            }
        )

        DocTextView(
            buildAnnotatedString {
                append("One more example: paying for internet service at the ")
                docAppendGreenSemiBold("END OF THE MONTH.")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_repeating_form_2,
            fraction = 0.7f,
        )

        DocTextView(
            buildAnnotatedString {
                append("Also, these tasks will appear on the ")
                docAppendGreenSemiBold("CALENDAR.")
            }
        )
    }

    item {

        DocHeaderView("Calendar")

        DocScreenshotView(
            resId = R.drawable.doc_calendar_button,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )

        DocTextView(
            buildAnnotatedString {
                append("A regular calendar where you can schedule tasks. As I mentioned, repeating tasks are also here.")
            }
        )

        DocScreenshotView(
            resId = R.drawable.doc_calendar_screen,
            fraction = 0.7f,
            innerPadding = 4.dp,
        )
    }

    item {

        DocHeaderView("Widgets")

        DocScreenshotView(
            resId = R.drawable.doc_widget_android,
            fraction = 0.7f,
        )
    }

    item {

        DocSeparatorView()

        DocHeaderView("Let's Go")

        DocTextView(
            buildAnnotatedString {
                append("I hope my app will lead you to what matters the most in your life.")
            },
        )

        DocTextView(
            buildAnnotatedString {
                append("Please feel free to ask me any questions - ")
                withLink(
                    link = LinkAnnotation.Clickable(
                        tag = "Asc a Question",
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                fontStyle = FontStyle.Normal,
                            ),
                        ),
                        linkInteractionListener = {
                            askAQuestion(state.askQuestionSubject)
                        },
                    ),
                ) {
                    docAppendBlueSemiBold("Ask")
                    append("\u00A0") // Prevent line break
                    docAppendBlueSemiBold("a")
                    append("\u00A0") // Prevent line break
                    docAppendBlueSemiBold("Question.")
                }
            },
        )

        HStack(
            modifier = Modifier
                .padding(top = 16.dp, start = H_PADDING - 1.dp),
        ) {

            Image(
                painter = painterResource(id = R.drawable.my_photo),
                modifier = Modifier
                    .size(60.dp)
                    .clip(roundedShape),
                contentDescription = "Ivan's photo",
                contentScale = ContentScale.Fit,
            )

            DocTextView(
                buildAnnotatedString {
                    append("Wish you all the best,\n")
                    withLink(LinkAnnotation.Url(url = "https://github.com/Medvedev91")) {
                        docAppendBlueSemiBold("Ivan")
                    }
                },
            )
        }

        val navigationLayer = LocalNavigationLayer.current

        Text(
            text = "Go to the App",
            modifier = Modifier
                .padding(start = H_PADDING - 1.dp, top = 60.dp, bottom = 20.dp)
                .clip(roundedShape)
                .background(c.blue)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable {
                    vm.onRead()
                    navigationLayer.close()
                },
            color = c.white,
            fontWeight = FontWeight.SemiBold,
        )
    }

    item {
        ZStack(Modifier.navigationBarsPadding())
    }
}
