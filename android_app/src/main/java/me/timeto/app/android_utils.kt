package me.timeto.app

import androidx.compose.runtime.MutableState

fun MutableState<Boolean>.toggle() {
    value = !value
}
