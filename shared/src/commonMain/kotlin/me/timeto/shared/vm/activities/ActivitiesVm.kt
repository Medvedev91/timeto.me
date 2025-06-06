package me.timeto.shared.vm.activities

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.launchExIo
import me.timeto.shared.onEachExIn
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.vm.activities.timer.ActivityTimerStrategy
import me.timeto.shared.vm.activities.timer.ActivityTimerVm
import me.timeto.shared.vm.Vm

class ActivitiesVm(
    private val timerStrategy: ActivityTimerStrategy,
) : Vm<ActivitiesVm.State>() {

    data class State(
        val activitiesUi: List<ActivityUi>
    )

    override val state = MutableStateFlow(
        State(
            activitiesUi =
                Cache.activitiesDbSorted.map { activityDb ->
                    ActivityUi(
                        activityDb = activityDb,
                        timerStrategy = timerStrategy,
                    )
                },
        )
    )

    init {
        val scopeVm = scopeVm()
        ActivityDb.selectSortedFlow().onEachExIn(scopeVm) { activitiesDb ->
            state.update { state ->
                state.copy(activitiesUi = activitiesDb.map { activityDb ->
                    ActivityUi(
                        activityDb = activityDb,
                        timerStrategy = timerStrategy,
                    )
                })
            }
        }
    }

    fun updateTimerHints(
        activityDb: ActivityDb,
        newTimerHints: Set<Int>,
    ) {
        launchExIo {
            activityDb.updateTimerHints(newTimerHints)
        }
    }

    ///

    class ActivityUi(
        val activityDb: ActivityDb,
        val timerStrategy: ActivityTimerStrategy,
    ) {

        val text: String =
            activityDb.name.textFeatures().textUi()

        val isActive: Boolean =
            Cache.lastIntervalDb.activity_id == activityDb.id

        val timerHintsUi: List<TimerHintUi> =
            activityDb.timerHints.sorted().map { seconds ->
                TimerHintUi(
                    seconds = seconds,
                    onTap = {
                        launchExIo {
                            ActivityTimerVm.startInterval(
                                seconds = seconds,
                                activityDb = activityDb,
                                strategy = timerStrategy,
                            )
                        }
                    },
                )
            }
    }

    class TimerHintUi(
        val seconds: Int,
        val onTap: () -> Unit,
    ) {
        val title: String =
            seconds.toTimerHintNote(isShort = true)
    }
}
