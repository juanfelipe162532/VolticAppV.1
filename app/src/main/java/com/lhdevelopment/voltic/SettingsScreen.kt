package com.lhdevelopment.voltic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SettingsScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settingsscreen) // Asegúrate de tener un layout

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, SettingsFragment()) // Asegúrate de que 'settings_container' existe en tu layout
                .commit()
        }
    }
}








