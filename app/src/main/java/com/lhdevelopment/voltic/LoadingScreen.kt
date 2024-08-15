package com.lhdevelopment.voltic

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity

class LoadingScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_screen)

        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

        Handler(Looper.getMainLooper()).postDelayed({
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


