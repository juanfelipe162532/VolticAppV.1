package com.lhdevelopment.voltic

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "language_preference") {
            val selectedLanguage = sharedPreferences?.getString(key, "es") ?: "es"

            // Guardar el idioma en SharedPreferences
            val prefs = requireActivity().getSharedPreferences("settings", AppCompatActivity.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putString("language_preference", selectedLanguage)
            editor.apply()

            // Aplicar el cambio globalmente
            (activity as? SettingsScreen)?.let {
                it.setLocale(selectedLanguage)
                it.recreate() // Recargar la actividad para aplicar los cambios
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
    }
}
