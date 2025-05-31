package me.timeto.shared.vm.activities.timer

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.launchExIo
import me.timeto.shared.textFeatures
import me.timeto.shared.toHms
import me.timeto.shared.vm.Vm

class ActivityTimerVm(
    private val activityDb: ActivityDb,
    private val strategy: ActivityTimerStrategy,
) : Vm<ActivityTimerVm.State>() {

    companion object {

        suspend fun startInterval(
            seconds: Int,
            activityDb: ActivityDb,
            strategy: ActivityTimerStrategy,
        ) {
            when (strategy) {
                ActivityTimerStrategy.Simple ->
                    activityDb.startInterval(
                        seconds = seconds,
                    )
                is ActivityTimerStrategy.Task ->
                    strategy.taskDb.startInterval(
                        timer = seconds,
                        activityDb = activityDb,
                    )
                is ActivityTimerStrategy.Interval ->
                    IntervalDb.insertWithValidation(
                        timer = seconds,
                        activityDb = activityDb,
                        note = strategy.intervalDb.note,
                    )
            }
        }
    }

    ///

    data class State(
        val title: String,
        val note: String?,
        val initSeconds: Int,
        val timerItemsUi: List<TimerItemUi>,
    )

    override val state: MutableStateFlow<State>

    init {
        val initSeconds: Int = when (strategy) {
            ActivityTimerStrategy.Simple ->
                activityDb.timer
            is ActivityTimerStrategy.Task ->
                strategy.taskDb.text.textFeatures().timer ?: activityDb.timer
            is ActivityTimerStrategy.Interval ->
                strategy.intervalDb.timer
        }
        state = MutableStateFlow(
            State(
                title = activityDb.name.textFeatures().textNoFeatures,
                note = when (strategy) {
                    ActivityTimerStrategy.Simple ->
                        null
                    is ActivityTimerStrategy.Task ->
                        strategy.taskDb.text.textFeatures().textNoFeatures
                    is ActivityTimerStrategy.Interval ->
                        strategy.intervalDb.note?.textFeatures()?.textNoFeatures?.takeIf { it.isNotBlank() }
                },
                initSeconds = initSeconds,
                timerItemsUi = makeTimerItemsUi(initSeconds),
            )
        )
    }

    fun start(
        seconds: Int,
        onSuccess: () -> Unit,
    ) {
        launchExIo {
            startInterval(
                seconds = seconds,
                activityDb = activityDb,
                strategy = strategy,
            )
            onUi {
                onSuccess()
            }
        }
    }

    ///

    data class TimerItemUi(
        val seconds: Int,
    ) {
        val title: String = run {
            val (h, m, _) = seconds.toHms()
            when {
                h == 0 -> "$m min"
                m == 0 -> "$h h"
                else -> "$h : ${m.toString().padStart(2, '0')}"
            }
        }
    }
}

private fun makeTimerItemsUi(
    defSeconds: Int,
): List<ActivityTimerVm.TimerItemUi> {

    val secondsSet: Set<Int> = run {
        (1..10).map { it * 60 } + // 1 - 10 min by 1 min
        (1..10).map { (600 + (it * 300)) } + // 15 min - 1 hour by 5 min
        (1..138).map { (3_600 + (it * 600)) } + // 1 hour+ by 10 min
        defSeconds
    }.toSet()

    return secondsSet.sorted().map { seconds ->
        ActivityTimerVm.TimerItemUi(
            seconds = seconds,
        )
    }
}
