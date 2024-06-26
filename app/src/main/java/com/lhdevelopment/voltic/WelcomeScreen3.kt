package com.lhdevelopment.voltic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class WelcomeScreen3 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcomescreen3)

        val nextButton: Button = findViewById(R.id.nextButton)

        nextButton.setOnClickListener {
            val intent = Intent(this, WelcomeScreen4::class.java)
            startActivity(intent)
        }
    }
}