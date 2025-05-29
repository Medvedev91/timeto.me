package me.timeto.shared.ui.main

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.Cache
import me.timeto.shared.ColorRgba
import me.timeto.shared.UnixTime
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.delayToNextMinute
import me.timeto.shared.misc.BatteryInfo
import me.timeto.shared.misc.ColorEnum
import me.timeto.shared.vm.__Vm

class MainTabsVm : __Vm<MainTabsVm.State>() {

    companion object {
        val menuPrimaryColorDark = ColorRgba(255, 255, 255, 200)
        val menuSecondaryColorDark = ColorRgba(255, 255, 255, 128)
    }

    data class State(
        val batteryLevel: Int?,
        val isBatteryCharging: Boolean,
        val lastIntervalId: Int,
        val todayTasksCount: Int,
        val forceUpdate: Int,
    ) {

        val timeText: String =
            UnixTime().getStringByComponents(UnixTime.StringComponent.hhmm24)

        val tasksText: String = "$todayTasksCount"

        val batteryUi: BatteryUi = run {
            val level: Int? = batteryLevel
            val text = "${level ?: "--"}"
            when {
                isBatteryCharging -> BatteryUi(
                    text = text,
                    colorEnum = if (level == 100) ColorEnum.green else ColorEnum.blue,
                    isHighlighted = true,
                )
                level in 0..20 -> BatteryUi(
                    text = text,
                    colorEnum = ColorEnum.red,
                    isHighlighted = true,
                )
                else -> BatteryUi(
                    text = text,
                    colorEnum = null,
                    isHighlighted = false,
                )
            }
        }
    }

    override val state = MutableStateFlow(
        State(
            batteryLevel = BatteryInfo.levelFlow.value,
            isBatteryCharging = BatteryInfo.isChargingFlow.value,
            lastIntervalId = Cache.lastInterval.id,
            todayTasksCount = Cache.tasksDb.count { it.isToday },
            forceUpdate = 0,
        )
    )

    init {

        val scopeVm = scopeVm()

        combine(
            BatteryInfo.levelFlow,
            BatteryInfo.isChargingFlow,
            TaskDb.selectAscFlow(),
            IntervalDb.selectLastOneOrNullFlow(),
        ) { level, isCharging, tasksDb, lastIntervalDb ->
            state.update {
                it.copy(
                    batteryLevel = level,
                    isBatteryCharging = isCharging,
                    todayTasksCount = tasksDb.count { taskDb -> taskDb.isToday },
                    lastIntervalId = lastIntervalDb?.id ?: it.lastIntervalId,
                )
            }
        }.launchIn(scopeVm)

        scopeVm.launch {
            while (true) {
                delayToNextMinute()
                state.update {
                    it.copy(forceUpdate = it.forceUpdate + 1)
                }
            }
        }
    }

    ///

    data class BatteryUi(
        val text: String,
        val colorEnum: ColorEnum?,
        val isHighlighted: Boolean,
    )
}
