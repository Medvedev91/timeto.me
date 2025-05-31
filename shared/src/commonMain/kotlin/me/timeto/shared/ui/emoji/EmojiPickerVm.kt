package me.timeto.shared.ui.emoji

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import me.timeto.shared.getResourceContent
import me.timeto.shared.launchEx
import me.timeto.shared.ui.__Vm

class EmojiPickerVm : __Vm<EmojiPickerVm.State>() {

    data class State(
        val emojis: List<Emoji>,
    ) {
        val searchPlaceholder = "Search Emoji"
    }

    override val state = MutableStateFlow(
        State(
            emojis = emptyList(),
        )
    )

    private var allEmojis: List<Emoji> = emptyList()

    init {
        val scopeVm = scopeVm()
        scopeVm.launchEx {
            val jString: String = getResourceContent("emojis", "json")
            allEmojis = Json.parseToJsonElement(jString).jsonArray.map { jElement ->
                val strings: List<String> =
                    jElement.jsonArray.map { it.jsonPrimitive.content }
                Emoji(emoji = strings[0], tags = strings[1])
            }
            state.update { it.copy(emojis = allEmojis) }
        }
    }

    fun search(text: String) {
        val prepValue = text.lowercase().trim()
        if (prepValue.isEmpty()) {
            state.update { it.copy(emojis = allEmojis) }
            return
        }

        val words = prepValue
            .replace("\\s+".toRegex(), " ")
            .split(" ")

        val resEmojis = allEmojis
            .filter { emoji ->
                words.all { emoji.tags.contains(it) }
            }

        state.update { it.copy(emojis = resEmojis) }
    }

    ///

    data class Emoji(
        val emoji: String,
        val tags: String,
    )
}
