package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.ColorRgba
import me.timeto.shared.DI

private const val CIRCLES_IN_ROW = 6

class ColorPickerSheetVM(
    selectedColor: ColorRgba,
    text: String,
) : __VM<ColorPickerSheetVM.State>() {

    class ActivityUI(
        val emoji: String,
        val colorRgba: ColorRgba,
    )

    data class State(
        val r: Float,
        val g: Float,
        val b: Float,
        val text: String,
        val isRgbSlidersShowed: Boolean,
        val activityUIGroups: List<List<ActivityUI>>,
    ) {
        val headerTitle = "Color"
        val doneTitle = "Save"

        val circlesInRow = CIRCLES_IN_ROW

        val rgbText = "RGB: ${r.toInt()}, ${g.toInt()}, ${b.toInt()}"
        val colorGroups = mdColors.chunked(CIRCLES_IN_ROW)

        val textColor = if (listOf(r, g, b).average() < 180) ColorRgba.white else ColorRgba(80, 80, 80)

        fun getSelectedColor() = ColorRgba(r.toInt(), g.toInt(), b.toInt())
    }

    override val state = MutableStateFlow(
        State(
            r = selectedColor.r.toFloat(),
            g = selectedColor.g.toFloat(),
            b = selectedColor.b.toFloat(),
            text = text,
            isRgbSlidersShowed = false,
            activityUIGroups = DI.activitiesSorted
                .map { ActivityUI(it.emoji, it.getColorRgba()) }
                .chunked(CIRCLES_IN_ROW),
        )
    )

    fun upR(r: Float): Unit = state.update { it.copy(r = r) }
    fun upG(g: Float): Unit = state.update { it.copy(g = g) }
    fun upB(b: Float): Unit = state.update { it.copy(b = b) }

    fun upColorRgba(colorRgba: ColorRgba) {
        state.update {
            it.copy(
                r = colorRgba.r.toFloat(),
                g = colorRgba.g.toFloat(),
                b = colorRgba.b.toFloat(),
            )
        }
    }

    fun toggleIsRgbSlidersShowed() {
        state.update { it.copy(isRgbSlidersShowed = !it.isRgbSlidersShowed) }
    }
}

/**
 * https://m2.material.io/design/color/the-color-system.html
 */
private val mdColors = listOf(
    // Red
    ColorRgba(255, 235, 238),
    ColorRgba(255, 205, 210),
    ColorRgba(229, 115, 115),
    ColorRgba(229, 57, 53),
    ColorRgba(198, 40, 40),
    ColorRgba(183, 28, 28),
    // Pink
    ColorRgba(252, 228, 236),
    ColorRgba(248, 187, 208),
    ColorRgba(240, 98, 146),
    ColorRgba(216, 27, 96),
    ColorRgba(173, 20, 87),
    ColorRgba(136, 14, 79),
    // Purple
    ColorRgba(243, 229, 245),
    ColorRgba(225, 190, 231),
    ColorRgba(186, 104, 200),
    ColorRgba(142, 36, 170),
    ColorRgba(106, 27, 154),
    ColorRgba(74, 20, 140),
    // Deep Purple
    // ColorRgba(237, 231, 246),
    // ColorRgba(209, 196, 233),
    // ColorRgba(149, 117, 205),
    // ColorRgba(94, 53, 177),
    // ColorRgba(69, 39, 160),
    // ColorRgba(49, 27, 146),
    // Indigo
    ColorRgba(232, 234, 246),
    ColorRgba(197, 202, 233),
    ColorRgba(121, 134, 203),
    ColorRgba(57, 73, 171),
    ColorRgba(40, 53, 147),
    ColorRgba(26, 35, 126),
    // Blue
    ColorRgba(227, 242, 253),
    ColorRgba(187, 222, 251),
    ColorRgba(100, 181, 246),
    ColorRgba(30, 136, 229),
    ColorRgba(21, 101, 192),
    ColorRgba(13, 71, 161),
    // Light Blue
    // ColorRgba(225, 245, 254),
    // ColorRgba(179, 229, 252),
    // ColorRgba(79, 195, 247),
    // ColorRgba(3, 155, 229),
    // ColorRgba(2, 119, 189),
    // ColorRgba(1, 87, 155),
    // Cyan
    ColorRgba(224, 247, 250),
    ColorRgba(178, 235, 242),
    ColorRgba(77, 208, 225),
    ColorRgba(0, 172, 193),
    ColorRgba(0, 131, 143),
    ColorRgba(0, 96, 100),
    // Teal
    ColorRgba(224, 242, 241),
    ColorRgba(178, 223, 219),
    ColorRgba(77, 182, 172),
    ColorRgba(0, 137, 123),
    ColorRgba(0, 105, 92),
    ColorRgba(0, 77, 64),
    // Green
    ColorRgba(232, 245, 233),
    ColorRgba(200, 230, 201),
    ColorRgba(129, 199, 132),
    ColorRgba(67, 160, 71),
    ColorRgba(46, 125, 50),
    ColorRgba(27, 94, 32),
    // Light Green
    // ColorRgba(241, 248, 233),
    // ColorRgba(220, 237, 200),
    // ColorRgba(174, 213, 129),
    // ColorRgba(124, 179, 66),
    // ColorRgba(85, 139, 47),
    // ColorRgba(51, 105, 30),
    // Lime
    ColorRgba(249, 251, 231),
    ColorRgba(240, 244, 195),
    ColorRgba(220, 231, 117),
    ColorRgba(192, 202, 51),
    ColorRgba(158, 157, 36),
    ColorRgba(130, 119, 23),
    // Yellow
    ColorRgba(255, 253, 231),
    ColorRgba(255, 249, 196),
    ColorRgba(255, 241, 118),
    ColorRgba(253, 216, 53),
    ColorRgba(249, 168, 37),
    ColorRgba(245, 127, 23),
    // Amber
    // ColorRgba(255, 248, 225),
    // ColorRgba(255, 236, 179),
    // ColorRgba(255, 213, 79),
    // ColorRgba(255, 179, 0),
    // ColorRgba(255, 143, 0),
    // ColorRgba(255, 111, 0),
    // Orange
    ColorRgba(255, 243, 224),
    ColorRgba(255, 224, 178),
    ColorRgba(255, 183, 77),
    ColorRgba(251, 140, 0),
    ColorRgba(239, 108, 0),
    ColorRgba(230, 81, 0),
    // Deep Orange
    // ColorRgba(251, 233, 231),
    // ColorRgba(255, 204, 188),
    // ColorRgba(255, 138, 101),
    // ColorRgba(244, 81, 30),
    // ColorRgba(216, 67, 21),
    // ColorRgba(191, 54, 12),
    // Brown
    ColorRgba(239, 235, 233),
    ColorRgba(215, 204, 200),
    ColorRgba(161, 136, 127),
    ColorRgba(109, 76, 65),
    ColorRgba(78, 52, 46),
    ColorRgba(62, 39, 35),
    // Gray
    ColorRgba(250, 250, 250),
    ColorRgba(245, 245, 245),
    ColorRgba(224, 224, 224),
    ColorRgba(117, 117, 117),
    ColorRgba(66, 66, 66),
    ColorRgba(33, 33, 33),
    // Blue Gray
    ColorRgba(236, 239, 241),
    ColorRgba(207, 216, 220),
    ColorRgba(144, 164, 174),
    ColorRgba(84, 110, 122),
    ColorRgba(55, 71, 79),
    ColorRgba(38, 50, 56),
)
