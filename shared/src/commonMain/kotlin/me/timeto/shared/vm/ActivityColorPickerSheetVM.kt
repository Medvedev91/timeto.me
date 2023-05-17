package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.ColorRgba
import me.timeto.shared.DI

private const val CIRCLES_IN_ROW = 6

class ActivityColorPickerSheetVM(
    initData: InitData,
) : __VM<ActivityColorPickerSheetVM.State>() {

    class InitData(
        val activityId: Int?,
        val selectedColor: ColorRgba,
    )

    sealed class MenuButton {

        class Activity(
            val id: Int,
            val emoji: String,
            val colorRgba: ColorRgba,
        ) : MenuButton()

        class RgbSlider : MenuButton()
    }

    class ColorItem(
        val colorRgba: ColorRgba,
        val isSelected: Boolean,
    )

    /**
     * TRICK Up isRgbSlidersAnimated on rgb change
     */
    data class State(
        val r: Float,
        val g: Float,
        val b: Float,
        val isRgbSlidersShowed: Boolean,
        val isRgbSlidersAnimated: Boolean,
        val initData: InitData,
    ) {
        val headerTitle = "Color"
        val doneTitle = "Done"

        val circlesInRow = CIRCLES_IN_ROW
        val selectedColor = ColorRgba(r.toInt(), g.toInt(), b.toInt())

        val rgbText =
            "#${r.toHex()}${g.toHex()}${b.toHex()}".uppercase() + " / " +
            "RGB: ${r.toInt()},${g.toInt()},${b.toInt()}"

        val colorGroups: List<List<ColorItem>> = mdColors
            .map {
                ColorItem(
                    colorRgba = it,
                    isSelected = it.isEquals(r.toInt(), g.toInt(), b.toInt(), 255),
                )
            }
            .chunked(CIRCLES_IN_ROW)

        val menuButtonGroups: List<List<MenuButton>> = run {
            val list = mutableListOf<MenuButton>()
            DI.activitiesSorted.forEach {
                list.add(
                    MenuButton.Activity(
                        id = it.id,
                        emoji = it.emoji,
                        colorRgba = if (it.id == initData.activityId)
                            selectedColor else it.getColorRgba()
                    )
                )
            }
            list.add(MenuButton.RgbSlider())
            return@run list.chunked(CIRCLES_IN_ROW)
        }

        private fun Float.toHex() = toInt().toString(16).padStart(2, '0')
    }

    override val state = MutableStateFlow(
        State(
            r = initData.selectedColor.r.toFloat(),
            g = initData.selectedColor.g.toFloat(),
            b = initData.selectedColor.b.toFloat(),
            isRgbSlidersShowed = false,
            isRgbSlidersAnimated = true,
            initData = initData,
        )
    )

    fun upR(r: Float): Unit = state.update { it.copy(r = r, isRgbSlidersAnimated = false) }
    fun upG(g: Float): Unit = state.update { it.copy(g = g, isRgbSlidersAnimated = false) }
    fun upB(b: Float): Unit = state.update { it.copy(b = b, isRgbSlidersAnimated = false) }

    fun upColorRgba(colorRgba: ColorRgba) {
        state.update {
            it.copy(
                r = colorRgba.r.toFloat(),
                g = colorRgba.g.toFloat(),
                b = colorRgba.b.toFloat(),
                isRgbSlidersAnimated = true,
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
    ColorRgba(255, 205, 210),
    ColorRgba(239, 154, 154),
    ColorRgba(229, 115, 115),
    ColorRgba(229, 57, 53),
    ColorRgba(198, 40, 40),
    ColorRgba(183, 28, 28),
    // Pink
    ColorRgba(248, 187, 208),
    ColorRgba(244, 143, 177),
    ColorRgba(240, 98, 146),
    ColorRgba(216, 27, 96),
    ColorRgba(173, 20, 87),
    ColorRgba(136, 14, 79),
    // Purple
    ColorRgba(225, 190, 231),
    ColorRgba(206, 147, 216),
    ColorRgba(186, 104, 200),
    ColorRgba(142, 36, 170),
    ColorRgba(106, 27, 154),
    ColorRgba(74, 20, 140),
    // Indigo
    ColorRgba(197, 202, 233),
    ColorRgba(159, 168, 218),
    ColorRgba(121, 134, 203),
    ColorRgba(57, 73, 171),
    ColorRgba(40, 53, 147),
    ColorRgba(26, 35, 126),
    // Blue
    ColorRgba(187, 222, 251),
    ColorRgba(144, 202, 249),
    ColorRgba(100, 181, 246),
    ColorRgba(30, 136, 229),
    ColorRgba(21, 101, 192),
    ColorRgba(13, 71, 161),
    // Cyan
    ColorRgba(178, 235, 242),
    ColorRgba(128, 222, 234),
    ColorRgba(77, 208, 225),
    ColorRgba(0, 172, 193),
    ColorRgba(0, 131, 143),
    ColorRgba(0, 96, 100),
    // Teal
    ColorRgba(178, 223, 219),
    ColorRgba(128, 203, 196),
    ColorRgba(77, 182, 172),
    ColorRgba(0, 137, 123),
    ColorRgba(0, 105, 92),
    ColorRgba(0, 77, 64),
    // Green
    ColorRgba(200, 230, 201),
    ColorRgba(165, 214, 167),
    ColorRgba(129, 199, 132),
    ColorRgba(67, 160, 71),
    ColorRgba(46, 125, 50),
    ColorRgba(27, 94, 32),
    // Lime
    ColorRgba(240, 244, 195),
    ColorRgba(230, 238, 156),
    ColorRgba(220, 231, 117),
    ColorRgba(192, 202, 51),
    ColorRgba(158, 157, 36),
    ColorRgba(130, 119, 23),
    // Yellow
    ColorRgba(255, 249, 196),
    ColorRgba(255, 245, 157),
    ColorRgba(255, 241, 118),
    ColorRgba(253, 216, 53),
    ColorRgba(249, 168, 37),
    ColorRgba(245, 127, 23),
    // Orange
    ColorRgba(255, 224, 178),
    ColorRgba(255, 204, 128),
    ColorRgba(255, 183, 77),
    ColorRgba(251, 140, 0),
    ColorRgba(239, 108, 0),
    ColorRgba(230, 81, 0),
    // Brown
    ColorRgba(215, 204, 200),
    ColorRgba(188, 170, 164),
    ColorRgba(161, 136, 127),
    ColorRgba(109, 76, 65),
    ColorRgba(78, 52, 46),
    ColorRgba(62, 39, 35),
    // Gray
    ColorRgba(245, 245, 245),
    ColorRgba(238, 238, 238),
    ColorRgba(224, 224, 224),
    ColorRgba(117, 117, 117),
    ColorRgba(66, 66, 66),
    ColorRgba(33, 33, 33),
    // Blue Gray
    ColorRgba(207, 216, 220),
    ColorRgba(176, 190, 197),
    ColorRgba(144, 164, 174),
    ColorRgba(84, 110, 122),
    ColorRgba(55, 71, 79),
    ColorRgba(38, 50, 56),
)
