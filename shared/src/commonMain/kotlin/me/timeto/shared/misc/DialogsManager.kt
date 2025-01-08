package me.timeto.shared.misc

interface DialogsManager {

    fun alert(message: String)

    fun confirmation(
        message: String,
        buttonText: String,
        onConfirm: () -> Unit,
    )
}
