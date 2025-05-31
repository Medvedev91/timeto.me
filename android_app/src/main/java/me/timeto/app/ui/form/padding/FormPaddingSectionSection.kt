package me.timeto.app.ui.form.padding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import me.timeto.app.goldenRatioDown
import me.timeto.app.ui.form.form__itemMinHeight

private val paddingSectionSection: Dp =
    form__itemMinHeight.goldenRatioDown()

@Composable
fun FormPaddingSectionSection() {
    Box(Modifier.height(paddingSectionSection))
}
