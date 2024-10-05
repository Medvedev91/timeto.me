package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.AppleColors
import me.timeto.shared.ColorRgba
import me.timeto.shared.Cache
import me.timeto.shared.textFeatures

class ActivityColorSheetVm(
    initData: InitData,
) : __Vm<ActivityColorSheetVm.State>() {

    class InitData(
        val title: String,
        val selectedColor: ColorRgba,
    )

    class ActivityUI(
        val text: String,
        val colorRgba: ColorRgba,
    )

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
        val headerTitle = "Activity Color"
        val doneTitle = "Done"

        val title = initData.title
        val otherActivitiesTitle = "OTHER ACTIVITIES"
        val otherActivitiesTitleColor = AppleColors.gray1Dark

        val rgbSlidersBtnColor = if (isRgbSlidersShowed) ColorRgba.white else AppleColors.gray2Dark

        val selectedColor = ColorRgba(r.toInt(), g.toInt(), b.toInt())

        val rgbText = "RGB: ${r.toInt()},${g.toInt()},${b.toInt()}" + " / " +
                      "#${r.toHex()}${g.toHex()}${b.toHex()}".uppercase()

        val colorGroups: List<List<ColorItem>> = AppleColors.Palettes.all
            .map { listOf(it.aLight, it.light, it.aDark) }
            .flatten()
            .map {
                ColorItem(
                    colorRgba = it,
                    isSelected = it.isEquals(r.toInt(), g.toInt(), b.toInt(), 255),
                )
            }
            .chunked(3)

        val allActivities: List<ActivityUI> = Cache.activitiesDbSorted.map {
            ActivityUI(
                text = "${it.emoji} ${it.name.textFeatures().textNoFeatures}",
                colorRgba = it.colorRgba,
            )
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
