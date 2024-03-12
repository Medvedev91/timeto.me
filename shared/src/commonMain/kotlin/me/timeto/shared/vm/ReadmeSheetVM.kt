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
            title = "How to Use",
            paragraphs = prepParagraphs(),
        )
    )

    sealed class Paragraph {

        class Title(val text: String) : Paragraph()

        class Text(val text: String) : Paragraph()

        class RedText(val text: String) : Paragraph()

        class ListDash(val items: List<String>) : Paragraph()

        class ChartImages() : Paragraph()

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
private typealias PText = ReadmeSheetVM.Paragraph.Text
private typealias PRedText = ReadmeSheetVM.Paragraph.RedText

private fun prepParagraphs(): List<ReadmeSheetVM.Paragraph> {
    val paragraphs = mutableListOf<ReadmeSheetVM.Paragraph>()
    paragraphs.add(PText("Hi!"))
    paragraphs.add(PText("Developer is here  $developerEmoji"))
    paragraphs.add(PText("I built this app to manage my productivity. Here I will show how I use it."))
    paragraphs.add(PText("First of all, it is a pragmatic guide. I mean, it is NOT just a list of features, but my real day-to-day experience."))
    paragraphs.add(PText("I will start with the most important things: timer, repeating tasks and checklists. That is 90% I use."))
    paragraphs.add(PText("Enjoy!"))
    paragraphs.add(PTitle("Timer"))
    paragraphs.add(PText("You must set a timer for each activity, like eating, working, reading, etc."))
    paragraphs.add(PText("Once you complete one activity, you must immediately set the timer for the next one, even if it is a \"sleeping\" activity."))
    paragraphs.add(PRedText("There is no \"stop\" option! Timer is running all the time."))
    paragraphs.add(ReadmeSheetVM.Paragraph.ChartImages())
    paragraphs.add(PRedText("There is no \"stop\" option! Timer is running all the time."))
    paragraphs.add(ReadmeSheetVM.Paragraph.ActivitiesImage())
    paragraphs.add(PTitle("Repeating Tasks"))
    paragraphs.add(PText("We learned the timer, repeating tasks, and checklists. Here are the most important points:"))
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
