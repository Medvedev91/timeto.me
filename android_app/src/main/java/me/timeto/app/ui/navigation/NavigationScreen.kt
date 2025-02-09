package me.timeto.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember

val LocalNavigationScreen = compositionLocalOf<Navigation> {
    throw Exception("LocalNavigationScreen")
}

@Composable
fun NavigationScreen(
    content: @Composable () -> Unit,
) {
    val navigationScreen = remember {
        Navigation()
    }
    CompositionLocalProvider(
        LocalNavigationScreen provides navigationScreen,
    ) {
        NavigationView(
            navigation = navigationScreen,
        ) {
            content()
        }
    }
}
