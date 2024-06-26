package com.lhdevelopment.voltic

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class WelcomeScreen2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcomescreen2)

        val nextButton: Button = findViewById(R.id.nextButton)

        nextButton.setOnClickListener {
            val intent = Intent(this, WelcomeScreen3::class.java)
            startActivity(intent)
        }
    }
}