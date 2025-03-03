package me.timeto.app.ui.form

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import me.timeto.app.goldenRatioDown

private val paddingSectionSection: Dp = Form__itemMinHeight.goldenRatioDown()

@Composable
fun FormPaddingSectionSection() {
    Box(Modifier.height(paddingSectionSection))
}
