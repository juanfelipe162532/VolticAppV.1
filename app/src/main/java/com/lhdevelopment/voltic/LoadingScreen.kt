package com.lhdevelopment.voltic

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import android.util.Log
import java.util.Calendar

class LoadingScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Determinar la hora actual
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Establecer el tema basado en la hora: modo oscuro entre 6 PM y 6 AM
        val themeResId = if (currentHour >= 18 || currentHour < 6) {
            R.style.Theme_VolticAppV1_Night
        } else {
            R.style.Theme_VolticAppV1_Day
        }

        // Aplicar el tema
        setTheme(themeResId)

        // Establecer el contenido
        setContentView(R.layout.loading_screen)

        // Obtener el nombre del tema actual para propósitos de depuración
        val themeName = resources.getResourceEntryName(themeResId)
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
                    sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
                    Intent(this, WelcomeScreen1::class.java)
                }
                isLoggedIn -> {
                    Intent(this, BluetoothConnection1::class.java)
                }
                else -> {
                    Intent(this, LoginScreen::class.java)
                }
            }
            startActivity(intent)
            finish() // Cierra la pantalla de carga para que no se vuelva a mostrar al presionar "Atrás".
        }, 2000) // Retraso de 2 segundos para mostrar la pantalla de carga.
    }
}
