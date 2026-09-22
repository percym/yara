package dev.percym.yara

import android.app.Application
import com.google.android.libraries.places.api.Places

class YaRAApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.PLACES_API_KEY)
        }
    }
}
