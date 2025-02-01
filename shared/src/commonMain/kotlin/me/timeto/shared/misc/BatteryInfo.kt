package me.timeto.shared.misc

import kotlinx.coroutines.flow.MutableStateFlow
import me.timeto.shared.launchExIo

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
