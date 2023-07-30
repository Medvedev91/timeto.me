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

private val bgFormDarkMode = Color(0xFF121214) // todo remove?

val c = MyColors(
    blue = Color(0xFF0A84FF),
    orange = Color(0xFFFF9D0A), // AG Orange iOS Dark
    text = Color(0xEEFFFFFF),
    textSecondary = Color(0xAAFFFFFF),
    bg = Color(0xFF000000), // TRICK Sync with night R.color.my_dn_background
    bgSheet = bgFormDarkMode,
    background = Color(0xFF000000), // TRICK Sync with night R.color.my_dn_background
    background2 = Color(0xFF202022), // 0xFF1C1C1E
    backgroundEditable = Color(0xFF444444),
    tabsText = Color(0x77FFFFFF),
    tabsBackground = Color(0xFF191919), // 0xFF131313
    dividerBg = AppleColors.gray5Dark.toColor(),
    dividerBg2 = AppleColors.gray4Dark.toColor(),
    formHeaderBackground = Color(0xFF191919),
    timerTitleDefault = Color.White,
    calendarIconColor = Color(0xFF777777),
    datePickerTitleBg = Color(0xFF2A2A2B),
    bgFormSheet = bgFormDarkMode,
    formButtonRightNoteText = Color(0x88FFFFFF),
    gray1 = AppleColors.gray1Dark.toColor(),
    gray2 = AppleColors.gray2Dark.toColor(),
    gray3 = AppleColors.gray3Dark.toColor(),
    gray4 = AppleColors.gray4Dark.toColor(),
    gray5 = AppleColors.gray5Dark.toColor(),
)

class MyColors(
    val blue: Color, // Not Color(0xFF0055FF)
    val orange: Color,
    val text: Color, // https://material.io/design/color/text-legibility.html ~ 87%
    val textSecondary: Color, // ~ 60%
    val bg: Color, // TRICK Using R.color.my_dn_background overrides compose ;(
    val bgSheet: Color,
    // todo remove
    val background: Color, // TRICK Using R.color.my_dn_background overrides compose ;(
    // todo remove
    val background2: Color,
    val backgroundEditable: Color,
    val tabsText: Color,
    val tabsBackground: Color,
    val dividerBg: Color,
    // todo remove
    val dividerBg2: Color,
    // todo remove?
    val formHeaderBackground: Color,
    val timerTitleDefault: Color,
    val calendarIconColor: Color,
    val datePickerTitleBg: Color,
    // todo rename to bgForm?
    val bgFormSheet: Color,
    val formButtonRightNoteText: Color,
    val gray1: Color,
    val gray2: Color,
    val gray3: Color,
    val gray4: Color,
    val gray5: Color,
) {
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
