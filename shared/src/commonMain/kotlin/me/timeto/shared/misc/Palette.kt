package me.timeto.shared.misc

import me.timeto.shared.ColorRgba

/**
 * Based on https://developer.apple.com/design/human-interface-guidelines/color
 */
data class Palette(
    val name: String,
    val light: ColorRgba,
    val dark: ColorRgba,
    val aLight: ColorRgba,
    val aDark: ColorRgba,
) {

    companion object {

        val red = Palette("Red", ColorRgba(255, 59, 48), ColorRgba(255, 69, 58), ColorRgba(255, 105, 97), ColorRgba(215, 0, 21))
        val orange = Palette("Orange", ColorRgba(255, 149, 0), ColorRgba(255, 159, 10), ColorRgba(255, 179, 64), ColorRgba(201, 52, 0))
        val yellow = Palette("Yellow", ColorRgba(255, 204, 0), ColorRgba(255, 214, 10), ColorRgba(255, 212, 38), ColorRgba(178, 80, 0))
        val green = Palette("Green", ColorRgba(52, 199, 89), ColorRgba(48, 209, 88), ColorRgba(48, 219, 91), ColorRgba(36, 138, 61))
        val mint = Palette("Mint", ColorRgba(0, 199, 190), ColorRgba(99, 230, 226), ColorRgba(102, 212, 207), ColorRgba(12, 129, 123))
        val teal = Palette("Teal", ColorRgba(48, 176, 199), ColorRgba(64, 200, 224), ColorRgba(93, 230, 255), ColorRgba(0, 130, 153))
        val cyan = Palette("Cyan", ColorRgba(50, 173, 230), ColorRgba(100, 210, 255), ColorRgba(112, 215, 255), ColorRgba(0, 113, 164))
        val blue = Palette("Blue", ColorRgba(0, 122, 255), ColorRgba(10, 132, 255), ColorRgba(64, 156, 255), ColorRgba(0, 64, 221))
        val indigo = Palette("Indigo", ColorRgba(88, 86, 214), ColorRgba(94, 92, 230), ColorRgba(125, 122, 255), ColorRgba(54, 52, 163))
        val purple = Palette("Purple", ColorRgba(175, 82, 222), ColorRgba(191, 90, 242), ColorRgba(218, 143, 255), ColorRgba(137, 68, 171))
        val pink = Palette("Pink", ColorRgba(255, 45, 85), ColorRgba(255, 55, 95), ColorRgba(255, 100, 130), ColorRgba(211, 15, 69))
        val brown = Palette("Brown", ColorRgba(165, 132, 94), ColorRgba(172, 142, 104), ColorRgba(181, 148, 105), ColorRgba(127, 101, 69))

        val gray = Palette("Gray", ColorRgba(142, 142, 147), ColorRgba(142, 142, 147), ColorRgba(108, 108, 112), ColorRgba(174, 174, 178))
        val gray2 = Palette("Gray 2", ColorRgba(174, 174, 178), ColorRgba(99, 99, 102), ColorRgba(142, 142, 147), ColorRgba(124, 124, 128))
        val gray3 = Palette("Gray 3", ColorRgba(199, 199, 204), ColorRgba(72, 72, 74), ColorRgba(174, 174, 178), ColorRgba(84, 84, 86))
        val gray4 = Palette("Gray 4", ColorRgba(209, 209, 214), ColorRgba(58, 58, 60), ColorRgba(188, 188, 192), ColorRgba(68, 68, 70))
        val gray5 = Palette("Gray 5", ColorRgba(229, 229, 234), ColorRgba(44, 44, 46), ColorRgba(216, 216, 220), ColorRgba(54, 54, 56))
        val gray6 = Palette("Gray 6", ColorRgba(242, 242, 247), ColorRgba(28, 28, 30), ColorRgba(235, 235, 240), ColorRgba(36, 36, 38))
    }
}
