package me.timeto.shared.vm.home.buttons

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.timeto.shared.Cache
import me.timeto.shared.DayBarsUi
import me.timeto.shared.HomeButtonSort
import me.timeto.shared.TimeFlows
import me.timeto.shared.combine
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.Vm
import me.timeto.shared.vm.task_form.TaskFormStrategy

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
            ChecklistItemDb.anyChangeFlow(),
            ActivityDb.anyChangeFlow(),
            KvDb.anyChangeFlow(),
            TaskFolderDb.anyChangeFlow(),
            TimeFlows.eachMinuteSecondsFlow,
        ) { _, _, _, _, _, _ ->
            fullUpdate()
            // Видимо из-за использованния кешированных данных при
            // обновлении не все данные успевают обновиться в кеше.
            // Делаем дополнительное обновление после обновления кеша.
            delay(200)
            fullUpdate()
        }.launchIn(scopeVm)

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
        val allTaskFolders: List<TaskFolderDb> = TaskFolderDb.selectAllSorted()

        val activityButtons: List<HomeButtonNoSorted> = Cache.activitiesDb.mapNotNull { activityDb ->
            if (!activityDb.buildPeriod().isToday())
                return@mapNotNull null

            val barsActivityStats: DayBarsUi.ActivityStats =
                allBarsUi.buildActivityStats(activityDb)
            val sort: HomeButtonSort =
                HomeButtonSort.parseOrNull(activityDb.home_button_sort) ?: return@mapNotNull null
            if (sort.rowIdx >= HomeButtonSort.visibleRows)
                return@mapNotNull null

            val type = HomeButtonType.Activity(
                activityDb = activityDb,
                activityTf = activityDb.name.textFeatures(),
                bgColor = activityDb.colorRgba,
                barsActivityStats = barsActivityStats,
                sort = sort,
                timerHintUi = activityDb.buildTimerHints().map {
                    HomeButtonType.Activity.TimerHintUi(activityDb = activityDb, timer = it)
                },
                childActivitiesUi = Cache.activitiesDb
                    .filter { it.parent_id == activityDb.id }
                    .map { HomeButtonType.Activity.ChildActivityUi(it) },
                newTaskFormStrategy = run {
                    val taskFolderDb: TaskFolderDb =
                        allTaskFolders.firstOrNull { it.activity_id == activityDb.id } ?: Cache.getTodayFolderDb()
                    TaskFormStrategy.NewTask(taskFolderDb)
                },
            )

            HomeButtonNoSorted(
                type = type,
                sort = sort,
                fullWidth = width,
                rowHeight = rowHeight,
                spacing = spacing,
            )
        }

        return activityButtons.homeButtonsUiSorted()
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
