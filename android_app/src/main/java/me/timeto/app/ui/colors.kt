package me.timeto.app.ui

import androidx.compose.ui.graphics.Color
import me.timeto.app.misc.extensions.toColor
import me.timeto.shared.Palette
import me.timeto.shared.vm.main.MainTabsVm

/**
 * https://sarunw.com/posts/dark-color-cheat-sheet
 */
object c {

    val white = Color.White
    val black = Color.Black
    val transparent = Color.Transparent

    val red = Palette.red.dark.toColor()
    val green = Palette.green.dark.toColor()
    val blue = Palette.blue.dark.toColor()
    val orange = Palette.orange.dark.toColor()
    val yellow = Palette.yellow.dark.toColor()
    val mint = Palette.mint.dark.toColor()
    val teal = Palette.teal.dark.toColor()
    val cyan = Palette.cyan.dark.toColor()
    val indigo = Palette.indigo.dark.toColor()
    val pink = Palette.pink.dark.toColor()
    val brown = Palette.brown.dark.toColor()
    val purple = Palette.purple.dark.toColor()

    val gray = Palette.gray.dark.toColor()
    val gray2 = Palette.gray2.dark.toColor()
    val gray3 = Palette.gray3.dark.toColor()
    val gray4 = Palette.gray4.dark.toColor()
    val gray5 = Palette.gray5.dark.toColor()
    val gray6 = Palette.gray6.dark.toColor()

    val text = white
    val secondaryText = Color(0xEB, 0xEB, 0xF5, 0x99) // secondaryLabel
    val tertiaryText = Color(0xEB, 0xEB, 0xF5, 0x4D) // tertiaryLabel

    val bg = black
    val fg = Color(0x1C, 0x1C, 0x1E) // secondarySystemBackground

    val divider = Color(0x54, 0x54, 0x58, 0x99) // separator

    //
    // App Colors

    val homeFg = gray5
    val homeTimerControls = Color(255, 255, 255, 180)

    val mainTabsMenuPrimary = MainTabsVm.menuPrimaryColorDark.toColor()
    val mainTabsMenuSecondary = MainTabsVm.menuSecondaryColorDark.toColor()

    val tasksDropFocused = Palette.green.dark.toColor()
}
