package me.timeto.shared.ui.main

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.Cache
import me.timeto.shared.ColorRgba
import me.timeto.shared.UnixTime
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.delayToNextMinute
import me.timeto.shared.misc.BatteryInfo
import me.timeto.shared.vm.__Vm

class MainTabsVm : __Vm<MainTabsVm.State>() {

    data class State(
        val batteryLevel: Int?,
        val isBatteryCharging: Boolean,
        val lastIntervalId: Int,
        val forceUpdate: Int,
    ) {

        val timeText: String =
            UnixTime().getStringByComponents(UnixTime.StringComponent.hhmm24)

        val tasksText: String =
            "${Cache.tasksDb.count { it.isToday }}"

        val batteryUi: BatteryUi = run {
            val level: Int? = batteryLevel
            val text = "${level ?: "--"}"
            when {
                isBatteryCharging -> BatteryUi(
                    text = text,
                    colorRgba = if (level == 100) ColorRgba.green else ColorRgba.blue,
                    isHighlighted = true,
                )
                level in 0..20 -> BatteryUi(
                    text = text,
                    colorRgba = ColorRgba.red,
                    isHighlighted = true,
                )
                else -> BatteryUi(
                    text = text,
                    colorRgba = ColorRgba.homeFontSecondary,
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
            forceUpdate = 0,
        )
    )

    init {

        val scopeVm = scopeVm()

        combine(
            BatteryInfo.levelFlow,
            BatteryInfo.isChargingFlow,
            IntervalDb.selectLastOneOrNullFlow(),
        ) { level, isCharging, lastIntervalDb ->
            state.update {
                it.copy(
                    batteryLevel = level,
                    isBatteryCharging = isCharging,
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
        val colorRgba: ColorRgba,
        val isHighlighted: Boolean,
    )
}
