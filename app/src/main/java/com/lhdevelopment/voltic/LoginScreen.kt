package com.lhdevelopment.voltic

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.json.JSONObject
import org.mindrot.jbcrypt.BCrypt

private const val TAG = "LoginScreen"

class LoginScreen : ComponentActivity() {

    private val apiService: ApiService by lazy {
        ApiClient.apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginscreen)

        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val rememberMeCheckBox: CheckBox = findViewById(R.id.RememberCheckBox)
        val loginButton: Button = findViewById(R.id.loginButton)
        val passwordToggle: ImageView = findViewById(R.id.passwordToggle)

        val registerText: TextView = findViewById(R.id.login_text)
        val spannableString = SpannableString("Nuevo por aquí? Regístrate.")

        passwordToggle.setOnClickListener {
            togglePasswordVisibility(passwordEditText, passwordToggle)
        }

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginScreen, RegisterScreen::class.java)
                startActivity(intent)
            }
        }

        spannableString.setSpan(clickableSpan, 16, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.RED), 16, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        registerText.text = spannableString
        registerText.movementMethod = LinkMovementMethod.getInstance()

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val rememberMe = rememberMeCheckBox.isChecked

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val fullUrl = "https://ykyoaekhp9.execute-api.us-east-1.amazonaws.com/Stage1/login/"
                loginUser(fullUrl, username, password, rememberMe)
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun loginUser(fullUrl: String, username: String, password: String, rememberMe: Boolean) {
        val userJsonObject = JSONObject().apply {
            put("Nombre_Usuario", username)
            put("Contrasena", password)
        }

        val requestBody = RegisterRequestBody(userJsonObject.toString())

        apiService.loginUser(fullUrl, requestBody).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    Log.d(TAG, "Respuesta del servidor: ${authResponse?.message}")
                    Toast.makeText(this@LoginScreen, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    navigateToBluetoothConnection()
                    if (rememberMe) {
                        saveUserCredentials(username, password)
                    }
                    navigateToBluetoothConnection()
                } else {
                    Log.e(TAG, "Error en el inicio de sesión: ${response.code()}")
                    Toast.makeText(this@LoginScreen, "Error en el inicio de sesión", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Log.e(TAG, "Error en la solicitud de inicio de sesión: ${t.message}", t)
                Toast.makeText(this@LoginScreen, "Error en el inicio de sesión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserCredentials(username: String, password: String) {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", true)
        editor.putString("username", username)
        editor.putString("password", password)
        editor.apply()
    }

    private fun togglePasswordVisibility(passwordEditText: EditText, toggle: ImageView) {
        if (passwordEditText.transformationMethod is PasswordTransformationMethod) {
            passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            toggle.setImageResource(R.drawable.ic_eye_off)  // Cambia el ícono para indicar que la contraseña es visible
        } else {
            passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            toggle.setImageResource(R.drawable.ic_eye)  // Cambia el ícono para indicar que la contraseña está oculta
        }
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    private fun navigateToBluetoothConnection() {
        val intent = Intent(this, BluetoothConnection1::class.java)
        startActivity(intent)
        finish() // Llama a finish() para cerrar la pantalla de inicio de sesión
    }
}








































