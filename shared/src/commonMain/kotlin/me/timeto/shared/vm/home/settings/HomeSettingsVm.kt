package me.timeto.shared.vm.home.settings

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.ColorRgba
import me.timeto.shared.Palette
import me.timeto.shared.vm.Vm
import kotlin.math.absoluteValue

class HomeSettingsVm(
    spacing: Float,
    cellWidth: Float,
    rowHeight: Float,
) : Vm<HomeSettingsVm.State>() {

    companion object {
        const val cellsCount = 6
    }

    data class State(
        val rowsCount: Int,
        val emptyButtonsUi: List<HomeSettingsButtonUi>,
        val dataButtonsUi: List<HomeSettingsButtonUi>,
    )

    override val state: MutableStateFlow<State>

    init {
        val rowsCount = 5
        val dataButtonsUi: List<HomeSettingsButtonUi> = listOf(
            HomeSettingsButtonUi(rowIdx = 1, cellStartIdx = 0, cellsSize = 2, colorRgba = Palette.red.dark, spacing = spacing, cellWidth = cellWidth, rowHeight = rowHeight),
            HomeSettingsButtonUi(rowIdx = 1, cellStartIdx = 2, cellsSize = 3, colorRgba = Palette.blue.dark, spacing = spacing, cellWidth = cellWidth, rowHeight = rowHeight),
            HomeSettingsButtonUi(rowIdx = 3, cellStartIdx = 0, cellsSize = 2, colorRgba = Palette.purple.dark, spacing = spacing, cellWidth = cellWidth, rowHeight = rowHeight),
            HomeSettingsButtonUi(rowIdx = 3, cellStartIdx = 3, cellsSize = 3, colorRgba = Palette.cyan.dark, spacing = spacing, cellWidth = cellWidth, rowHeight = rowHeight),
        )
        state = MutableStateFlow(
            State(
                rowsCount = rowsCount,
                emptyButtonsUi = buildEmptyButtonsUi(
                    rowsCount = rowsCount,
                    spacing = spacing,
                    cellWidth = cellWidth,
                    rowHeight = rowHeight,
                ),
                dataButtonsUi = dataButtonsUi,
            )
        )
    }

    fun calcHoverButtonsUi(
        buttonUi: HomeSettingsButtonUi,
        x: Float,
        y: Float,
    ): List<HomeSettingsButtonUi> {
        val emptyButtonsUi = state.value.emptyButtonsUi

        val nearestButtonUi: HomeSettingsButtonUi = emptyButtonsUi.minBy { emptyButtonUi ->
            (emptyButtonUi.initX - x).absoluteValue + (emptyButtonUi.initY - y).absoluteValue
        }

        val usedCellIds: List<Int> = state.value.dataButtonsUi
            .filter { it.id != buttonUi.id }
            .filter { it.rowIdx == nearestButtonUi.rowIdx }
            .map { it.cellStartIdx until (it.cellStartIdx + it.cellsSize) }
            .flatten()

        val hoverCellIds: IntRange =
            (nearestButtonUi.cellStartIdx until (nearestButtonUi.cellStartIdx + buttonUi.cellsSize))

        if (usedCellIds.intersect(hoverCellIds).isNotEmpty())
            return listOf()

        val hoverButtonsUi = emptyButtonsUi
            .filter { it.rowIdx == nearestButtonUi.rowIdx }
            .filter { it.cellStartIdx in hoverCellIds }
            .map { it.copy(colorRgba = hoverButtonBgColorRgba) }

        return hoverButtonsUi
    }
}

private val hoverButtonBgColorRgba: ColorRgba = Palette.gray2.dark
private val emptyButtonBgColorRgba: ColorRgba = Palette.gray5.dark

private fun buildEmptyButtonsUi(
    rowsCount: Int,
    spacing: Float,
    cellWidth: Float,
    rowHeight: Float,
): List<HomeSettingsButtonUi> =
    (0 until rowsCount)
        .map { rowIdx ->
            (0 until HomeSettingsVm.cellsCount)
                .map { cellIdx ->
                    HomeSettingsButtonUi(
                        rowIdx = rowIdx,
                        cellStartIdx = cellIdx,
                        cellsSize = 1,
                        colorRgba = emptyButtonBgColorRgba,
                        spacing = spacing,
                        cellWidth = cellWidth,
                        rowHeight = rowHeight,
                    )
                }
        }
        .flatten()
