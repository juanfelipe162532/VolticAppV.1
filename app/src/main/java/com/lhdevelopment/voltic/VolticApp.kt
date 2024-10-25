package com.lhdevelopment.voltic

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import java.util.Calendar
import java.util.Locale

class VolticApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Determinar la hora actual para el tema claro/oscuro
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Establecer el modo de tema basado en la hora: modo oscuro entre 6 PM y 6 AM
        AppCompatDelegate.setDefaultNightMode(
            if (currentHour >= 18 || currentHour < 6)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )

        // Obtener el idioma guardado en SharedPreferences (por defecto es "es")
        val sharedPrefs = getSharedPreferences("settings", MODE_PRIVATE)
        val languageCode = sharedPrefs.getString("language_preference", "es") ?: "es"

        // Aplicar el idioma globalmente
        setLocale(languageCode)
    }

    // Método para aplicar el idioma en toda la aplicación
    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        // Actualizar la configuración global
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }
}
