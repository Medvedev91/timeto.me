package me.timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow

object BatteryInfo {

    val levelFlow = MutableStateFlow<Int?>(null)
    val isChargingFlow = MutableStateFlow<Boolean>(false)

    fun emitLevel(level: Int) {
        launchExIo {
            levelFlow.emit(level)
        }
    }

    fun emitIsCharging(isCharging: Boolean) {
        launchExIo {
            isChargingFlow.emit(isCharging)
        }
    }
}
