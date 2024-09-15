package com.lhdevelopment.voltic

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import java.util.Locale
import java.util.Date
import java.util.Timer
import java.util.TimerTask
import java.text.SimpleDateFormat
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import android.view.WindowManager
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.util.Calendar
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import android.media.MediaPlayer


@Suppress("DEPRECATION", "RedundantSamConstructor")
class MainPanel : ComponentActivity() {

    private var flashTimer: Timer? = null
    private var isSoundPlaying = false
    private var isImageVisible = false
    private var mediaPlayer: MediaPlayer? = null
    private val viewModel: MainPanelViewModel by viewModels()
    private var isCalculating = false
    private lateinit var calculationThread: Thread
    private var startTime: Long = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var locationRequest: LocationRequest
    private lateinit var timeTextView: TextView
    private lateinit var speedWarning: ImageView
    private lateinit var batteryDataNumbers: TextView
    private val handler = Handler()
    private lateinit var maxSpeed: RelativeLayout
    private var lastLocation: Location? = null
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000) // Actualiza cada segundo
        }
    }

    private val dataHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            loadAndDisplayData()
            // Ejecutar nuevamente después de 1 segundo (1000 ms)
            dataHandler.postDelayed(this, 1000)
        }
    }


    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dataHandler.post(updateRunnable)

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

        batteryDataNumbers = findViewById(R.id.batterydataNumbers)
        val numbers = intent.getIntArrayExtra("NUMBERS") ?: intArrayOf()
        val numbersText = numbers.joinToString(", ")
        batteryDataNumbers.text = numbersText

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager


        locationRequest = LocationRequest.create().apply {
            interval = 200
            fastestInterval = 100
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        timeTextView = findViewById(R.id.timeTextView)
        maxSpeed = findViewById(R.id.maxSpeedIcon)
        speedWarning = findViewById(R.id.speedWarning)

        startLocationUpdates()

        val exitIcon = findViewById<ImageView>(R.id.exitIcon)
        val halfMoonImageView = findViewById<ClippingImageView>(R.id.speedMeterShape)
        val speedMeterNumbers2 = findViewById<ImageView>(R.id.speedMeter_numbers)
        val speedMeterMetric = findViewById<TextView>(R.id.speedMeterMetric)
        val connectionStatusValidation =
            findViewById<RelativeLayout>(R.id.connectionStatusValidation)
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
        val distanceDataText = findViewById<TextView>(R.id.distancedataText)
        val distanceDataKm = findViewById<TextView>(R.id.distancedatakm)
        val timeDataText = findViewById<TextView>(R.id.timedataText)
        val distanceDataNumbers = findViewById<TextView>(R.id.distancedataNumbers)
        val timeDataNumbers = findViewById<TextView>(R.id.timedataNumbers)
        val speedMeterNumbers = findViewById<TextView>(R.id.speedMeterNumbers)

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

        viewModel.distance.observe(this, Observer { distance ->
            distanceDataNumbers.text = String.format("%.1f", distance)
        })

        viewModel.speed.observe(this, Observer { speed ->
            speedMeterNumbers.text = String.format("%d", speed.toInt())
        })


        // Obtener si la conexión Bluetooth fue exitosa desde el Intent
        val isBluetoothConnected = intent.getBooleanExtra("EXTRA_BT_CONNECTED", false)


        // Usar un ViewTreeObserver para asegurar que la vista esté medida antes de iniciar la animación
        halfMoonImageView.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // Eliminar el listener para evitar múltiples llamadas
                    halfMoonImageView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    // Crear la animación de recorte
                    val animator =
                        ObjectAnimator.ofFloat(halfMoonImageView, "revealProgress", 0f, 1f).apply {
                            duration = 1000 // Duración de la animación en milisegundos
                            interpolator =
                                AccelerateDecelerateInterpolator() // Interpolador para animación suave
                            startDelay = 500 // Retardo antes de que comience la animación
                        }

                    // Iniciar la animación de recorte
                    animator.start()

                    // Cargar la animación de desvanecimiento
                    val fadeInAnimation =
                        AnimationUtils.loadAnimation(this@MainPanel, R.anim.fade_in)

                    // Esperar a que la animación del ClippingImageView termine antes de mostrar los otros objetos
                    animator.addListener(object : android.animation.Animator.AnimatorListener {
                        override fun onAnimationStart(animation: android.animation.Animator) {}

                        @SuppressLint("DefaultLocale")
                        override fun onAnimationEnd(animation: android.animation.Animator) {
                            // Cambiar la visibilidad a VISIBLE antes de iniciar la animación de desvanecimiento
                            //timeTextView
                            timeTextView.visibility = View.VISIBLE
                            timeTextView.alpha = 0f // Iniciar invisible
                            timeTextView.animate().alpha(1f).setDuration(500).setStartDelay(800)
                                .start()

                            maxSpeed.visibility = View.VISIBLE
                            maxSpeed.alpha = 0f // Iniciar invisible
                            maxSpeed.animate().alpha(1f).setDuration(500).setStartDelay(900).start()

                            exitIcon.visibility = View.VISIBLE
                            exitIcon.alpha = 0f // Iniciar invisible
                            exitIcon.animate().alpha(1f).setDuration(500).setStartDelay(1000)
                                .start()

                            connectionStatusValidation.visibility = View.VISIBLE

                            //dataRectangle
                            dataRectangle.visibility = View.VISIBLE
                            dataRectangle.alpha = 0f // Iniciar invisible
                            dataRectangle.animate().alpha(1f).setDuration(500).setStartDelay(1000)
                                .start()

                            batteryDataText.visibility = View.VISIBLE
                            batteryDataText.alpha = 0f
                            batteryDataText.animate().alpha(1f).setDuration(500).setStartDelay(1200)
                                .start()

                            batteryDataNumbers.visibility = View.VISIBLE
                            batteryDataNumbers.alpha = 0f
                            batteryDataNumbers.animate().alpha(1f).setDuration(500)
                                .setStartDelay(1300).start()

                            distanceDataText.visibility = View.VISIBLE
                            distanceDataText.alpha = 0f
                            distanceDataText.animate().alpha(1f).setDuration(500)
                                .setStartDelay(1400).start()

                            distanceDataNumbers.visibility = View.VISIBLE
                            distanceDataNumbers.alpha = 0f
                            distanceDataNumbers.animate().alpha(1f).setDuration(500)
                                .setStartDelay(1500).start()

                            distanceDataKm.visibility = View.VISIBLE
                            distanceDataKm.alpha = 0f
                            distanceDataKm.animate().alpha(1f).setDuration(500).setStartDelay(1500)
                                .start()

                            timeDataText.visibility = View.VISIBLE
                            timeDataText.alpha = 0f
                            timeDataText.animate().alpha(1f).setDuration(500).setStartDelay(1600)
                                .start()

                            timeDataNumbers.visibility = View.VISIBLE
                            timeDataNumbers.alpha = 0f
                            timeDataNumbers.animate().alpha(1f).setDuration(500).setStartDelay(1700)
                                .start()

                            //rectangle1
                            stadisticsButton.visibility = View.VISIBLE
                            stadisticsButton.alpha = 0f // Iniciar invisible
                            stadisticsButton.animate().alpha(1f).setDuration(500)
                                .setStartDelay(1100).start()
                            //rectangle2
                            historyButton.visibility = View.VISIBLE
                            historyButton.alpha = 0f // Iniciar invisible
                            historyButton.animate().alpha(1f).setDuration(500).setStartDelay(1200)
                                .start()
                            //rectangle3
                            servicesButton.visibility = View.VISIBLE
                            servicesButton.alpha = 0f // Iniciar invisible
                            servicesButton.animate().alpha(1f).setDuration(500).setStartDelay(1300)
                                .start()
                            //rectangle4
                            settingsButton.visibility = View.VISIBLE
                            settingsButton.alpha = 0f // Iniciar invisible
                            settingsButton.animate().alpha(1f).setDuration(500).setStartDelay(1400)
                                .start()
                            //rectangle5
                            mapButton.visibility = View.VISIBLE
                            mapButton.alpha = 0f // Iniciar invisible
                            mapButton.animate().alpha(1f).setDuration(500).setStartDelay(1500)
                                .start()
                            //rectangle6
                            batteryButton.visibility = View.VISIBLE
                            batteryButton.alpha = 0f // Iniciar invisible
                            batteryButton.animate().alpha(1f).setDuration(500).setStartDelay(1600)
                                .start()

                            // Animaciones adicionales para los otros elementos
                            speedMeterNumbers.visibility = View.VISIBLE
                            speedMeterMetric.visibility = View.VISIBLE
                            speedMeterNumbers2.visibility = View.VISIBLE

                            // Aplicar la animación de entrada
                            speedMeterNumbers.startAnimation(fadeInAnimation)
                            speedMeterMetric.startAnimation(fadeInAnimation)
                            speedMeterNumbers2.startAnimation(fadeInAnimation)

                            // Animación de validación: hacer girar el icono de validación
                            val rotationAnimation =
                                ObjectAnimator.ofFloat(validationIcon, "rotation", 0f, 360f).apply {
                                    duration = 1000 // Duración de la animación de rotación
                                    repeatCount = ObjectAnimator.INFINITE
                                    interpolator = AccelerateDecelerateInterpolator()
                                    start()
                                }

                            // Validación de conexión Bluetooth
                            validationIcon.postDelayed({
                                rotationAnimation.cancel() // Detener la animación de rotación
                                connectionStatusValidation.visibility =
                                    View.GONE // Ocultar la validación

                                if (isBluetoothConnected) {
                                    connectionStatusTrue.visibility =
                                        View.VISIBLE // Mostrar la conexión exitosa
                                    startSpeedTracking()
                                    loadAndDisplayData()
                                } else {
                                    connectionStatusFalse.visibility = View.VISIBLE
                                    connectionStatusFalse.alpha = 0f
                                    connectionStatusFalse.animate().alpha(1f).setDuration(500)
                                        .start() // Mostrar la conexión fallida
                                }
                            }, 1500)
                        }

                        override fun onAnimationCancel(animation: android.animation.Animator) {}

                        override fun onAnimationRepeat(animation: android.animation.Animator) {}
                    })
                }
            }
        )
        handler.post(updateTimeRunnable)
    }

    private fun updateTime() {
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        timeTextView.text = currentTime
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Si no se tienen permisos, solicitarlos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        // Usar la instancia de locationRequest que has creado
        fusedLocationClient.requestLocationUpdates(
            locationRequest, // Usar la variable de instancia
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val newLocation = locationResult.lastLocation
                    if (newLocation != null) {
                        updateLocationData(newLocation)
                    }
                }
            },
            null
        )
    }

    private fun playWarningSound() {
        if (isSoundPlaying) return // Si el sonido ya está en reproducción, no hacer nada

        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.warning_sound)
            mediaPlayer?.apply {
                setOnCompletionListener {
                    release() // Liberar recursos cuando termine la reproducción
                    isSoundPlaying = false // Marcar que el sonido ha terminado de reproducirse
                }
                start()
                isSoundPlaying = true // Marcar que el sonido está en reproducción
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkSpeed(speed: Int) {
        if (speed > 40) { // Ajusta el umbral según tus necesidades
            startFlashEffect(speedWarning, 500L, 300L)
            playWarningSound()
        } else {
            // Si la velocidad es menor al umbral, detener el sonido si está en reproducción
            if (isSoundPlaying) {
                stopFlashEffect()
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                isSoundPlaying = false
            }
        }
    }

    private fun startFlashEffect(
        speedWarning: ImageView,
        flashInterval: Long,
        visibilityDuration: Long
    ) {
        this.speedWarning = speedWarning

        // Cancelar cualquier Timer anterior, si existe
        stopFlashEffect()

        // Crear un nuevo Timer
        flashTimer = Timer()

        // Tarea para mostrar la imagen
        val showTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    speedWarning.visibility = View.VISIBLE
                    isImageVisible = true

                    // Programar la tarea de ocultar después de 'visibilityDuration'
                    flashTimer?.schedule(object : TimerTask() {
                        override fun run() {
                            handler.post {
                                speedWarning.visibility = View.INVISIBLE
                                isImageVisible = false
                            }
                        }
                    }, visibilityDuration)
                }
            }
        }

        // Iniciar el Timer para ejecutar la tarea repetidamente con el intervalo definido
        flashTimer?.schedule(showTask, 0, flashInterval + visibilityDuration)
    }

    private fun stopFlashEffect() {
        // Cancelar el Timer si está en uso
        flashTimer?.cancel()
        flashTimer = null

        // Asegurarse de que la imagen esté visible o en el estado deseado
        speedWarning.visibility = View.GONE
    }

    @SuppressLint("DefaultLocale")
    private fun updateLocationData(newLocation: Location) {
        if (lastLocation != null) {
            // Calcular la distancia y actualizar
            val distance = lastLocation!!.distanceTo(newLocation)
            viewModel.updateDistance(distance)

        }

        // Guardar la nueva ubicación
        lastLocation = newLocation
        val speed = (newLocation.speed * 3.6).toFloat() // convertir m/s a km/h
        viewModel.updateSpeed(speed)

        checkSpeed(speed.toInt())
    }

    private fun startCalculationThread() {
        calculationThread = Thread {
            while (isCalculating) {
                val elapsedTime = System.currentTimeMillis() - startTime
                runOnUiThread {
                    val timeDataNumbers = findViewById<TextView>(R.id.timedataNumbers)
                    timeDataNumbers.text = formatTime(elapsedTime)
                }
                Thread.sleep(1000) // Actualizar cada segundo
            }
        }
        calculationThread.start()
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }


    private fun startSpeedTracking() {
        startTime = System.currentTimeMillis()
        isCalculating = true
        startCalculationThread() // Iniciar el thread de tiempo
        startLocationUpdates() // Iniciar la actualización de ubicación
    }

    private fun loadAndDisplayData() {
        // Recuperar datos de SharedPreferences
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        //val voltage = sharedPreferences.getString("voltage", "0.0") ?: "0.0"
        val chargePercentage = sharedPreferences.getString("chargePercentage", "0.0") ?: "0.0"


        batteryDataNumbers.text = "$chargePercentage %"


    }
}