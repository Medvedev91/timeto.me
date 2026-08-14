package me.timeto.app

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import me.timeto.shared.timeMls

object Haptic {

    fun shot() {
        oneShot(40)
    }

    fun long() {
        oneShot(70)
    }

    ///

    private val vibrator: Vibrator by lazy { buildVibrator() }
    private var oneShotLastMillis: Long = 0

    private fun oneShot(duration: Long) {
        if ((timeMls() - oneShotLastMillis) < (duration * 1.5))
            return
        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        oneShotLastMillis = timeMls()
    }
}

///

private fun buildVibrator(): Vibrator {
    val vibratorManager: VibratorManager =
        App.instance.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    return vibratorManager.defaultVibrator
}
