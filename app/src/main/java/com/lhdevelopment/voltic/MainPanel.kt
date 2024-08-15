package com.lhdevelopment.voltic

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainPanel : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainpanel)

        val halfMoonImageView = findViewById<ClippingImageView>(R.id.speedMeterShape)
        val connectedIcon = findViewById<ImageView>(R.id.connectedIcon)
        val disconnectedIcon = findViewById<ImageView>(R.id.disconnectedIcon)
        val speedMeterNumbers = findViewById<TextView>(R.id.speedMeterNumbers)
        val speedMeterNumbers2 = findViewById<ImageView>(R.id.speedMeter_numbers)
        val speedMeterMetric = findViewById<TextView>(R.id.speedMeterMetric)
        val connectionStatusValidation = findViewById<RelativeLayout>(R.id.connectionStatusValidation)
        val connectionStatusTrue = findViewById<RelativeLayout>(R.id.connectionStatusTrue)
        val connectionStatusFalse = findViewById<RelativeLayout>(R.id.connectionStatusFalse)
        val validationIcon = findViewById<ImageView>(R.id.ValidationIcon)

        // Obtener si la conexión Bluetooth fue exitosa desde el Intent
        val isBluetoothConnected = intent.getBooleanExtra("EXTRA_BT_CONNECTED", false)

        // Usar un ViewTreeObserver para asegurar que la vista esté medida antes de iniciar la animación
        halfMoonImageView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // Eliminar el listener para evitar múltiples llamadas
                    halfMoonImageView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    // Crear la animación de recorte
                    val animator = ObjectAnimator.ofFloat(halfMoonImageView, "revealProgress", 0f, 1f).apply {
                        duration = 1000 // Duración de la animación en milisegundos
                        interpolator = AccelerateDecelerateInterpolator() // Interpolador para animación suave
                        startDelay = 500 // Retardo antes de que comience la animación
                    }

                    // Iniciar la animación de recorte
                    animator.start()

                    // Cargar la animación de desvanecimiento
                    val fadeInAnimation = AnimationUtils.loadAnimation(this@MainPanel, R.anim.fade_in)

                    // Esperar a que la animación del ClippingImageView termine antes de mostrar los otros objetos
                    animator.addListener(object : android.animation.Animator.AnimatorListener {
                        override fun onAnimationStart(animation: android.animation.Animator) {}

                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            // Cambiar la visibilidad a VISIBLE antes de iniciar la animación de desvanecimiento
                            speedMeterNumbers.visibility = View.VISIBLE
                            speedMeterMetric.visibility = View.VISIBLE
                            connectionStatusValidation.visibility = View.VISIBLE
                            speedMeterNumbers2.visibility = View.VISIBLE

                            // Aplicar la animación de entrada
                            speedMeterNumbers.startAnimation(fadeInAnimation)
                            speedMeterMetric.startAnimation(fadeInAnimation)
                            speedMeterNumbers2.startAnimation(fadeInAnimation)

                            // Animación de validación: hacer girar el icono de validación
                            val rotationAnimation = ObjectAnimator.ofFloat(validationIcon, "rotation", 0f, 360f).apply {
                                duration = 1000 // Duración de la animación de rotación
                                repeatCount = ObjectAnimator.INFINITE
                                interpolator = AccelerateDecelerateInterpolator()
                                start()
                            }

                            // Validación de conexión Bluetooth
                            validationIcon.postDelayed({
                                rotationAnimation.cancel() // Detener la animación de rotación
                                connectionStatusValidation.visibility = View.GONE // Ocultar la validación

                                if (isBluetoothConnected) {
                                    connectionStatusTrue.visibility = View.VISIBLE // Mostrar la conexión exitosa
                                    connectedIcon.startAnimation(fadeInAnimation) // Animación de entrada para el icono conectado
                                } else {
                                    connectionStatusFalse.visibility = View.VISIBLE // Mostrar la conexión fallida
                                    disconnectedIcon.startAnimation(fadeInAnimation) // Animación de entrada para el icono desconectado
                                }
                            }, 3000) // 3 segundos de retraso (simulación de la validación)

                            // Animación de cambio de números
                            val numberAnimator = ValueAnimator.ofFloat(0f, 10f).apply {
                                duration = 1500 // Duración de la animación
                                interpolator = AccelerateDecelerateInterpolator() // Interpolador para animación suave

                                addUpdateListener { animation ->
                                    val value = animation.animatedValue as Float
                                    speedMeterNumbers.text = String.format("%.1f", value)
                                }
                            }

                            // Animación inversa
                            val reverseNumberAnimator = ValueAnimator.ofFloat(10f, 0f).apply {
                                duration = 1500 // Duración de la animación
                                interpolator = AccelerateDecelerateInterpolator() // Interpolador para animación suave

                                addUpdateListener { animation ->
                                    val value = animation.animatedValue as Float
                                    speedMeterNumbers.text = String.format("%.1f", value)
                                }
                            }

                            // Ejecutar animaciones de números secuencialmente
                            numberAnimator.start()
                            numberAnimator.addListener(object : android.animation.Animator.AnimatorListener {
                                override fun onAnimationStart(animation: android.animation.Animator) {}

                                override fun onAnimationEnd(animation: android.animation.Animator) {
                                    reverseNumberAnimator.start()
                                }

                                override fun onAnimationCancel(animation: android.animation.Animator) {}

                                override fun onAnimationRepeat(animation: android.animation.Animator) {}
                            })
                        }

                        override fun onAnimationCancel(animation: android.animation.Animator) {}

                        override fun onAnimationRepeat(animation: android.animation.Animator) {}
                    })
                }
            }
        )
    }
}










