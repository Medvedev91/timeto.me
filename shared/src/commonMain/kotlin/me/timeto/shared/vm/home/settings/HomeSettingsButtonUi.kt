package me.timeto.shared.vm.home.settings

import me.timeto.shared.ColorRgba
import me.timeto.shared.HomeButtonSort
import kotlin.math.absoluteValue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class HomeSettingsButtonUi(
    val sort: HomeButtonSort,
    val colorRgba: ColorRgba,
    val spacing: Float,
    val cellWidth: Float,
    val rowHeight: Float,
) {

    @OptIn(ExperimentalUuidApi::class)
    val id: String = Uuid.random().toString()

    val initX: Float =
        (sort.cellIdx.toFloat() * cellWidth) + (sort.cellIdx.toFloat() * spacing)
    val initY: Float =
        sort.rowIdx.toFloat() * rowHeight

    val fullWidth: Float =
        ((cellWidth * sort.size) + ((sort.size - 1).toFloat() * spacing)).absoluteValue

    val resizeLeftMinOffset: Float =
        -((cellWidth * (sort.size - 1)) + (spacing * (sort.size - 1)))

    val resizeLeftMaxOffset: Float =
        (cellWidth * sort.cellIdx) + (spacing * sort.cellIdx)

    val resizeRightMinOffset: Float =
        -((cellWidth * (sort.size - 1)) + (spacing * (sort.size - 1)))

    val resizeRightMaxOffset: Float = run {
        val cellsRight: Int =
            HomeSettingsVm.cellsCount - (sort.cellIdx + sort.size)
        (cellWidth * cellsRight) + (spacing * cellsRight)
    }
}
