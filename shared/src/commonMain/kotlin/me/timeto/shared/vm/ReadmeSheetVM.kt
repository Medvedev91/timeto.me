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

        class ChartImages() : Paragraph()

        class TimerTypical() : Paragraph()

        class TimerMyActivities() : Paragraph()

        class ActivitiesImage() : Paragraph()

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
    paragraphs.add(ReadmeSheetVM.Paragraph.ChartImages())
    paragraphs.add(PText("The app has some activities by default, but you can add your own. Here are mine:"))
    paragraphs.add(ReadmeSheetVM.Paragraph.TimerMyActivities())

    paragraphs.add(
        ReadmeSheetVM.Paragraph.ListDash(
            items = listOf(
                "Timer is running all the time;",
                "Using repeating tasks to the max;",
                "Timer, activity and checklist will be set automatically.",
            ),
        )
    )

    paragraphs.add(ReadmeSheetVM.Paragraph.AskAQuestion())

    return paragraphs
}
