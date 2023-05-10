package me.timeto.shared

import android.app.Application
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.squareup.sqldelight.android.AndroidSqliteDriver
import me.timeto.appdbsq.TimetomeDB
import me.timeto.shared.db.DB_NAME
import java.io.InputStreamReader

private lateinit var androidApplication: Application

internal actual val REPORT_API_TITLE = "🤖 Android"

fun initKmmAndroid(application: Application, build: Int) {
    androidApplication = application

    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    val deviceName = if (model.startsWith(manufacturer)) model else "$manufacturer $model"

    val deviceData = DeviceData(
        build = build,
        os = "android-${Build.VERSION.RELEASE}",
        device = deviceName,
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
    // https://developer.android.com/topic/security/data
    private val espMasterKey by lazy { MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC) }
    private val esp by lazy {
        EncryptedSharedPreferences.create(
            "timetome_encrypted_shared_preferences",
            espMasterKey,
            androidApplication,
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
