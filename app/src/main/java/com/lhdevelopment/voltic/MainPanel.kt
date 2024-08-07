package com.lhdevelopment.voltic

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.activity.ComponentActivity
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

class MainPanel : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mainpanel)

        val halfMoonImageView = findViewById<ClippingImageView>(R.id.speedMeterShape)
        val connectedIcon = findViewById<ImageView>(R.id.connectedIcon)
        val speedMeterNumbers = findViewById<TextView>(R.id.speedMeterNumbers)
        val speedMeterMetric = findViewById<TextView>(R.id.speedMeterMetric)
        val connectionStatus = findViewById<RelativeLayout>(R.id.connectionStatus)

        // Usar un ViewTreeObserver para asegurar que la vista esté medida antes de iniciar la animación
        halfMoonImageView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // Eliminar el listener para evitar múltiples llamadas
                    halfMoonImageView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    // Crear la animación de recorte
                    val animator = ObjectAnimator.ofFloat(halfMoonImageView, "revealProgress", 0f, 1f).apply {
                        duration = 2000 // Duración de la animación en milisegundos
                        interpolator = AccelerateDecelerateInterpolator() // Interpolador para animación suave
                        startDelay = 1000 // Retardo antes de que comience la animación
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
                            connectedIcon.visibility = View.VISIBLE
                            connectionStatus.visibility = View.VISIBLE

                            // Aplicar la animación de entrada
                            speedMeterNumbers.startAnimation(fadeInAnimation)
                            speedMeterMetric.startAnimation(fadeInAnimation)
                            connectedIcon.startAnimation(fadeInAnimation)
                            connectionStatus.startAnimation(fadeInAnimation)

                            // Animación de cambio de números
                            val numberAnimator = ValueAnimator.ofFloat(0f, 25f).apply {
                                duration = 3000 // Duración de la animación
                                interpolator = AccelerateDecelerateInterpolator() // Interpolador para animación suave

                                addUpdateListener { animation ->
                                    val value = animation.animatedValue as Float
                                    speedMeterNumbers.text = String.format("%.1f", value)
                                }
                            }

                            // Animación inversa
                            val reverseNumberAnimator = ValueAnimator.ofFloat(25f, 0f).apply {
                                duration = 3000 // Duración de la animación
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







