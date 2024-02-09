package me.timeto.shared

import android.app.Application
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import me.timeto.appdbsq.TimetomeDB
import me.timeto.shared.db.DB_NAME
import java.io.InputStreamReader

private lateinit var androidApplication: Application

internal actual val REPORT_API_TITLE = "🤖 Android"

fun initKmmAndroid(
    application: Application,
    build: Int,
    flavor: String,
) {
    androidApplication = application

    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    val deviceName = if (model.startsWith(manufacturer)) model else "$manufacturer $model"

    val deviceData = DeviceData(
        build = build,
        os = "android-${Build.VERSION.RELEASE}",
        device = deviceName,
        flavor = flavor,
    )
    initKmm(AndroidSqliteDriver(TimetomeDB.Schema, application, DB_NAME), deviceData)
}

actual fun getResourceContent(file: String, type: String) = androidApplication
    .resources
    .openRawResource(
        // No type, only file name without extension.
        androidApplication.resources.getIdentifier(file, "raw", androidApplication.packageName)
    )
    .use { stream ->
        InputStreamReader(stream).use { reader ->
            reader.readText()
        }
    }

internal actual object SecureLocalStorage {

    // ESP - Encrypted Shared Preferences
    // https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences
    // todo https://developer.android.com/jetpack/androidx/releases/security
    // todo https://issuetracker.google.com/issues/164901843

    private val espMasterKey by lazy {
        MasterKey.Builder(androidApplication)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val esp by lazy {
        EncryptedSharedPreferences.create(
            androidApplication,
            "timetome_encrypted_shared_preferences_v2",
            espMasterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    actual fun getOrNull(key: SecureLocalStorage__Key): String? =
        esp.getString(key.name, null)

    actual fun upsert(key: SecureLocalStorage__Key, value: String?) {
        esp.edit().putString(key.name, value).apply()
    }
}
