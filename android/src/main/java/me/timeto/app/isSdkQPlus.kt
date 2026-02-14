package me.timeto.app

import android.os.Build

fun isSdkQPlus(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
