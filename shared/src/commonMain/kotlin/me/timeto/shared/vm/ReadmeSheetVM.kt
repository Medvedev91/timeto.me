package me.timeto.shared.vm

import kotlinx.coroutines.flow.*

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

        data class Text(val text: String) : Paragraph()
    }
}

//

private typealias PText = ReadmeSheetVM.Paragraph.Text

private fun prepParagraphs(): List<ReadmeSheetVM.Paragraph> {
    val paragraphs = mutableListOf<ReadmeSheetVM.Paragraph>()
    paragraphs.add(PText("Hi!"))
    paragraphs.add(PText("Developer is here."))

    return paragraphs
}
