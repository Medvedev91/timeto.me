package me.timeto.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember

val LocalNavigationFs = compositionLocalOf<Navigation> {
    throw Exception("LocalNavigationFs")
}

@Composable
fun NavigationFs(
    content: @Composable () -> Unit,
) {
    val navigationFs = remember {
        Navigation()
    }
    CompositionLocalProvider(
        LocalNavigationFs provides navigationFs,
    ) {
        NavigationView(
            navigation = navigationFs,
        ) {
            content()
        }
    }
}
