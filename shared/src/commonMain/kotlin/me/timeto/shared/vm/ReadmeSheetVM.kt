package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.KvDb

class ReadmeSheetVM : __VM<ReadmeSheetVM.State>() {

    data class State(
        val title: String,
        val paragraphs: List<Paragraph>,
    )

    override val state = MutableStateFlow(
        State(
            title = "How to Use the App",
            paragraphs = prepParagraphs(),
        )
    )

    sealed class Paragraph(
        val isSlider: Boolean = false,
    ) {

        class Title(val text: String) : Paragraph()
        class Text(val text: String) : Paragraph()
        class TextHighlight(val text: String) : Paragraph()

        class TimerTypical() : Paragraph(isSlider = true)
        class TimerCharts() : Paragraph(isSlider = true)
        class TimerMyActivities() : Paragraph(isSlider = true)
        class TimerPractice1() : Paragraph(isSlider = true)
        class TimerPractice2() : Paragraph(isSlider = true)

        class RepeatingsMy() : Paragraph(isSlider = true)
        class RepeatingsToday() : Paragraph(isSlider = true)
        class RepeatingsPractice1() : Paragraph(isSlider = true)
        class RepeatingsPractice2() : Paragraph(isSlider = true)

        class ChecklistsExamples() : Paragraph(isSlider = true)
        class ChecklistsPractice1() : Paragraph(isSlider = true)
        class ChecklistsPractice2() : Paragraph(isSlider = true)

        class GoalsExamples() : Paragraph(isSlider = true)

        class CalendarExamples() : Paragraph(isSlider = true)

        class AskAQuestion() : Paragraph() {
            val title = "Ask a Question"
            val subject: String =
                when (val fs = KvDb.KEY.FEEDBACK_SUBJECT.getFromDIOrNull()) {
                    null -> "Feedback Readme"
                    else -> "$fs Readme"
                }
        }
    }
}

///

private typealias PTitle = ReadmeSheetVM.Paragraph.Title
private typealias PText = ReadmeSheetVM.Paragraph.Text
private typealias PTextHighlight = ReadmeSheetVM.Paragraph.TextHighlight

private fun prepParagraphs(): List<ReadmeSheetVM.Paragraph> {

    val paragraphs = mutableListOf<ReadmeSheetVM.Paragraph>()

    paragraphs.add(PText("Hi,"))
    paragraphs.add(PText("Developer is here."))
    paragraphs.add(PText("I built this app to manage my productivity. Here I will show how I use it."))
    paragraphs.add(PTextHighlight("It is not just a list of features, it is my real day-to-day experience."))
    paragraphs.add(PText("Enjoy!"))

    paragraphs.add(PTitle("Timer"))
    paragraphs.add(PText("You must set a timer for each activity, like eating, working, reading, etc."))
    paragraphs.add(PTextHighlight("Timer is running all the time, even for sleep or breakfast."))
    paragraphs.add(PText("There is NO stop option! To stop the current activity, you have to start the next one."))
    paragraphs.add(PText("In other words, once you complete one activity, you must start the timer for the next one."))
    paragraphs.add(PText("This way I always remember what I have to do. Most of the time my screen looks like a typical pomodoro timer:"))
    paragraphs.add(ReadmeSheetVM.Paragraph.TimerTypical())
    paragraphs.add(PText("This way also provides 24/7 data on how long everything takes:"))
    paragraphs.add(ReadmeSheetVM.Paragraph.TimerCharts())
    paragraphs.add(PText("The app has some activities by default. Here are mine:"))
    paragraphs.add(ReadmeSheetVM.Paragraph.TimerMyActivities())
    paragraphs.add(PTitle("Practice"))
    paragraphs.add(PText("Let's start a 45 min timer to work."))
    paragraphs.add(ReadmeSheetVM.Paragraph.TimerPractice1())
    paragraphs.add(PText("The timer starts. Let's see the summary and history."))
    paragraphs.add(ReadmeSheetVM.Paragraph.TimerPractice2())

    paragraphs.add(PTitle("Repeating Tasks"))
    paragraphs.add(PText("You may think choosing activity and timer for each task is overwhelming. This is where repeating tasks come in."))
    paragraphs.add(PText("How I use it:"))
    paragraphs.add(PText("Most of my activities are repeated. I wake up at the same time, then 1 hour to get ready, 2 hours working, 1 hour eating, etc. So I created a repeating task for each of these."))
    paragraphs.add(ReadmeSheetVM.Paragraph.RepeatingsMy())
    paragraphs.add(PText("Each day, these tasks move to the \"Today\" folder. It's like a schedule:"))
    paragraphs.add(ReadmeSheetVM.Paragraph.RepeatingsToday())
    paragraphs.add(PTextHighlight("The most important, when I press it, it automatically starts a timer. You don't have to choose an activity with a timer."))
    paragraphs.add(PText("You can create not only everyday tasks. Like watering a cactus once a week or paying for internet once a month."))
    paragraphs.add(PText("I believe this is the main feature of the app. I recommend using it to the max."))
    paragraphs.add(PTitle("Practice"))
    paragraphs.add(PText("Let's create a repeating task for a daily workout at 18:00."))
    paragraphs.add(ReadmeSheetVM.Paragraph.RepeatingsPractice1())
    paragraphs.add(PText("Every day, a \"Workout\" task will be added to the \"Today\" folder."))
    paragraphs.add(PText("This task is already created for today. Open \"Today\" and tap the \"Workout\" task. The timer will start automatically."))
    paragraphs.add(ReadmeSheetVM.Paragraph.RepeatingsPractice2())

    paragraphs.add(PTitle("First Steps"))
    paragraphs.add(PText("We learned timer and repeating tasks. It is 80% I use."))
    paragraphs.add(PText("From now on, you have to use a timer for everything you do."))
    paragraphs.add(PText("Right now, I recommend creating repeating tasks to make a daily schedule. You can rely on my example:"))
    paragraphs.add(ReadmeSheetVM.Paragraph.RepeatingsMy())
    paragraphs.add(PText("Try to follow that schedule the rest of this day. The next day, move on to advanced features."))
    paragraphs.add(PText("Good luck!  🍀"))

    paragraphs.add(PTitle("Checklists"))
    paragraphs.add(PText("Checklists are an addition to repeating tasks that are placed under the timer."))
    paragraphs.add(ReadmeSheetVM.Paragraph.ChecklistsExamples())
    paragraphs.add(PTitle("Practice"))
    paragraphs.add(PText("Let's create a repeating task for a daily morning routine at 7:00 with a checklist."))
    paragraphs.add(ReadmeSheetVM.Paragraph.ChecklistsPractice1())
    paragraphs.add(PText("Test it! Open \"Today\" and tap the \"Morning Routine\" task. You will see the checklist."))
    paragraphs.add(ReadmeSheetVM.Paragraph.ChecklistsPractice2())

    paragraphs.add(PTitle("Goals"))
    paragraphs.add(PText("Look at the bottom of the screenshot. For me, I set a goal to work 8 hours a day and read for 30 minutes."))
    paragraphs.add(ReadmeSheetVM.Paragraph.GoalsExamples())
    paragraphs.add(PText("To create goals, go to the activity edit form."))

    paragraphs.add(PTitle("Tasks List"))
    paragraphs.add(PText("A typical task list with folders. But there are 2 special folders: today and tmrw (tomorrow)."))
    paragraphs.add(PText("Today - tasks from repeating tasks and calendar go here on a set day, you can add your own."))
    paragraphs.add(PText("Tmrw (tomorrow) - tasks that will be tomorrow including repeating tasks and calendar. In other words, the tasks that will be moved to \"Today\" tomorrow."))
    paragraphs.add(PText("Swipe left to delete and right to edit."))

    paragraphs.add(PTitle("Calendar"))
    paragraphs.add(PText("A typical calendar. Tasks from the calendar will be displayed not only in \"Today\" but also on the Home Screen. \"Call Ann\" example:"))
    paragraphs.add(ReadmeSheetVM.Paragraph.CalendarExamples())

    paragraphs.add(PTitle("Shortcuts"))
    paragraphs.add(PText("Real life example: I meditate every day, to start I open a special video on YouTube."))
    paragraphs.add(PText("Shortcuts automate this. When I start the \"Meditation\" activity, the video will start automatically."))
    paragraphs.add(PText("This works especially well with repeating tasks. \"Meditation\" is automatically created every day, I just tap on it, the timer and video starts."))

    paragraphs.add(PTitle("Day Start Time"))
    paragraphs.add(PText("Especially for night owls. You can set the time when repeating tasks will be added for the next day. Default 00:00."))

    paragraphs.add(PTitle("Conclusion"))
    paragraphs.add(PText("I hope the app will improve your life like it improved mine. I would be very happy to get feedback and answer questions."))

    paragraphs.add(ReadmeSheetVM.Paragraph.AskAQuestion())

    return paragraphs
}
