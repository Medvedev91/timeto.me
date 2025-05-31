package me.timeto.shared

import android.app.Application
import android.os.Build
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import me.timeto.appdbsq.TimetomeDB
import me.timeto.shared.db.DB_NAME

fun initKmpAndroid(
    application: Application,
    build: Int,
    version: String,
    flavor: String,
) {
    androidApplication = application

    val manufacturer: String = Build.MANUFACTURER
    val model: String = Build.MODEL
    val deviceName: String =
        if (model.startsWith(manufacturer)) model else "$manufacturer $model"

    val systemInfo = SystemInfo(
        build = build,
        version = version,
        os = SystemInfo.Os.Android(Build.VERSION.RELEASE),
        device = deviceName,
        flavor = flavor,
    )
    initKmp(AndroidSqliteDriver(TimetomeDB.Schema, application, DB_NAME), systemInfo)
}
