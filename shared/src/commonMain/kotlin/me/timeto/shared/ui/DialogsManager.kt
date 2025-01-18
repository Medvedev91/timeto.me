package me.timeto.shared.ui

interface DialogsManager {

    fun alert(message: String)

    fun confirmation(
        message: String,
        buttonText: String,
        onConfirm: () -> Unit,
    )
}
