package me.timeto.shared.vm.readme

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.KvDb
import me.timeto.shared.vm.__Vm

class ReadmeVm(
    defaultItem: DefaultItem,
) : __Vm<ReadmeVm.State>() {

    enum class DefaultItem {
        basics, pomodoro,
    }

    data class State(
        val tabUi: TabUi,
    ) {
        val title = "How to Use the App"
        val tabsUi: List<TabUi> = listOf(tabBasics, tabAdvanced)
    }

    override val state = MutableStateFlow(
        State(
            tabUi = when (defaultItem) {
                DefaultItem.basics -> tabBasics
                DefaultItem.pomodoro -> tabAdvanced
            },
        )
    )

    fun setTabUi(tabUi: TabUi) {
        state.update { it.copy(tabUi = tabUi) }
    }

    ///

    data class TabUi(
        val id: String,
        val title: String,
        val paragraphs: List<Paragraph>,
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

        class PomodoroExamples() : Paragraph(isSlider = true)

        class GoalsExamples() : Paragraph(isSlider = true)

        class CalendarExamples() : Paragraph(isSlider = true)

        class AskAQuestion() : Paragraph() {
            val title = "Ask a Question"
            val subject: String =
                when (val fs = KvDb.KEY.FEEDBACK_SUBJECT.selectStringOrNullCached()) {
                    null -> "Feedback Readme"
                    else -> "$fs Readme"
                }
        }
    }
}

///

private typealias PTitle = ReadmeVm.Paragraph.Title
private typealias PText = ReadmeVm.Paragraph.Text
private typealias PTextHighlight = ReadmeVm.Paragraph.TextHighlight

private val tabBasics = ReadmeVm.TabUi(
    id = "tab_basics",
    title = "Basics",
    paragraphs = listOf(

        PText("Hi,"),
        PText("Developer is here."),
        PText("I built this app to manage my productivity. Here I will show how I use it."),
        PTextHighlight("It is not just a list of features, it is my real day-to-day experience."),
        PText("Enjoy!"),

        PTitle("Timer"),
        PText("You must set a timer for each activity, like eating, working, reading, etc."),
        PTextHighlight("Timer is running all the time, even for sleep or breakfast."),
        PText("There is NO stop option! To stop the current activity, you have to start the next one."),
        PText("In other words, once you complete one activity, you must start the timer for the next one."),
        PText("This way I always remember what I have to do. Most of the time my screen looks like a typical pomodoro timer:"),
        ReadmeVm.Paragraph.TimerTypical(),
        PText("This way also provides 24/7 data on how long everything takes:"),
        ReadmeVm.Paragraph.TimerCharts(),
        PText("The app has some activities by default. Here are mine:"),
        ReadmeVm.Paragraph.TimerMyActivities(),
        PTitle("Practice"),
        PText("Let's start a 45 min timer to work."),
        ReadmeVm.Paragraph.TimerPractice1(),
        PText("The timer starts. Let's see the summary and history."),
        ReadmeVm.Paragraph.TimerPractice2(),

        PTitle("Repeating Tasks"),
        PText("You may think choosing activity and timer for each task is overwhelming. This is where repeating tasks come in."),
        PText("How I use it:"),
        PText("Most of my activities are repeated. I wake up at the same time, then 1 hour to get ready, 2 hours working, 1 hour eating, etc. So I created a repeating task for each of these."),
        ReadmeVm.Paragraph.RepeatingsMy(),
        PText("Each day, these tasks move to the \"Today\" folder. It's like a schedule:"),
        ReadmeVm.Paragraph.RepeatingsToday(),
        PTextHighlight("The most important, when I press it, it automatically starts a timer. You don't have to choose an activity with a timer."),
        PText("You can create not only everyday tasks. Like watering a cactus once a week or paying for internet once a month."),
        PText("I believe this is the main feature of the app. I recommend using it to the max."),
        PTitle("Practice"),
        PText("Let's create a repeating task for a daily workout at 18:00."),
        ReadmeVm.Paragraph.RepeatingsPractice1(),
        PText("Every day, a \"Workout\" task will be added to the \"Today\" folder."),
        PText("This task is already created for today. Open \"Today\" and tap the \"Workout\" task. The timer will start automatically."),
        ReadmeVm.Paragraph.RepeatingsPractice2(),

        PTitle("First Steps"),
        PText("We learned timer and repeating tasks. It is 80% I use."),
        PText("From now on, you have to use a timer for everything you do."),
        PText("Right now, I recommend creating repeating tasks to make a daily schedule. You can rely on my example:"),
        ReadmeVm.Paragraph.RepeatingsMy(),
        PText("Try to follow that schedule the rest of this day. The next day, move on to advanced features."),
        PText("Good luck!  🍀"),

        ReadmeVm.Paragraph.AskAQuestion(),
    ),
)

private val tabAdvanced = ReadmeVm.TabUi(
    id = "tab_advanced",
    title = "Advanced",
    paragraphs = listOf(

        PTitle("Pomodoro"),
        PText("I use the Pomodoro only for work:\n- start the timer for 45 min,\n- work until the timer rings,\n- tap the timer to start a break,\n- tap it again to restart."),
        PTextHighlight("In other words you only need one tap before the break and one tap after the break."),
        ReadmeVm.Paragraph.PomodoroExamples(),

        PTitle("Checklists"),
        PText("Checklists are an addition to repeating tasks that are placed under the timer."),
        ReadmeVm.Paragraph.ChecklistsExamples(),
        PTitle("Practice"),
        PText("Let's create a repeating task for a daily morning routine at 7:00 with a checklist."),
        ReadmeVm.Paragraph.ChecklistsPractice1(),
        PText("Test it! Open \"Today\" and tap the \"Morning Routine\" task. You will see the checklist."),
        ReadmeVm.Paragraph.ChecklistsPractice2(),

        PTitle("Goals"),
        PText("Look at the bottom of the screenshot. For me, I set a goal to work 8 hours a day and read for 30 minutes."),
        ReadmeVm.Paragraph.GoalsExamples(),
        PText("To create goals, go to the activity edit form."),

        PTitle("Tasks List"),
        PText("A typical task list with folders. But there are 2 special folders: today and tmrw (tomorrow)."),
        PText("Today - tasks from repeating tasks and calendar go here on a set day, you can add your own."),
        PText("Tmrw (tomorrow) - tasks that will be tomorrow including repeating tasks and calendar. In other words, the tasks that will be moved to \"Today\" tomorrow."),
        PText("Swipe left to delete and right to edit."),

        PTitle("Calendar"),
        PText("A typical calendar. Tasks from the calendar will be displayed not only in \"Today\" but also on the Home Screen. \"Call Ann\" example:"),
        ReadmeVm.Paragraph.CalendarExamples(),

        PTitle("Shortcuts"),
        PText("Real life example: I meditate every day, to start I open a special video on YouTube."),
        PText("Shortcuts automate this. When I start the \"Meditation\" activity, the video will start automatically."),
        PText("This works especially well with repeating tasks. \"Meditation\" is automatically created every day, I just tap on it, the timer and video starts."),

        PTitle("Day Start Time"),
        PText("Especially for night owls. You can set the time when repeating tasks will be added for the next day. Default 00:00."),

        PTitle("Conclusion"),
        PText("I hope the app will improve your life like it improved mine. I would be very happy to get feedback and answer questions."),

        ReadmeVm.Paragraph.AskAQuestion(),
    ),
)
