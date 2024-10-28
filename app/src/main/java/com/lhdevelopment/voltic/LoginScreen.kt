package com.lhdevelopment.voltic

import android.content.Intent
import android.graphics.Color
import android.content.Context
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
import android.widget.*
import androidx.activity.ComponentActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.json.JSONObject
import java.util.Calendar

private const val TAG = "LoginScreen"

class LoginScreen : ComponentActivity() {

    private val apiService: ApiService by lazy {
        ApiClient.apiService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val themeResId = if (currentHour >= 18 || currentHour < 6) {
            R.style.Theme_VolticAppV1_Night
        } else {
            R.style.Theme_VolticAppV1_Day
        }

        setTheme(themeResId)
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
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val rememberMe = rememberMeCheckBox.isChecked

            when {
                username.isEmpty() -> {
                    Toast.makeText(this, "El nombre de usuario no puede estar vacío", Toast.LENGTH_SHORT).show()
                    usernameEditText.requestFocus()
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "La contraseña no puede estar vacía", Toast.LENGTH_SHORT).show()
                    passwordEditText.requestFocus()
                }
                else -> {
                    val fullUrl = "https://ykyoaekhp9.execute-api.us-east-1.amazonaws.com/Stage1/login/"
                    loginUser(fullUrl, username, password, rememberMe)
                }
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
                // Obtener la respuesta cruda del servidor
                val rawResponse = response.body()?.toString() ?: response.errorBody()?.string()
                Log.d(TAG, "Respuesta cruda del servidor: $rawResponse")

                if (response.isSuccessful) {
                    val authResponse = response.body()

                    if (authResponse != null) {
                        val statusCode = authResponse.statusCode

                        if (statusCode == 200) {
                            try {
                                // Parsear el JSON anidado en el campo "body"
                                val responseBody =
                                    JSONObject(authResponse.body) // Asegúrate de que 'body' es un String en AuthResponse
                                val idUsuario = responseBody.optInt("ID_Usuario", 0)
                                val message =
                                    responseBody.optString("message", "Mensaje no disponible")

                                Log.d(TAG, "ID_Usuario obtenido: $idUsuario, Mensaje: $message")

                                // Comprobar ID_Usuario y manejar la lógica de navegación
                                if (idUsuario != 0) {
                                    saveUserId(idUsuario, this@LoginScreen)

                                    if (rememberMe) {
                                        saveUserCredentials(username, password)
                                    }

                                    navigateToBluetoothConnection() // Navega a la siguiente actividad
                                } else {
                                    Toast.makeText(
                                        this@LoginScreen,
                                        "Usuario o contraseña incorrectos",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error al parsear el cuerpo de la respuesta", e)
                            }
                        } else {
                            Toast.makeText(
                                this@LoginScreen,
                                "Error en el inicio de sesión. Código: $statusCode",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Log.e(TAG, "Respuesta vacía del servidor")
                    }
                } else {
                    Log.e(TAG, "Error en el inicio de sesión: ${response.code()}")
                    Toast.makeText(
                        this@LoginScreen,
                        "Error en el servidor. Intente más tarde.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Log.e(TAG, "Error en la solicitud de inicio de sesión: ${t.message}", t)
                Toast.makeText(
                    this@LoginScreen,
                    "Error en la conexión. Verifique su red.",
                    Toast.LENGTH_SHORT
                ).show()
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
            toggle.setImageResource(R.drawable.ic_eye_off)
        } else {
            passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            toggle.setImageResource(R.drawable.ic_eye)
        }
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    private fun navigateToBluetoothConnection() {
        val intent = Intent(this, BluetoothConnection2::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveUserId(userId: Int, context: Context) {
        val sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("ID_Usuario", userId)
        editor.apply()
    }
}
