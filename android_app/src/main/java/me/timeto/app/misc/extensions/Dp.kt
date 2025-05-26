package me.timeto.app.misc.extensions

import androidx.compose.ui.unit.Dp
import me.timeto.shared.GOLDEN_RATIO

fun Dp.limitMin(dp: Dp) = if (this < dp) dp else this
fun Dp.limitMax(dp: Dp) = if (this > dp) dp else this
fun Dp.goldenRatioUp() = this * GOLDEN_RATIO
fun Dp.goldenRatioDown() = this / GOLDEN_RATIO
