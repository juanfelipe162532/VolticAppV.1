package com.lhdevelopment.voltic

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SettingsScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settingsscreen)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    // Método para cambiar el idioma
    fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        // Actualizar la configuración global
        val config = Configuration()
        config.setLocale(locale)

        // Actualizar el contexto de la aplicación
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        // Reiniciar la actividad principal para aplicar el cambio en toda la aplicación
        val intent = Intent(this, MainPanel::class.java)
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
