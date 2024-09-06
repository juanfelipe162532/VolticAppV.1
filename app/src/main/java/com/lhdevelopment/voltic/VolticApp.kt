package com.lhdevelopment.voltic

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import java.util.Calendar

class VolticApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Determinar la hora actual
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Establecer el modo de tema basado en la hora: modo oscuro entre 6 PM y 6 AM
        AppCompatDelegate.setDefaultNightMode(
            if (currentHour >= 18 || currentHour < 6)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
