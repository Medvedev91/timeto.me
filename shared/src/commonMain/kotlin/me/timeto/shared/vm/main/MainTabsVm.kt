package me.timeto.shared.vm.main

import kotlinx.coroutines.flow.*
import me.timeto.shared.Cache
import me.timeto.shared.ColorRgba
import me.timeto.shared.UnixTime
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.BatteryInfo
import me.timeto.shared.ColorEnum
import me.timeto.shared.TimeFlows
import me.timeto.shared.vm.Vm

class MainTabsVm : Vm<MainTabsVm.State>() {

    companion object {
        val menuPrimaryColorDark = ColorRgba(255, 255, 255, 200)
        val menuSecondaryColorDark = ColorRgba(255, 255, 255, 128)
    }

    data class State(
        val batteryLevel: Int?,
        val isBatteryCharging: Boolean,
        val lastIntervalId: Int,
        val forceUpdate: Int,
    ) {

        val timeText: String =
            UnixTime().getStringByComponents(UnixTime.StringComponent.hhmm24)

        val dateText: String = UnixTime().getStringByComponents(
            UnixTime.StringComponent.dayOfWeek3,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.dayOfMonth,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.month3,
        )

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
            lastIntervalId = Cache.lastIntervalDb.id,
            forceUpdate = 0,
        )
    )

    init {
        val scopeVm = scopeVm()
        combine(
            BatteryInfo.levelFlow,
            BatteryInfo.isChargingFlow,
            IntervalDb.selectLastOneOrNullFlow(),
            TimeFlows.eachMinuteSecondsFlow,
        ) { level, isCharging, lastIntervalDb, lastMinuteSeconds ->
            state.update {
                it.copy(
                    batteryLevel = level,
                    isBatteryCharging = isCharging,
                    lastIntervalId = lastIntervalDb?.id ?: it.lastIntervalId,
                    forceUpdate = lastMinuteSeconds,
                )
            }
        }.launchIn(scopeVm)
    }

    ///

    data class BatteryUi(
        val text: String,
        val colorEnum: ColorEnum?,
        val isHighlighted: Boolean,
    )
}
