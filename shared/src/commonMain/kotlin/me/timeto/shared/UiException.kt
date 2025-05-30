package me.timeto.shared

class UiException(
    val uiMessage: String,
) : Exception(uiMessage)
