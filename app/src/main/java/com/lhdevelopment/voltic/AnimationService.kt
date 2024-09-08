package com.lhdevelopment.voltic



import android.animation.ObjectAnimator

import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.animation.Animator
import android.content.Context

class AnimationService(private val context: Context) {

    fun startAnimations(
        halfMoonImageView: ImageView,
        timeTextView: TextView,
        exitIcon: ImageView,
        connectionStatusValidation: View,
        dataRectangle: View,
        batteryDataText: TextView,
        batteryDataNumbers: TextView,
        distanceDataText: TextView,
        distanceDataNumbers: TextView,
        distanceDataKm: TextView,
        timeDataText: TextView,
        timeDataNumbers: TextView,
        stadisticsButton: View,
        historyButton: View,
        servicesButton: View,
        settingsButton: View,
        mapButton: View,
        batteryButton: View,
        speedMeterNumbers: View,
        speedMeterMetric: View,
        speedMeterNumbers2: View,
        validationIcon: ImageView,
        connectionStatusTrue: View,
        connectionStatusFalse: View,
        isBluetoothConnected: Boolean
    ) {
        // Usar un ViewTreeObserver para asegurar que la vista esté medida antes de iniciar la animación
        halfMoonImageView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // Eliminar el listener para evitar múltiples llamadas
                    halfMoonImageView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    // Crear la animación de recorte
                    val animator = ObjectAnimator.ofFloat(halfMoonImageView, "revealProgress", 0f, 1f).apply {
                        duration = 1000
                        interpolator = AccelerateDecelerateInterpolator()
                        startDelay = 500
                    }

                    animator.start()

                    // Animaciones de desvanecimiento para los elementos
                    val fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)

                    animator.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {}

                        override fun onAnimationEnd(animation: Animator) {
                            // Configurar visibilidad y animaciones para cada vista
                            animateViewVisibility(timeTextView, 500, 900)
                            animateViewVisibility(exitIcon, 500, 900)
                            animateViewVisibility(dataRectangle, 500, 1000)
                            animateViewVisibility(batteryDataText, 500, 1200)
                            animateViewVisibility(batteryDataNumbers, 500, 1300)
                            animateViewVisibility(distanceDataText, 500, 1400)
                            animateViewVisibility(distanceDataNumbers, 500, 1500)
                            animateViewVisibility(distanceDataKm, 500, 1500)
                            animateViewVisibility(timeDataText, 500, 1600)
                            animateViewVisibility(timeDataNumbers, 500, 1700)
                            animateViewVisibility(stadisticsButton, 500, 1100)
                            animateViewVisibility(historyButton, 500, 1200)
                            animateViewVisibility(servicesButton, 500, 1300)
                            animateViewVisibility(settingsButton, 500, 1400)
                            animateViewVisibility(mapButton, 500, 1500)
                            animateViewVisibility(batteryButton, 500, 1600)

                            // Aplicar animación de entrada
                            speedMeterNumbers.startAnimation(fadeInAnimation)
                            speedMeterMetric.startAnimation(fadeInAnimation)
                            speedMeterNumbers2.startAnimation(fadeInAnimation)

                            // Animación de validación
                            val rotationAnimation = ObjectAnimator.ofFloat(validationIcon, "rotation", 0f, 360f).apply {
                                duration = 1000
                                repeatCount = ObjectAnimator.INFINITE
                                interpolator = AccelerateDecelerateInterpolator()
                                start()
                            }

                            // Validación de conexión Bluetooth
                            validationIcon.postDelayed({
                                rotationAnimation.cancel()
                                connectionStatusValidation.visibility = View.GONE

                                if (isBluetoothConnected) {
                                    connectionStatusTrue.visibility = View.VISIBLE
                                    startSpeedTracking()
                                } else {
                                    connectionStatusFalse.visibility = View.VISIBLE
                                    connectionStatusFalse.alpha = 0f
                                    connectionStatusFalse.animate().alpha(1f).setDuration(500).start()
                                    startSpeedTracking()
                                }
                            }, 1500)
                        }

                        override fun onAnimationCancel(animation: Animator) {}

                        override fun onAnimationRepeat(animation: Animator) {}
                    })
                }
            }
        )
    }

    private fun animateViewVisibility(view: View, duration: Long, startDelay: Long) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        view.animate().alpha(1f).setDuration(duration).setStartDelay(startDelay).start()
    }

    private fun startSpeedTracking() {
        // Implementar lógica para iniciar el rastreo de velocidad
    }
}
