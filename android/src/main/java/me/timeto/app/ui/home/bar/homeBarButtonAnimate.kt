package me.timeto.app.ui.home.bar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D

suspend fun homeBarButtonAnimate(
    animation: Animatable<Float, AnimationVector1D>,
) {
    animation.animateTo(1.40f)
    animation.animateTo(1f)
    animation.animateTo(1.25f)
    animation.animateTo(1f)
}
