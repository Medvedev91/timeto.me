package me.timeto.app.ui

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import me.timeto.app.colorFromRgbaString

//
// https://sarunw.com/posts/dark-color-cheat-sheet/
// Remember that the transparency in the end.

// Sources
// MD https://material.io/resources/color/
// AG https://developer.apple.com/design/human-interface-guidelines/foundations/color

// todo check performance
val c: MyColors
    @Composable
    get() = if (MaterialTheme.colors.isLight) myLightColors else myDarkColors

@Composable
fun myDarkColors() = darkColors(primary = c.blue)
@Composable
fun myLightColors() = lightColors(primary = c.blue)

class MyColors(
    val blue: Color, // Not Color(0xFF0055FF)
    val orange: Color,
    val text: Color, // https://material.io/design/color/text-legibility.html ~ 87%
    val textSecondary: Color, // ~ 60%
    val background: Color, // TRICK Using R.color.my_dn_background overrides compos ;(
    val background2: Color,
    val backgroundEditable: Color,
    val tabsText: Color,
    val tabsBackground: Color,
    val dividerBg2: Color,
    val timerBarBorder: Color,
    val timerBarBackground: Color,
    val calendarIconBg: Color,
    val calendarIconColor: Color,
    val datePickerTitleBg: Color,
    val bgFormSheet: Color,
    val formButtonRightNoteText: Color,
) {
    val red = Color(0xFFFF453A)
    val green = colorFromRgbaString("52,199,89") // AG Green Light
    val purple = colorFromRgbaString("175,82,222") // AG Purple Light
    val white = Color.White
    val black = Color.Black
    val transparent = Color.Transparent
    val tasksTabDropFocused = green
}

private val tgLikeLightSheetBg = Color(0xFFEFEFF3)

private val myLightColors = MyColors(
    blue = Color(0xFF007AFF),
    orange = Color(0xFFFF9500), // AG Orange iOS Light
    text = Color(0xEE000000),
    textSecondary = Color(0xAA000000),
    background = Color(0xFFEEEEF3), // TRICK Sync with light R.color.my_dn_background
    background2 = Color.White,
    backgroundEditable = Color(0xFFf1f8e9), // Light Green 50
    tabsText = Color(0x99000000),
    tabsBackground = Color.White,
    dividerBg2 = Color(0x13000000),
    timerBarBorder = Color(0xFFDDDDDD),
    timerBarBackground = Color.White,
    calendarIconBg = Color.White,
    calendarIconColor = Color(0xAA000000),
    datePickerTitleBg = Color(0xFFEEEEF3),
    bgFormSheet = tgLikeLightSheetBg,
    formButtonRightNoteText = Color(0x88000000),
)

private val myDarkColors = MyColors(
    blue = Color(0xFF0A84FF),
    orange = Color(0xFFFF9D0A), // AG Orange iOS Dark
    text = Color(0xEEFFFFFF),
    textSecondary = Color(0xAAFFFFFF),
    background = Color(0xFF000000), // TRICK Sync with night R.color.my_dn_background
    background2 = Color(0xFF202022), // 0xFF1C1C1E
    backgroundEditable = Color(0xFF444444),
    tabsText = Color(0x77FFFFFF),
    tabsBackground = Color(0xFF191919), // 0xFF131313
    dividerBg2 = Color(0x19FFFFFF),
    timerBarBorder = Color(0xFF393939),
    timerBarBackground = Color(0xFF1C1C1E),
    calendarIconBg = Color(0x88202022),
    calendarIconColor = Color(0xFF777777),
    datePickerTitleBg = Color(0xFF2A2A2B),
    bgFormSheet = Color(0xFF121214),
    formButtonRightNoteText = Color(0x88FFFFFF),
)
