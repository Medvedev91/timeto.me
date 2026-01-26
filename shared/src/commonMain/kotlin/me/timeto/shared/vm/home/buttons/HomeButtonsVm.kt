package me.timeto.shared.vm.home.buttons

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.timeto.shared.Cache
import me.timeto.shared.DayBarsUi
import me.timeto.shared.HomeButtonSort
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.KvDb
import me.timeto.shared.delayToNextMinute
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.Vm

class HomeButtonsVm(
    val width: Float,
    val rowHeight: Float,
    val spacing: Float,
) : Vm<HomeButtonsVm.State>() {

    data class State(
        val update: Int = 1,
        private val rowHeight: Float,
        private val rawButtonsUi: List<HomeButtonUi>,
    ) {

        val buttonsUi: List<HomeButtonUi> =
            rawButtonsUi.map { it.recalculateUi() }

        val height: Float =
            (buttonsUi.maxOfOrNull { it.sort.rowIdx }?.plus(1) ?: 0) * rowHeight
    }

    override val state = MutableStateFlow(
        State(
            rowHeight = rowHeight,
            rawButtonsUi = emptyList(),
        )
    )

    init {
        val scopeVm = scopeVm()

        combine(
            IntervalDb.anyChangeFlow(),
            Goal2Db.anyChangeFlow(),
            KvDb.anyChangeFlow(),
        ) { _, _, _ ->
            fullUpdate()
        }.launchIn(scopeVm)

        scopeVm.launch {
            while (true) {
                delayToNextMinute(extraMls = 10)
                fullUpdate()
            }
        }

        scopeVm.launch {
            while (true) {
                delay(1_000)
                state.update { it.copy(update = it.update + 1) }
            }
        }
    }

    private suspend fun fullUpdate() {
        state.update {
            it.copy(rawButtonsUi = buildButtonsUi())
        }
    }

    private suspend fun buildButtonsUi(): List<HomeButtonUi> {
        val allBarsUi: DayBarsUi = DayBarsUi.buildToday()

        val goalButtons: List<HomeButtonNoSorted> = Cache.goals2Db.mapNotNull { goalDb ->
            if (!goalDb.buildPeriod().isToday())
                return@mapNotNull null

            val barsGoalStats: DayBarsUi.GoalStats =
                allBarsUi.buildGoalStats(goalDb)
            val sort: HomeButtonSort =
                HomeButtonSort.parseOrNull(goalDb.home_button_sort) ?: return@mapNotNull null
            if (sort.rowIdx >= HomeButtonSort.visibleRows)
                return@mapNotNull null

            val type = HomeButtonType.Goal(
                goalDb = goalDb,
                goalTf = goalDb.name.textFeatures(),
                bgColor = goalDb.colorRgba,
                barsGoalStats = barsGoalStats,
                sort = sort,
                timerHintUi = goalDb.buildTimerHints().map {
                    HomeButtonType.Goal.TimerHintUi(goalDb = goalDb, timer = it)
                },
                childGoalsUi = Cache.goals2Db
                    .filter { it.parent_id == goalDb.id }
                    .map { HomeButtonType.Goal.ChildGoalUi(it) },
            )

            HomeButtonNoSorted(
                type = type,
                sort = sort,
                fullWidth = width,
                rowHeight = rowHeight,
                spacing = spacing,
            )
        }

        return goalButtons.homeButtonsUiSorted()
    }
}

private data class HomeButtonNoSorted(
    val type: HomeButtonType,
    val sort: HomeButtonSort,
    val fullWidth: Float,
    val rowHeight: Float,
    val spacing: Float,
)

private fun List<HomeButtonNoSorted>.homeButtonsUiSorted(): List<HomeButtonUi> {
    val buttonsNoSortedWithInvalidPosition = mutableListOf<HomeButtonNoSorted>()
    return this
        .groupBy { it.sort.rowIdx }
        .toList()
        .sortedBy { it.first }
        .map { it.second }
        .map { buttonsNotSortedRow ->
            val usedCellIds: MutableSet<Int> = mutableSetOf()
            buttonsNotSortedRow.mapNotNull { buttonNoSorted ->
                val cellIds: IntRange =
                    buttonNoSorted.sort.cellIdx until (buttonNoSorted.sort.cellIdx + buttonNoSorted.sort.size)
                if (usedCellIds.intersect(cellIds).isNotEmpty()) {
                    buttonsNoSortedWithInvalidPosition.add(buttonNoSorted)
                    return@mapNotNull null
                }
                usedCellIds.addAll(cellIds)
                buttonNoSorted
            }
        }
        .plus(
            buttonsNoSortedWithInvalidPosition.map { buttonNoSorted ->
                listOf(
                    buttonNoSorted.copy(
                        sort = buttonNoSorted.sort.copy(
                            cellIdx = 0,
                            size = homeButtonsCellsCount,
                        ),
                    )
                )
            }
        )
        .mapIndexed { rowIdx, buttonsNoSorted ->
            buttonsNoSorted.map { buttonNoSorted ->
                HomeButtonUi.build(
                    type = buttonNoSorted.type,
                    sort = buttonNoSorted.sort.copy(rowIdx = rowIdx),
                    fullWidth = buttonNoSorted.fullWidth,
                    rowHeight = buttonNoSorted.rowHeight,
                    spacing = buttonNoSorted.spacing,
                )
            }
        }
        .flatten()
}
