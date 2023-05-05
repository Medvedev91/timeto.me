package me.timeto.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun RowScope.SpacerW1() {
    Box(Modifier.weight(1f))
}

@Composable
fun ColumnScope.SpacerW1() {
    Box(Modifier.weight(1f))
}
