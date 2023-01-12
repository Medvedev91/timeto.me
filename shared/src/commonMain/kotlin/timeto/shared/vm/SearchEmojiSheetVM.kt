package timeto.shared.vm

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import timeto.shared.*

class SearchEmojiSheetVM : __VM<SearchEmojiSheetVM.State>() {

    data class Emoji(
        val emoji: String,
        val tags: String,
    )

    data class State(
        val inputValue: String,
        val selectedEmojis: List<Emoji>,
    ) {
        val inputPlaceholder = "Search Emoji"
    }

    override val state = MutableStateFlow(
        State(
            inputValue = "",
            selectedEmojis = listOf(),
        )
    )

    private var allEmojis = listOf<Emoji>()

    override fun onAppear() {
        scopeVM().launchEx {
            val jString = getResourceContent("emojis", "json")
            allEmojis = Json.parseToJsonElement(jString).jsonArray.map { jElement ->
                val strings = jElement.jsonArray.map { it.jsonPrimitive.content }
                Emoji(emoji = strings[0], tags = strings[1])
            }
            state.update { it.copy(selectedEmojis = allEmojis) }
        }
    }

    fun setInputValue(newValue: String) {
        val prepValue = newValue.lowercase().trim()
        if (prepValue.isEmpty())
            return setInputAndSelectedEmoji("", allEmojis)

        val words = prepValue
            .replace("\\s+".toRegex(), " ")
            .split(" ")

        val resEmojis = allEmojis
            .filter { emoji ->
                words.all { emoji.tags.contains(it) }
            }

        // WARNING newValue, not a prepValue, otherwise recursive call
        setInputAndSelectedEmoji(newValue, resEmojis)
    }

    private fun setInputAndSelectedEmoji(
        input: String,
        selectedEmojis: List<Emoji>,
    ) {
        state.update {
            it.copy(
                inputValue = input,
                selectedEmojis = selectedEmojis
            )
        }
    }
}
