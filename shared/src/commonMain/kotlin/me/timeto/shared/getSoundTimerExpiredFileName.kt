package me.timeto.shared

fun getSoundTimerExpiredFileName(withExtension: Boolean): String =
    "sound_timer_expired${if (withExtension) ".mp3" else ""}"
