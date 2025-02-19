package me.timeto.app.ui.header

data class HeaderActionButton(
    val text: String,
    val isEnabled: Boolean,
    val onClick: () -> Unit,
)
