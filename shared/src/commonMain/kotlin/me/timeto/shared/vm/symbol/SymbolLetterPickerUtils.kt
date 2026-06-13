package me.timeto.shared.vm.symbol

import me.timeto.shared.DialogsManager
import me.timeto.shared.Symbol

object SymbolLetterPickerUtils {

    fun validateLetter(
        letter: String,
        dialogsManager: DialogsManager,
        onSuccess: (Symbol.Letter) -> Unit,
    ) {
        val letter: String =
            letter.trim()
        if (letter.isBlank()) {
            dialogsManager.alert("Empty Symbol")
            return
        }
        onSuccess(Symbol.Letter(letter))
    }
}
