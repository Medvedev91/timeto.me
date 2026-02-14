package me.timeto.app

import androidx.compose.ui.unit.Dp
import me.timeto.shared.goldenRatio

fun Dp.limitMin(dp: Dp): Dp = if (this < dp) dp else this
fun Dp.limitMax(dp: Dp): Dp = if (this > dp) dp else this
fun Dp.goldenRatioUp(): Dp = this * goldenRatio
fun Dp.goldenRatioDown(): Dp = this / goldenRatio
