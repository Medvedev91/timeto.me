package me.timeto.app.misc

import android.os.Build

fun isSdkQPlus(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
