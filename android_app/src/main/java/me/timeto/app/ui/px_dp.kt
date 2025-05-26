package me.timeto.app.ui

import android.content.res.Resources
import androidx.compose.ui.unit.dp

private val density: Float = Resources.getSystem().displayMetrics.density
fun dpToPx(dp: Float): Int = (dp * density).toInt()
fun pxToDp(px: Int): Float = (px / density)

val H_PADDING = 16.dp
val H_PADDING_HALF = H_PADDING / 2
val onePx = pxToDp(1).dp

val halfDpFloor = pxToDp(dpToPx(1f) / 2).dp // -=
val halfDpCeil = 1.dp - halfDpFloor // +=
