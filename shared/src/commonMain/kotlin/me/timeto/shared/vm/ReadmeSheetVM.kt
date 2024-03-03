package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
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

        class Text(val text: String) : Paragraph()

        class ChartImages() : Paragraph()
    }
}

//

private typealias PText = ReadmeSheetVM.Paragraph.Text

private fun prepParagraphs(): List<ReadmeSheetVM.Paragraph> {
    val paragraphs = mutableListOf<ReadmeSheetVM.Paragraph>()
    paragraphs.add(PText("Hi!"))
    paragraphs.add(PText("Developer is here  $developerEmoji"))
    paragraphs.add(PText("I built this app to manage my productivity. Here I will show how I use it."))
    paragraphs.add(PText("First of all, it is a pragmatic guide. I mean, it is NOT just a list of features, but my real day-to-day experience."))
    paragraphs.add(PText("I will start with the most important things: timer, repeating tasks and checklists. That is 90% I use."))
    paragraphs.add(PText("Enjoy!"))
    paragraphs.add(ReadmeSheetVM.Paragraph.ChartImages())
    return paragraphs
}
