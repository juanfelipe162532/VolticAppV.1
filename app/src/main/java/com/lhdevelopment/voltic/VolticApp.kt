package com.lhdevelopment.voltic

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import java.util.Calendar

class VolticApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Cambiar al modo noche si la hora es superior a las 6 PM
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val nightMode = if (currentHour >= 18) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
