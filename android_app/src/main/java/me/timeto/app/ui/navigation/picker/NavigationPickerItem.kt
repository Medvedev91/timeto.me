package me.timeto.app.ui.navigation.picker

data class NavigationPickerItem<T>(
    val title: String,
    val isSelected: Boolean,
    val item: T,
)
