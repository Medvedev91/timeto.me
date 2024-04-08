package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.KvDb
import me.timeto.shared.developerEmoji

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

    sealed class Paragraph {

        class Title(val text: String) : Paragraph()
        class Subtitle(val text: String) : Paragraph()
        class Text(val text: String) : Paragraph()
        class RedText(val text: String) : Paragraph()
        class ListDash(val items: List<String>) : Paragraph()

        class TimerTypical() : Paragraph()
        class TimerCharts() : Paragraph()
        class TimerMyActivities() : Paragraph()
        class TimerPractice1() : Paragraph()
        class TimerPractice2() : Paragraph()

        class RepeatingsMy() : Paragraph()
        class RepeatingsToday() : Paragraph()
        class RepeatingsPractice1() : Paragraph()
        class RepeatingsPractice2() : Paragraph()

        class ChecklistsExamples() : Paragraph()
        class ChecklistsPractice1() : Paragraph()
        class ChecklistsPractice2() : Paragraph()

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

//

private typealias PTitle = ReadmeSheetVM.Paragraph.Title
private typealias PSubtitle = ReadmeSheetVM.Paragraph.Subtitle
private typealias PText = ReadmeSheetVM.Paragraph.Text
private typealias PRedText = ReadmeSheetVM.Paragraph.RedText

private fun prepParagraphs(): List<ReadmeSheetVM.Paragraph> {

    val paragraphs = mutableListOf<ReadmeSheetVM.Paragraph>()

    paragraphs.add(PText("Hi,"))
    paragraphs.add(PText("Developer is here  $developerEmoji"))
    paragraphs.add(PText("I built this app to manage my productivity. Here I will show how I use it."))
    paragraphs.add(PRedText("This guide is not just a list of features, but my real day-to-day experience."))
    paragraphs.add(PText("I will start with the most important things: timer, repeating tasks and checklists, that is 90% I use. Then tasks, calendar, goals, shortcuts, etc."))
    paragraphs.add(PText("Enjoy!"))

    paragraphs.add(PTitle("Timer"))
    paragraphs.add(PText("You must set a timer for each activity, like eating, working, reading, etc."))
    paragraphs.add(PRedText("There is NO stop option! To finish the current activity, you have to start the next one."))
    paragraphs.add(PText("In other words, once you complete one activity, you must start the timer for the next activity, even sleep or breakfast."))
    paragraphs.add(PText("It helps me to always stay focused on what I have to do. Most of the time my screen looks like a typical pomodoro timer:"))
    paragraphs.add(ReadmeSheetVM.Paragraph.TimerTypical())
    paragraphs.add(PText("This way also provides real 24/7 data on how long everything takes:"))
    paragraphs.add(ReadmeSheetVM.Paragraph.TimerCharts())
    paragraphs.add(PText("The app has some activities by default, but you can add your own. Here are mine:"))
    paragraphs.add(ReadmeSheetVM.Paragraph.TimerMyActivities())
    paragraphs.add(PSubtitle("Practice"))
    paragraphs.add(PText("Try to start a new activity. To do so, back to the home screen, tap the timer at the bottom left, tap \"Work\" and start the timer."))
    paragraphs.add(ReadmeSheetVM.Paragraph.TimerPractice1())
    paragraphs.add(PText("The timer starts. Then tap the timer again to see the \"Summary\" and \"History\"."))
    paragraphs.add(ReadmeSheetVM.Paragraph.TimerPractice2())

    paragraphs.add(PTitle("Repeating Tasks"))
    paragraphs.add(PText("You may think choosing activity and timer for each task is overwhelming. This is where repeating tasks come into play."))
    paragraphs.add(PText("How it works for me:"))
    paragraphs.add(PText("Most of my activities are repeated. I wake up at the same time, then 1 hour to get ready, 2 hours working, 1 hour eating, etc. So I created a repeating task for each of these."))
    paragraphs.add(ReadmeSheetVM.Paragraph.RepeatingsMy())
    paragraphs.add(PText("Each day, these tasks move to the \"Today\" folder. Tasks are sorted by time of day and show how much time is left. It's like a schedule:"))
    paragraphs.add(ReadmeSheetVM.Paragraph.RepeatingsToday())
    paragraphs.add(PRedText("The most important, when I press it, it automatically starts a timer with the right activity."))
    paragraphs.add(PText("You can create not only everyday tasks. For example, watering a cactus once a week or paying for internet once a month, etc."))
    paragraphs.add(PText("I believe this is the main feature of the app. I recommend using it to the max."))
    paragraphs.add(PSubtitle("Practice"))
    paragraphs.add(PText("Let's create a repeating task for a daily workout at 18:00."))
    paragraphs.add(PText("On the home screen, tap at the bottom center to open tasks, tap the repeating icon, tap \"New Repeating Task\" and fill the form:"))
    paragraphs.add(
        ReadmeSheetVM.Paragraph.ListDash(
            items = listOf(
                "Task: Workout;",
                "Activity: Exercises / Health;",
                "Timer: 1h;",
                "Time of the Day: 18:00;",
                "Repetition Period: Every Day;",
                "Tap \"Create\".",
            ),
        )
    )
    paragraphs.add(ReadmeSheetVM.Paragraph.RepeatingsPractice1())
    paragraphs.add(PText("Every day, a \"Workout\" task will be added to the \"Today\" folder."))
    paragraphs.add(PText("This task is already created for today. Open \"Today\" and tap the \"Workout\" task. The timer will automatically start with the right activity."))
    paragraphs.add(ReadmeSheetVM.Paragraph.RepeatingsPractice2())

    paragraphs.add(PTitle("Checklists"))
    paragraphs.add(PText("Checklists are an addition to repeating tasks that are placed under the timer."))
    paragraphs.add(ReadmeSheetVM.Paragraph.ChecklistsExamples())
    paragraphs.add(PSubtitle("Practice"))
    paragraphs.add(PText("Let's create a repeating task for a daily morning routine at 7:00 with a checklist."))
    paragraphs.add(PText("On the home screen, tap at the bottom center to open tasks, tap the repeating icon, tap \"New Repeating Task\" and fill the form:"))
    paragraphs.add(
        ReadmeSheetVM.Paragraph.ListDash(
            items = listOf(
                "Task: Morning Routine;",
                "Activity: Getting Ready;",
                "Timer: 1h;",
                "Time of the Day: 7:00;",
                "Repetition Period: Every Day;",
                "Checklists: Tap \"+ new checklist\", type \"Morning Routine\", \"Save\", \"+ new item\". Create few items like Drink a glass of water, Warm up, Shower, Light breakfast, Medication, Yesterday's reflection, Day plan. Tap \"Done\", again \"Done\";",
                "Tap \"Create\".",
            ),
        )
    )
    paragraphs.add(ReadmeSheetVM.Paragraph.ChecklistsPractice1())
    paragraphs.add(PText("Test it! Open \"Today\" and tap the \"Morning Routine\" task. You will see the checklist."))
    paragraphs.add(ReadmeSheetVM.Paragraph.ChecklistsPractice2())

    paragraphs.add(PTitle("First Steps"))
    paragraphs.add(PText("We learned the timer, repeating tasks, and checklists. The most important points:"))
    paragraphs.add(
        ReadmeSheetVM.Paragraph.ListDash(
            items = listOf(
                "Timer is running all the time;",
                "Using repeating tasks to the max;",
                "Timer, activity and checklist will be set automatically.",
            ),
        )
    )
    paragraphs.add(PRedText("You can already use the app!"))
    paragraphs.add(PText("Right now, try playing with the app: start a new activity, create repeating tasks, checklists. You can rely on my examples."))
    paragraphs.add(PText("Then move on to advanced features."))
    paragraphs.add(PText("Good luck!  🍀"))

    paragraphs.add(ReadmeSheetVM.Paragraph.AskAQuestion())

    return paragraphs
}
