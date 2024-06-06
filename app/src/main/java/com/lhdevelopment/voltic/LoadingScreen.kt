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

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, WelcomeScreen1::class.java)
            startActivity(intent)
            finish()
        }, 5000)
    }
}