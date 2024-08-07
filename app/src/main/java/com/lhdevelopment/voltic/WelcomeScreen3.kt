package com.lhdevelopment.voltic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.lhdevelopment.voltic.RegisterScreen

class WelcomeScreen3 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcomescreen3)

        val nextButton: Button = findViewById(R.id.nextButton)
        val skipButton: Button = findViewById(R.id.skipButton)

        skipButton.setOnClickListener {
            val intent = Intent(this, RegisterScreen::class.java)
            startActivity(intent)
        }

        nextButton.setOnClickListener {
            val intent = Intent(this, WelcomeScreen4::class.java)
            startActivity(intent)
        }
    }
}