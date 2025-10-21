package me.timeto.shared.vm.home.settings.buttons

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.timeto.shared.Cache
import me.timeto.shared.ColorRgba
import me.timeto.shared.HomeButtonSort
import me.timeto.shared.Palette
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.launchExIo
import me.timeto.shared.onEachExIn
import me.timeto.shared.vm.Vm
import me.timeto.shared.vm.home.buttons.homeButtonsCellsCount
import kotlin.math.absoluteValue

typealias ButtonUi = HomeSettingsButtonUi

private const val rowsCount: Int =
    HomeButtonSort.visibleRows + 5 // Visible + Hidden

class HomeSettingsButtonsVm(
    private val spacing: Float,
    private val rowHeight: Float,
    width: Float,
) : Vm<HomeSettingsButtonsVm.State>() {

    private val cellWidth: Float =
        (width - ((homeButtonsCellsCount - 1) * spacing)) / homeButtonsCellsCount

    data class State(
        val buttonsData: ButtonsData,
        val rowHeight: Float,
        val update: Int = 0,
    ) {

        val height: Float =
            rowHeight * buttonsData.rowsCount

        val newGoalText = "New Goal"
    }

    override val state: MutableStateFlow<State>

    init {
        val scopeVm = scopeVm()
        scopeVm.launch {
            Goal2Db.selectAll().forEach { goal2Db ->
                val sort: HomeButtonSort? = HomeButtonSort.parseOrNull(goal2Db.home_button_sort)
                if (sort == null || (sort.rowIdx > (rowsCount - 1))) {
                    val newSort = HomeButtonSort.findNextPosition(true, barSize = 2)
                    goal2Db.updateHomeButtonSort(newSort)
                }
            }
        }

        fun prepButtonsData(goals2Db: List<Goal2Db>): ButtonsData {
            val dataButtonsUiRaw: List<ButtonUi> = goals2Db.map { goalDb ->
                ButtonUi(
                    type = HomeSettingsButtonType.Goal(goalDb),
                    // todo should be impossible and must be refactored
                    sort = HomeButtonSort.parseOrNull(goalDb.home_button_sort) ?: HomeButtonSort(
                        rowIdx = 999,
                        cellIdx = 0,
                        size = 6,
                    ),
                    colorRgba = goalDb.colorRgba,
                    spacing = spacing,
                    cellWidth = cellWidth,
                    rowHeight = rowHeight,
                )
            }

            val buttonsData = buildButtonsData(
                dataButtonsUiRaw = dataButtonsUiRaw,
                spacing = spacing,
                cellWidth = cellWidth,
                rowHeight = rowHeight,
            )

            return buttonsData
        }

        state = MutableStateFlow(
            State(
                buttonsData = prepButtonsData(Cache.goals2Db),
                rowHeight = rowHeight,
            )
        )

        Goal2Db.selectAllFlow().onEachExIn(scopeVm) { goals2Db ->
            state.update {
                it.copy(
                    buttonsData = prepButtonsData(goals2Db),
                    update = it.update + 1,
                )
            }
        }
    }

    fun getHoverButtonsUiOnDrag(
        buttonUi: ButtonUi,
        x: Float,
        y: Float,
    ): List<ButtonUi> {
        val buttonsData: ButtonsData = state.value.buttonsData

        val nearestButtonUi: ButtonUi = buttonsData.emptyButtonsUi.minBy { emptyButtonUi ->
            (emptyButtonUi.offsetX - x).absoluteValue + (emptyButtonUi.offsetY - y).absoluteValue
        }

        if ((nearestButtonUi.sort.cellIdx + buttonUi.sort.size) > homeButtonsCellsCount)
            return emptyList()

        val usedCellIds: List<Int> = buttonsData.dataButtonsUi
            .filter { it.id != buttonUi.id }
            .filter { it.sort.rowIdx == nearestButtonUi.sort.rowIdx }
            .map { it.sort.cellIdx until (it.sort.cellIdx + it.sort.size) }
            .flatten()

        val hoverCellIds: IntRange =
            (nearestButtonUi.sort.cellIdx until (nearestButtonUi.sort.cellIdx + buttonUi.sort.size))

        if (usedCellIds.intersect(hoverCellIds).isNotEmpty())
            return emptyList()

        val hoverButtonsUi = buttonsData.emptyButtonsUi
            .filter { it.sort.rowIdx == nearestButtonUi.sort.rowIdx }
            .filter { it.sort.cellIdx in hoverCellIds }
            .map { it.copy(colorRgba = hoverButtonBgColorRgba) }

        return hoverButtonsUi
    }

    fun onButtonDragEnd(
        buttonUi: ButtonUi,
        x: Float,
        y: Float,
    ): Boolean {
        val hoverButtonsUi: List<ButtonUi> =
            getHoverButtonsUiOnDrag(buttonUi, x = x, y = y)
        if (hoverButtonsUi.isEmpty())
            return false

        val firstHoverButtonUi: ButtonUi =
            hoverButtonsUi.minBy { it.sort.cellIdx }

        val newSort: HomeButtonSort = buttonUi.sort.copy(
            rowIdx = firstHoverButtonUi.sort.rowIdx,
            cellIdx = firstHoverButtonUi.sort.cellIdx,
        )

        updateSort(buttonUi, newSort)

        return true
    }

    fun getHoverButtonsUiOnResize(
        buttonUi: ButtonUi,
        left: Float,
        right: Float,
    ): List<ButtonUi> {
        val rowIdx: Int = buttonUi.sort.rowIdx
        val buttonsData: ButtonsData = state.value.buttonsData

        val usedCellIds: List<Int> = buttonsData.dataButtonsUi
            .filter { it.id != buttonUi.id }
            .filter { it.sort.rowIdx == rowIdx }
            .map { it.sort.cellIdx until (it.sort.cellIdx + it.sort.size) }
            .flatten()

        val nearestLeftEmptyButtonUi: ButtonUi = buttonsData.emptyButtonsUi.minBy { emptyButtonUi ->
            (emptyButtonUi.offsetX - (buttonUi.offsetX - left)).absoluteValue
        }

        val nearestRightEmptyButtonUi: ButtonUi = buttonsData.emptyButtonsUi.minBy { emptyButtonUi ->
            (emptyButtonUi.offsetX - (buttonUi.offsetX + buttonUi.fullWidth - buttonUi.cellWidth + right)).absoluteValue
        }

        val hoverCellIds: IntRange =
            (nearestLeftEmptyButtonUi.sort.cellIdx..nearestRightEmptyButtonUi.sort.cellIdx)

        if (usedCellIds.intersect(hoverCellIds).isNotEmpty())
            return emptyList()

        val hoverButtonsUi = buttonsData.emptyButtonsUi
            .filter { it.sort.rowIdx == rowIdx }
            .filter { it.sort.cellIdx in hoverCellIds }
            .map { it.copy(colorRgba = hoverButtonBgColorRgba) }

        return hoverButtonsUi
    }

    fun onButtonResizeEnd(
        buttonUi: ButtonUi,
        left: Float,
        right: Float,
    ): Boolean {
        val hoverButtonsUi: List<ButtonUi> =
            getHoverButtonsUiOnResize(buttonUi, left = left, right = right)
        if (hoverButtonsUi.isEmpty())
            return false

        val firstHoverCellIdx: Int =
            hoverButtonsUi.minOf { it.sort.cellIdx }

        val lastHoverButtonUi: Int =
            hoverButtonsUi.maxOf { it.sort.cellIdx }

        val newSort: HomeButtonSort = buttonUi.sort.copy(
            cellIdx = firstHoverCellIdx,
            size = lastHoverButtonUi - firstHoverCellIdx + 1,
        )

        updateSort(buttonUi, newSort)

        return true
    }

    private fun updateSort(
        buttonUi: ButtonUi,
        newSort: HomeButtonSort,
    ) {
        val newButtonsUi: List<ButtonUi> =
            state.value.buttonsData.dataButtonsUi.filter { it.id != buttonUi.id } +
                    buttonUi.copy(sort = newSort)

        val buttonsData = buildButtonsData(
            dataButtonsUiRaw = newButtonsUi,
            spacing = spacing,
            cellWidth = cellWidth,
            rowHeight = rowHeight,
        )

        state.update {
            it.copy(
                buttonsData = buttonsData,
                update = it.update + 1,
            )
        }

        val type = buttonUi.type
        if (type is HomeSettingsButtonType.Goal) launchExIo {
            type.goalDb.updateHomeButtonSort(newSort)
        }
    }

    ///

    data class ButtonsData(
        val rowsCount: Int,
        val emptyButtonsUi: List<ButtonUi>,
        val dataButtonsUi: List<ButtonUi>,
        val headersUi: List<HeaderUi>,
    )

    data class HeaderUi(
        val title: String,
        val offsetY: Float,
    )
}

private val hoverButtonBgColorRgba: ColorRgba = Palette.gray2.dark

private fun buildEmptyButtonsUi(
    rowsCount: Int,
    spacing: Float,
    cellWidth: Float,
    rowHeight: Float,
): List<ButtonUi> =
    (0 until rowsCount)
        .map { rowIdx ->
            (0 until homeButtonsCellsCount)
                .map { cellIdx ->
                    ButtonUi(
                        type = HomeSettingsButtonType.Empty,
                        sort = HomeButtonSort(
                            rowIdx = rowIdx,
                            cellIdx = cellIdx,
                            size = 1,
                        ),
                        colorRgba = Palette.gray6.dark,
                        spacing = spacing,
                        cellWidth = cellWidth,
                        rowHeight = rowHeight,
                    )
                }
        }
        .flatten()


private fun buildButtonsData(
    dataButtonsUiRaw: List<ButtonUi>,
    spacing: Float,
    cellWidth: Float,
    rowHeight: Float,
) = HomeSettingsButtonsVm.ButtonsData(
    rowsCount = rowsCount,
    emptyButtonsUi = buildEmptyButtonsUi(
        rowsCount = rowsCount,
        spacing = spacing,
        cellWidth = cellWidth,
        rowHeight = rowHeight,
    ),
    dataButtonsUi = dataButtonsUiRaw,
    headersUi = listOf(
        HomeSettingsButtonsVm.HeaderUi(
            title = "Home Screen",
            offsetY = 0f,
        ),
        HomeSettingsButtonsVm.HeaderUi(
            title = "Hidden",
            offsetY = rowHeight * (HomeButtonSort.visibleRows + 1),
        ),
    ),
)
