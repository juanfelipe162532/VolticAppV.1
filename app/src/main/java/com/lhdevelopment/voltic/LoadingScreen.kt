package com.lhdevelopment.voltic

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import android.util.Log

class LoadingScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_screen)

        // Forzar el modo oscuro para pruebas
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        // Obtener el nombre del tema actual
        val themeName = resources.getResourceEntryName(R.style.Theme_VolticAppV1_Night)
        Log.d("CurrentTheme", "Tema actual: $themeName")

        // Espera 2 segundos para mostrar la pantalla de carga
        Handler(Looper.getMainLooper()).postDelayed({
            // Accede a las preferencias compartidas
            val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
            val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
            val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

            // Navega a la pantalla correspondiente
            val intent = when {
                isFirstRun -> {
                    // Si es la primera vez que se ejecuta la app, muestra la pantalla de bienvenida.
                    sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
                    Intent(this, WelcomeScreen1::class.java)
                }
                isLoggedIn -> {
                    // Si el usuario ya ha iniciado sesión y seleccionó "Recordar usuario", navega a la pantalla de conexión Bluetooth.
                    Intent(this, BluetoothConnection1::class.java)
                }
                else -> {
                    // Si no ha iniciado sesión, muestra la pantalla de inicio de sesión.
                    Intent(this, LoginScreen::class.java)
                }
            }
            startActivity(intent)
            finish() // Cierra la pantalla de carga para que no se vuelva a mostrar al presionar "Atrás".
        }, 2000) // Retraso de 2 segundos para mostrar la pantalla de carga.
    }
}
