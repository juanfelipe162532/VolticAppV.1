package com.lhdevelopment.voltic

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import org.json.JSONObject


data class RegisterRequestBody(val body: String)
private const val TAG = "RegisterScreen"

class RegisterScreen : ComponentActivity() {

    private val apiService: ApiService by lazy {
        ApiClient.apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registerscreen)

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirmPasswordEditText)
        val termsCheckBox: CheckBox = findViewById(R.id.termsCheckBox)
        val registerButton: Button = findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val termsAccepted = termsCheckBox.isChecked

            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos requeridos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!termsAccepted) {
                Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fullUrl = "https://bbsozgku32.execute-api.us-east-1.amazonaws.com/Stage1-Registration/"
            createUser(fullUrl, email, username, password)
        }
    }

    private fun createUser(fullUrl: String, email: String, username: String, password: String) {
        val userJsonObject = JSONObject().apply {
            put("Email", email)
            put("Nombre_Usuario", username)
            put("Contrasena", password)
        }

        // Crear un objeto RegisterRequestBody con el cuerpo JSON como una cadena
        val requestBody = RegisterRequestBody(userJsonObject.toString())

        // Log para mostrar la estructura y contenido del JSON
        Log.d(TAG, "Estructura del JSON enviado: ${requestBody.body}")

        apiService.registerUser(fullUrl, requestBody).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Registro exitoso")
                    Log.d(TAG, "Respuesta del servidor: ${response.body()}")
                    Toast.makeText(this@RegisterScreen, "Registro exitoso", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG, "Error en el registro: ${response.code()}")
                    Log.e(TAG, "Respuesta del servidor: ${response.errorBody()?.string()}")
                    Toast.makeText(this@RegisterScreen, "Error en el registro", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e(TAG, "Error en la solicitud de registro: ${t.message}", t)
                Toast.makeText(this@RegisterScreen, "Error en el registro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

























