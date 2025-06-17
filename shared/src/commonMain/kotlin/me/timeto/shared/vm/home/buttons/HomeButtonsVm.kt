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
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.GoalDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.delayToNextMinute
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.Vm

class HomeButtonsVm(
    val width: Float,
    val rowHeight: Float,
    val spacing: Float,
) : Vm<HomeButtonsVm.State>() {

    data class State(
        val vm: HomeButtonsVm,
        val rawButtonsUi: List<HomeButtonUi>,
        val update: Int = 1,
    ) {

        val buttonsUi: List<HomeButtonUi> =
            rawButtonsUi.map { it.recalculateUi() }

        val height: Float =
            (buttonsUi.maxOfOrNull { it.sort.rowIdx }?.plus(1) ?: 0) * vm.rowHeight
    }

    override val state = MutableStateFlow(
        State(
            vm = this,
            rawButtonsUi = emptyList(),
        )
    )

    init {
        val scopeVm = scopeVm()

        combine(
            IntervalDb.anyChangeFlow(),
            GoalDb.anyChangeFlow(),
        ) { _, _ ->
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

        val goalButtons: List<HomeButtonNoSorted> = Cache.goalsDb.mapNotNull { goalDb ->
            if (!goalDb.buildPeriod().isToday())
                return@mapNotNull null

            val activityDb: ActivityDb = goalDb.getActivityDbCached()

            val goalBarsUi: List<DayBarsUi.BarUi> = allBarsUi.barsUi
                .filter { barUi ->
                    // We can attach goal for any interval,
                    // no matter what the activity is.
                    if (barUi.intervalTf.goalDb?.id == goalDb.id)
                        return@filter true
                    if (goalDb.isEntireActivity && (barUi.activityDb?.id == goalDb.activity_id))
                        return@filter true
                    false
                }

            val intervalsSeconds: Int = goalBarsUi.sumOf { it.seconds }

            val lastBarUiWithActivity: DayBarsUi.BarUi? =
                allBarsUi.barsUi.lastOrNull { it.activityDb != null }

            val activeTimeFrom: Int? =
                if ((lastBarUiWithActivity != null) && (lastBarUiWithActivity == goalBarsUi.lastOrNull()))
                    lastBarUiWithActivity.timeFinish
                else null

            val sort: HomeButtonSort =
                HomeButtonSort.parseOrDefault(goalDb.home_button_sort)

            val type = HomeButtonType.Goal(
                goalDb = goalDb,
                goalTf = goalDb.note.textFeatures(),
                bgColor = activityDb.colorRgba,
                intervalsSeconds = intervalsSeconds,
                activeTimeFrom = activeTimeFrom,
                sort = sort,
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
