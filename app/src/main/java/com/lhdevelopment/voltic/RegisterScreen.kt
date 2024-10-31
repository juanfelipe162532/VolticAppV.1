package com.lhdevelopment.voltic

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
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
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.regex.Pattern

private const val TAG = "RegisterScreen"

private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@(hotmail\\.com|gmail\\.com|yahoo\\.com|yahoo\\.es|outlook\\.com)$"
        )
        return emailPattern.matcher(email).matches()
    }

    private fun isValidUsername(username: String): Boolean {
        val usernamePattern = Pattern.compile(
            "^[A-Za-z][A-Za-z0-9@#\$%^&+=!]{4,11}$"
        )
        return usernamePattern.matcher(username).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = Pattern.compile("^.{5,12}$")
        return passwordPattern.matcher(password).matches()
    }


class RegisterScreen : ComponentActivity() {

    private val apiService: ApiService by lazy {
        ApiClient.apiService
    }

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
        setContentView(R.layout.registerscreen)

        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirmPasswordEditText)
        val termsCheckBox: CheckBox = findViewById(R.id.termsCheckBox)
        val registerButton: Button = findViewById(R.id.registerButton)

        val passwordToggle: ImageView = findViewById(R.id.passwordToggle)
        val confirmPasswordToggle: ImageView = findViewById(R.id.confirmPasswordToggle)

        passwordToggle.setOnClickListener {
            togglePasswordVisibility(passwordEditText, passwordToggle)
        }

        confirmPasswordToggle.setOnClickListener {
            togglePasswordVisibility(confirmPasswordEditText, confirmPasswordToggle)
        }
        val emailStatus: ImageView = findViewById(R.id.emailStatus)
        val usernameStatus: ImageView = findViewById(R.id.usernameStatus)
        val passwordStatus: ImageView = findViewById(R.id.passwordStatus)
        val confirmPasswordStatus: ImageView = findViewById(R.id.confirmPasswordStatus)

        val registerText: TextView = findViewById(R.id.register_text)
        val spannableString = SpannableString("Ya estás registrado? Inicia sesión.")

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@RegisterScreen, LoginScreen::class.java)
                startActivity(intent)
            }
        }

        spannableString.setSpan(clickableSpan, 21, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(
            ForegroundColorSpan(Color.RED), 21, 34,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        registerText.text = spannableString
        registerText.movementMethod = LinkMovementMethod.getInstance()

        // Validación en tiempo real
        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isValidEmail(s.toString())) {
                    emailStatus.setImageResource(R.drawable.ic_check)
                    emailStatus.visibility = ImageView.VISIBLE
                } else {
                    emailStatus.setImageResource(R.drawable.ic_error)
                    emailStatus.visibility = ImageView.VISIBLE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        usernameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isValidUsername(s.toString())) {
                    usernameStatus.setImageResource(R.drawable.ic_check)
                    usernameStatus.visibility = ImageView.VISIBLE
                } else {
                    usernameStatus.setImageResource(R.drawable.ic_error)
                    usernameStatus.visibility = ImageView.VISIBLE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isValidPassword(s.toString())) {
                    passwordStatus.setImageResource(R.drawable.ic_check)
                    passwordStatus.visibility = ImageView.VISIBLE
                } else {
                    passwordStatus.setImageResource(R.drawable.ic_error)
                    passwordStatus.visibility = ImageView.VISIBLE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        confirmPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == passwordEditText.text.toString()) {
                    confirmPasswordStatus.setImageResource(R.drawable.ic_check)
                    confirmPasswordStatus.visibility = ImageView.VISIBLE
                } else {
                    confirmPasswordStatus.setImageResource(R.drawable.ic_error)
                    confirmPasswordStatus.visibility = ImageView.VISIBLE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val termsAccepted = termsCheckBox.isChecked

            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor, completa todos los campos requeridos",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!termsAccepted) {
                Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val fullUrl = "https://ykyoaekhp9.execute-api.us-east-1.amazonaws.com/Stage1/registration/"
            createUser(fullUrl, email, username, password)
        }
    }

    private fun createUser(fullUrl: String, email: String, username: String, password: String) {
        val userJsonObject = JSONObject().apply {
            put("Email", email)
            put("Nombre_Usuario", username)
            put("Contrasena", password)
        }

        // Se crea un objeto RegisterRequestBody con el cuerpo JSON como una cadena
        val requestBody = RegisterRequestBody(userJsonObject.toString())

        // Log para mostrar la estructura y contenido del JSON
        Log.d(TAG, "Estructura del JSON enviado: ${requestBody.body}")

        apiService.registerUser(fullUrl, requestBody).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Registro exitoso")
                    Log.d(
                        TAG,
                        "Respuesta del servidor: ${response.body()}"
                    )
                    Toast.makeText(this@RegisterScreen, "Registro exitoso", Toast.LENGTH_SHORT).show()

                    val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("isRegistered", true)
                    editor.apply()

                    navigateToLoginScreen()
                } else {
                    Log.e(TAG, "Error en el registro: ${response.code()}")
                    Log.e(
                        TAG,
                        "Respuesta del servidor: ${response.errorBody()?.string()}"
                    )
                    Toast.makeText(this@RegisterScreen, "Error en el registro", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e(
                    TAG,
                    "Error en la solicitud de registro: ${t.message}",
                    t
                )
                Toast.makeText(
                    this@RegisterScreen,
                    "Error en el registro: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun togglePasswordVisibility(passwordEditText: EditText, toggle: ImageView) {
        if (passwordEditText.transformationMethod is PasswordTransformationMethod) {
            passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            toggle.setImageResource(R.drawable.ic_eye_off)  // Se cambia el ícono para indicar que la contraseña es visible
        } else {
            passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            toggle.setImageResource(R.drawable.ic_eye)  // Se cambia el ícono para indicar que la contraseña está oculta
        }
        passwordEditText.setSelection(passwordEditText.text.length)
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(this, LoginScreen::class.java)
        startActivity(intent)
        finish()
    }
}