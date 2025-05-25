package me.timeto.app.ui.form.padding

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack

@Composable
fun FormPaddingBottom(
    withNavigation: Boolean,
) {
    VStack {
        FormPaddingSectionSection()
        if (withNavigation) {
            ZStack(Modifier.navigationBarsPadding())
        }
    }
}
