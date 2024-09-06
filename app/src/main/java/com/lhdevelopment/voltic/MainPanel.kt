package com.lhdevelopment.voltic

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.text.SimpleDateFormat
import android.view.WindowManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*
import java.util.Calendar

@Suppress("DEPRECATION")
class MainPanel : ComponentActivity() {

    private var speedChangeTime: Long = 0
    private var zeroSpeedStartTime: Long = 0
    private var isSpeedNonZero: Boolean = false
    private var isSpeedZeroMoreThanFiveMinutes: Boolean = false
    private val timeDataNumbers: TextView by lazy { findViewById(R.id.timedataNumbers) }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private var isTrackingSpeed = false

    private lateinit var timeTextView: TextView
    private val handler = Handler()
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000) // Actualiza cada segundo
        }
    }

    private var totalDistance: Float = 0f
    private var lastLocation: Location? = null

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

        setContentView(R.layout.mainpanel)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        timeTextView = findViewById(R.id.timeTextView)

        val exitIcon = findViewById<ImageView>(R.id.exitIcon)
        val halfMoonImageView = findViewById<ClippingImageView>(R.id.speedMeterShape)
        val speedMeterNumbers = findViewById<TextView>(R.id.speedMeterNumbers)
        val speedMeterNumbers2 = findViewById<ImageView>(R.id.speedMeter_numbers)
        val speedMeterMetric = findViewById<TextView>(R.id.speedMeterMetric)
        val connectionStatusValidation = findViewById<RelativeLayout>(R.id.connectionStatusValidation)
        val connectionStatusTrue = findViewById<RelativeLayout>(R.id.connectionStatusTrue)
        val connectionStatusFalse = findViewById<RelativeLayout>(R.id.connectionStatusFalse)
        val validationIcon = findViewById<ImageView>(R.id.ValidationIcon)
        val dataRectangle = findViewById<RelativeLayout>(R.id.data_rectangle)
        val stadisticsButton = findViewById<RelativeLayout>(R.id.rectangle1)
        val button1 = findViewById<Button>(R.id.button1)
        val historyButton = findViewById<RelativeLayout>(R.id.rectangle2)
        val button2 = findViewById<Button>(R.id.button2)
        val servicesButton = findViewById<RelativeLayout>(R.id.rectangle3)
        val button3 = findViewById<Button>(R.id.button3)
        val settingsButton = findViewById<RelativeLayout>(R.id.rectangle4)
        val button4 = findViewById<Button>(R.id.button4)
        val mapButton = findViewById<RelativeLayout>(R.id.rectangle5)
        val button5 = findViewById<Button>(R.id.button5)
        val batteryButton = findViewById<RelativeLayout>(R.id.rectangle6)
        val button6 = findViewById<Button>(R.id.button6)
        val batteryDataText = findViewById<TextView>(R.id.batterydataText)
        val batteryDataNumbers = findViewById<TextView>(R.id.batterydataNumbers)
        val distanceDataText = findViewById<TextView>(R.id.distancedataText)
        val distanceDataNumbers = findViewById<TextView>(R.id.distancedataNumbers)
        val distanceDataKm = findViewById<TextView>(R.id.distancedatakm)
        val timeDataText = findViewById<TextView>(R.id.timedataText)
        val timeDataNumbers = findViewById<TextView>(R.id.timedataNumbers)

        exitIcon.setOnClickListener {
            finishAffinity()
        }

        button1.setOnClickListener {
            val intent = Intent(this, StadisticsScreen::class.java)
            startActivity(intent)
        }

        button2.setOnClickListener {
            val intent = Intent(this, HistoryScreen::class.java)
            startActivity(intent)
        }

        button3.setOnClickListener {
            val intent = Intent(this, ServicesScreen::class.java)
            startActivity(intent)
        }

        button4.setOnClickListener {
            val intent = Intent(this, SettingsScreen::class.java)
            startActivity(intent)
        }

        button5.setOnClickListener {
            val intent = Intent(this, MapScreen1::class.java)
            startActivity(intent)
        }

        button6.setOnClickListener {
            val intent = Intent(this, BatteryScreen::class.java)
            startActivity(intent)
        }

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

                        @SuppressLint("DefaultLocale")
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            // Cambiar la visibilidad a VISIBLE antes de iniciar la animación de desvanecimiento
                            //timeTextView
                            timeTextView.visibility = View.VISIBLE
                            timeTextView.alpha = 0f // Iniciar invisible
                            timeTextView.animate().alpha(1f).setDuration(500).setStartDelay(900).start()

                            exitIcon.visibility = View.VISIBLE
                            exitIcon.alpha = 0f // Iniciar invisible
                            exitIcon.animate().alpha(1f).setDuration(500).setStartDelay(900).start()

                            connectionStatusValidation.visibility = View.VISIBLE

                            //dataRectangle
                            dataRectangle.visibility = View.VISIBLE
                            dataRectangle.alpha = 0f // Iniciar invisible
                            dataRectangle.animate().alpha(1f).setDuration(500).setStartDelay(1000).start()

                            batteryDataText.visibility = View.VISIBLE
                            batteryDataText.alpha = 0f
                            batteryDataText.animate().alpha(1f).setDuration(500).setStartDelay(1200).start()

                            batteryDataNumbers.visibility = View.VISIBLE
                            batteryDataNumbers.alpha = 0f
                            batteryDataNumbers.animate().alpha(1f).setDuration(500).setStartDelay(1300).start()

                            distanceDataText.visibility = View.VISIBLE
                            distanceDataText.alpha = 0f
                            distanceDataText.animate().alpha(1f).setDuration(500).setStartDelay(1400).start()

                            distanceDataNumbers.visibility = View.VISIBLE
                            distanceDataNumbers.alpha = 0f
                            distanceDataNumbers.animate().alpha(1f).setDuration(500).setStartDelay(1500).start()

                            distanceDataKm.visibility = View.VISIBLE
                            distanceDataKm.alpha = 0f
                            distanceDataKm.animate().alpha(1f).setDuration(500).setStartDelay(1500).start()

                            timeDataText.visibility = View.VISIBLE
                            timeDataText.alpha = 0f
                            timeDataText.animate().alpha(1f).setDuration(500).setStartDelay(1600).start()

                            timeDataNumbers.visibility = View.VISIBLE
                            timeDataNumbers.alpha = 0f
                            timeDataNumbers.animate().alpha(1f).setDuration(500).setStartDelay(1700).start()

                            //rectangle1
                            stadisticsButton.visibility = View.VISIBLE
                            stadisticsButton.alpha = 0f // Iniciar invisible
                            stadisticsButton.animate().alpha(1f).setDuration(500).setStartDelay(1100).start()
                            //rectangle2
                            historyButton.visibility = View.VISIBLE
                            historyButton.alpha = 0f // Iniciar invisible
                            historyButton.animate().alpha(1f).setDuration(500).setStartDelay(1200).start()
                            //rectangle3
                            servicesButton.visibility = View.VISIBLE
                            servicesButton.alpha = 0f // Iniciar invisible
                            servicesButton.animate().alpha(1f).setDuration(500).setStartDelay(1300).start()
                            //rectangle4
                            settingsButton.visibility = View.VISIBLE
                            settingsButton.alpha = 0f // Iniciar invisible
                            settingsButton.animate().alpha(1f).setDuration(500).setStartDelay(1400).start()
                            //rectangle5
                            mapButton.visibility = View.VISIBLE
                            mapButton.alpha = 0f // Iniciar invisible
                            mapButton.animate().alpha(1f).setDuration(500).setStartDelay(1500).start()
                            //rectangle6
                            batteryButton.visibility = View.VISIBLE
                            batteryButton.alpha = 0f // Iniciar invisible
                            batteryButton.animate().alpha(1f).setDuration(500).setStartDelay(1600).start()

                            // Animaciones adicionales para los otros elementos
                            speedMeterNumbers.visibility = View.VISIBLE
                            speedMeterMetric.visibility = View.VISIBLE
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
                                    startSpeedTracking()
                                } else {
                                    connectionStatusFalse.visibility = View.VISIBLE
                                    connectionStatusFalse.alpha = 0f
                                    connectionStatusFalse.animate().alpha(1f).setDuration(500).start() // Mostrar la conexión fallida
                                    startSpeedTracking()
                                }
                            }, 1500)

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

        // Iniciar la actualización del tiempo
        handler.post(updateTimeRunnable)
    }
    private fun startSpeedTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        isTrackingSpeed = true

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, object : LocationListener {
            @SuppressLint("SetTextI18n")
            override fun onLocationChanged(location: Location) {
                val speedKmh = location.speed * 3.6 // Convierte m/s a km/h
                val speedMeterNumbers = findViewById<TextView>(R.id.speedMeterNumbers)

                // Muestra la velocidad
                speedMeterNumbers.text = String.format("%.1f", speedKmh)

                // Verifica si la velocidad ha cambiado de 0
                if (speedKmh > 0) {
                    if (!isSpeedNonZero) {
                        // Registra el tiempo cuando la velocidad cambia de 0
                        speedChangeTime = System.currentTimeMillis()
                        isSpeedNonZero = true
                        isSpeedZeroMoreThanFiveMinutes = false // Restablece el estado
                    }
                } else {
                    if (isSpeedNonZero) {
                        // Registra el tiempo cuando la velocidad se vuelve 0
                        zeroSpeedStartTime = System.currentTimeMillis()
                        isSpeedNonZero = false
                    }
                }

                // Calcula la distancia recorrida si lastLocation no es nulo
                lastLocation?.let {
                    val distance = it.distanceTo(location)
                    totalDistance += distance
                    val distanceDataNumbers = findViewById<TextView>(R.id.distancedataNumbers)
                    distanceDataNumbers.text = String.format("%.2f", totalDistance / 1000)
                }

                // Actualiza la última ubicación
                lastLocation = location

                // Calcula el tiempo transcurrido
                if (isSpeedNonZero && speedChangeTime > 0) {
                    val elapsedTime = System.currentTimeMillis() - speedChangeTime
                    val minutes = (elapsedTime / 1000) / 60
                    val seconds = (elapsedTime / 1000) % 60
                    timeDataNumbers.text = String.format("%02d:%02d", minutes, seconds)
                } else if (!isSpeedNonZero) {
                    // Calcula el tiempo transcurrido desde que la velocidad se volvió 0
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - zeroSpeedStartTime > 5 * 60 * 1000) { // 5 minutos en milisegundos
                        timeDataNumbers.text = "00:00"
                        isSpeedZeroMoreThanFiveMinutes = true
                    } else {
                        val elapsedTime = currentTime - zeroSpeedStartTime
                        val minutes = (elapsedTime / 1000) / 60
                        val seconds = (elapsedTime / 1000) % 60
                        timeDataNumbers.text = String.format("%02d:%02d", minutes, seconds)
                    }
                } else {
                    timeDataNumbers.text = "00:00" // Muestra 00:00 si no se ha registrado el tiempo de velocidad
                }
            }


            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        })
    }
    private fun updateTime() {
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        timeTextView.text = currentTime
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
        locationManager.removeUpdates { } // Detener actualizaciones de GPS
    }
}