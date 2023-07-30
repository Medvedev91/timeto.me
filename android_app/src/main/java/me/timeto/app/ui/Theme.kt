package me.timeto.app.ui

import androidx.compose.ui.graphics.Color
import me.timeto.app.colorFromRgbaString
import me.timeto.app.toColor
import me.timeto.shared.AppleColors

//
// https://sarunw.com/posts/dark-color-cheat-sheet/
// Remember that the transparency in the end.

// Sources
// MD https://material.io/resources/color/
// AG https://developer.apple.com/design/human-interface-guidelines/color

object c {

    private val bgFormDarkMode = Color(0xFF121214) // todo remove?

    val blue = Color(0xFF0A84FF)
    val orange = Color(0xFFFF9D0A) // AG Orange iOS Dark
    val text = Color(0xEEFFFFFF)
    val textSecondary = Color(0xAAFFFFFF)
    val bg = Color(0xFF000000) // TRICK Sync with night R.color.my_dn_background
    val bgSheet = bgFormDarkMode

    // todo remove
    val background = Color(0xFF000000) // TRICK Sync with night R.color.my_dn_background

    // todo remove
    val background2 = Color(0xFF202022) // 0xFF1C1C1E
    val backgroundEditable = Color(0xFF444444)
    val tabsText = Color(0x77FFFFFF)
    val tabsBackground = Color(0xFF191919) // 0xFF131313
    val dividerBg = AppleColors.gray5Dark.toColor()

    // todo remove
    val dividerBg2 = AppleColors.gray4Dark.toColor()

    // todo remove?
    val formHeaderBackground = Color(0xFF191919)
    val timerTitleDefault = Color.White
    val calendarIconColor = Color(0xFF777777)
    val datePickerTitleBg = Color(0xFF2A2A2B)

    // todo rename to bgForm?
    val bgFormSheet = bgFormDarkMode
    val formButtonRightNoteText = Color(0x88FFFFFF)
    val gray1 = AppleColors.gray1Dark.toColor()
    val gray2 = AppleColors.gray2Dark.toColor()
    val gray3 = AppleColors.gray3Dark.toColor()
    val gray4 = AppleColors.gray4Dark.toColor()
    val gray5 = AppleColors.gray5Dark.toColor()

    val red = Color(0xFFFF453A)
    val green = colorFromRgbaString("52,199,89") // AG Green Light
    val purple = colorFromRgbaString("175,82,222") // AG Purple Light
    val white = Color.White
    val black = Color.Black
    val transparent = Color.Transparent
    val tasksTabDropFocused = green
    val iconButtonBg = gray2
    val formHeaderDivider = gray4
}
