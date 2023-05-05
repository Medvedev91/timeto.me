package app.time_to.timeto

import android.app.Application
import timeto.shared.initKmmAndroid

class App : Application() {

    companion object {

        lateinit var instance: App
            private set
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
        initKmmAndroid(this, BuildConfig.VERSION_CODE)
    }
}
