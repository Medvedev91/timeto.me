package me.timeto.shared.ui.main

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.Cache
import me.timeto.shared.ColorRgba
import me.timeto.shared.UnixTime
import me.timeto.shared.batteryLevelOrNull
import me.timeto.shared.delayToNextMinute
import me.timeto.shared.isBatteryChargingOrNull
import me.timeto.shared.vm.__Vm

class MainTabsVm : __Vm<MainTabsVm.State>() {

    data class State(
        val updateId: Int,
    ) {

        val timeText: String =
            UnixTime().getStringByComponents(UnixTime.StringComponent.hhmm24)

        val tasksText: String =
            "${Cache.tasksDb.count { it.isToday }}"

        val batteryUi: BatteryUi = run {
            val level: Int? = batteryLevelOrNull
            val text = "${level ?: "--"}"
            when {
                isBatteryChargingOrNull == true -> BatteryUi(
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
        State(updateId = 0)
    )

    init {
        scopeVm().launch {
            while (true) {
                delayToNextMinute()
                state.update {
                    it.copy(updateId = it.updateId + 1)
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
