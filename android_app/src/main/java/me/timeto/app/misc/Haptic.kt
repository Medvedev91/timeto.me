package me.timeto.app.misc

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import me.timeto.app.App
import me.timeto.shared.misc.timeMls

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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager: VibratorManager =
            App.instance.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        return vibratorManager.defaultVibrator
    }
    return App.instance.getSystemService(VIBRATOR_SERVICE) as Vibrator
}
