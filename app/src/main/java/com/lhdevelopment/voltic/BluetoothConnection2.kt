@file:Suppress("DEPRECATION")

package com.lhdevelopment.voltic

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.util.regex.Pattern
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.IOException
import java.util.Calendar
import java.util.UUID
import kotlin.random.Random
import android.content.res.Resources
import kotlin.math.cos
import android.app.ActivityOptions
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.sin
import kotlin.math.sqrt

class BluetoothConnection2 : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }
    private lateinit var devicesContainer: RelativeLayout
    private var bluetoothSocket: BluetoothSocket? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted && coarseLocationGranted) {
            if (!bluetoothAdapter?.isEnabled!!) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
            startDiscovery()
        } else {
            Toast.makeText(this, "Permisos de ubicación necesarios no concedidos", Toast.LENGTH_SHORT).show()
        }
    }


    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            // Si se encuentra un dispositivo Bluetooth
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Obtiene el dispositivo Bluetooth encontrado desde el Intent
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                device?.let { bluetoothDevice ->
                    // Comenta la parte que filtra por UUID para añadir todos los dispositivos
                    // val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                    // val deviceUuids = bluetoothDevice.uuids
                    // val hasRequiredUuid = deviceUuids?.any { it.uuid == uuid } ?: false

                    // Si no deseas filtrar por UUID, añade el dispositivo directamente
                    // Aquí añades el dispositivo sin importar su UUID
                    addDeviceToLayout(bluetoothDevice) // Añade todos los dispositivos sin filtrar
                }
            }
            // Si la búsqueda de dispositivos finalizó
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                // Reinicia la búsqueda de dispositivos
                startDiscovery()
            }
        }
    }


    private val placedDevices = mutableListOf<Pair<Int, Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("voltage", "0.0 V")
        editor.putString("chargePercentage", "0.0 %")
        editor.apply()

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

        setContentView(R.layout.bluetoothconnection2)

        Log.d("BluetoothConnection2", "onCreate")
        val lottieAnimationView: LottieAnimationView = findViewById(R.id.lottieAnimationView)
        lottieAnimationView.setAnimation(R.raw.bluetoothanim2)
        lottieAnimationView.playAnimation()

        val lottieAnimationView2: LottieAnimationView = findViewById(R.id.lottieAnimationView2)
        lottieAnimationView2.setAnimation(R.raw.bluetoothanim3)
        lottieAnimationView2.playAnimation()

        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        lottieAnimationView2.layoutParams = layoutParams

        devicesContainer = findViewById(R.id.devices_container)


        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth no está disponible en este dispositivo", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                startDiscovery()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startDiscovery() {
        Log.d("BluetoothConnection2", "Iniciando descubrimiento de Bluetooth")
        bluetoothAdapter?.startDiscovery()
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    @SuppressLint("MissingPermission")
    private fun addDeviceToLayout(device: BluetoothDevice) {
        val deviceView = layoutInflater.inflate(R.layout.device_item, devicesContainer, false)
        val deviceNameTextView = deviceView.findViewById<TextView>(R.id.device_name)
        val deviceIcon = deviceView.findViewById<ImageView>(R.id.device_icon)

        deviceNameTextView.text = device.name ?: "Desconocido"
        deviceIcon.setImageResource(getIconForDevice(device.name ?: ""))

        val centerX = devicesContainer.width / 2
        val centerY = devicesContainer.height / 2

        val scale = Resources.getSystem().displayMetrics.density
        val minDistance = (20 * scale).toInt()
        val maxDistance = (50 * scale).toInt()

        var x: Int
        var y: Int
        var validPosition: Boolean

        // Generar una posición válida sin solapamiento
        do {
            val angle = Random.nextDouble(0.0, 2 * Math.PI)
            val distance = Random.nextInt(minDistance, maxDistance)

            x = centerX + (distance * cos(angle)).toInt()
            y = centerY + (distance * sin(angle)).toInt()

            validPosition = true
            for ((placedX, placedY) in placedDevices) {
                val dx = x - placedX
                val dy = y - placedY
                val distanceBetweenDevices = sqrt((dx * dx + dy * dy).toDouble()).toInt()

                // Verifica si la distancia es menor que el tamaño mínimo para evitar solapamiento
                if (distanceBetweenDevices < minDistance) {
                    validPosition = false
                    break
                }
            }
        } while (!validPosition)

        // Guardar la posición válida
        placedDevices.add(Pair(x, y))

        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.leftMargin = x
        layoutParams.topMargin = y

        deviceView.layoutParams = layoutParams
        deviceView.setOnClickListener {
            showDeviceInfoBottomSheet(device) // Muestra el BottomSheetDialog
        }

        devicesContainer.addView(deviceView)
    }

    @SuppressLint("MissingPermission", "InflateParams")
    private fun showDeviceInfoBottomSheet(device: BluetoothDevice) {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_device_info, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)

        val deviceIcon = bottomSheetView.findViewById<ImageView>(R.id.device_icon)
        val deviceName = bottomSheetView.findViewById<TextView>(R.id.device_name)
        val cancelButton = bottomSheetView.findViewById<Button>(R.id.cancel_button)

        deviceIcon.setImageResource(getIconForDevice(device.name ?: ""))
        deviceName.text = device.name ?: "Desconocido"

        // Mostrar el BottomSheetDialog
        bottomSheetDialog.show()

        // Iniciar la conexión inmediatamente después de mostrar el BottomSheetDialog
        connectToDevice(device)

        // Configurar el botón Cancelar
        cancelButton.setOnClickListener {
            cancelConnection() // Cancelar la conexión al presionar "Cancelar"
            bottomSheetDialog.dismiss() // Cerrar el BottomSheetDialog
        }

        // Configurar el listener para cancelar la conexión si se cierra el BottomSheetDialog
        bottomSheetDialog.setOnDismissListener {
            // Opcional: Si deseas realizar alguna acción cuando el BottomSheet se cierre,
            // como verificar si la conexión se realizó o canceló.
        }
    }



    private fun getIconForDevice(name: String): Int {
        return when {
            name.contains("laptop", true) -> R.drawable.ic_laptop
            name.contains("tv", true) -> R.drawable.ic_tv
            name.contains("audio", true) -> R.drawable.ic_audio
            else -> R.drawable.ic_bluetooth
        }
    }


    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        // El UUID utilizado para la conexión (ya no es necesario filtrar por él)
        val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        // Comentar la verificación del UUID para conectar a cualquier dispositivo
        // val deviceUuids = device.uuids
        // val hasRequiredUuid = deviceUuids?.any { it.uuid == uuid } ?: false

        // Remover la verificación del UUID para conectar sin importar si lo tiene
        // if (hasRequiredUuid) {
        Thread {
            try {
                // Cancelar la búsqueda de otros dispositivos
                bluetoothAdapter?.cancelDiscovery()
                Log.d("BluetoothConnection2", "Conectando a ${device.name} con UUID $uuid")

                // Intentar crear y conectar el socket Bluetooth
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()

                // Notifica al usuario que la conexión fue exitosa
                runOnUiThread {
                    Toast.makeText(this, "Conectado a ${device.name}", Toast.LENGTH_SHORT).show()
                    navigateToMainPanel(true) // Navega al panel principal después de la conexión
                }

                // Lee los datos del dispositivo conectado
                readDataFromDevice(bluetoothSocket)
            } catch (e: IOException) {
                // En caso de error al conectar
                Log.e("BluetoothConnection2", "Error al conectar con ${device.name}", e)
                runOnUiThread {
                    Toast.makeText(this, "Error al conectar con ${device.name}", Toast.LENGTH_SHORT).show()
                }
                try {
                    // Cierra el socket en caso de error
                    bluetoothSocket?.close()
                } catch (closeException: IOException) {
                    Log.e("BluetoothConnection2", "Error al cerrar el socket", closeException)
                }
            }
        }.start()
        // } else {
        //     Log.d("BluetoothConnection2", "El dispositivo ${device.name} no tiene el UUID requerido.")
        //     Toast.makeText(this, "El dispositivo ${device.name} no tiene el UUID requerido.", Toast.LENGTH_SHORT).show()
        // }
    }


    private fun cancelConnection() {
        bluetoothSocket?.let {
            try {
                it.close()
                bluetoothSocket = null
                runOnUiThread {
                    Toast.makeText(this, "Conexión cancelada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Log.e("BluetoothConnection2", "Error al cancelar la conexión", e)
                runOnUiThread {
                    Toast.makeText(this, "Error al cancelar la conexión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun parseBluetoothData(data: String) {
        // Patrón para buscar números enteros
        val pattern = Pattern.compile("\\d+")
        val matcher = pattern.matcher(data)

        // Variables para almacenar los números parseados
        var voltage: Int? = null
        var percentage: Int? = null
        var voltageFound = false
        var percentageFound = false

        // Encuentra y parsea los números
        while (matcher.find()) {
            val number = matcher.group()
            try {
                val value = number.toInt() // Parseamos el número como Int
                if (!voltageFound) {
                    voltage = value  // El primer número es el voltaje
                    voltageFound = true
                } else if (!percentageFound) {
                    percentage = value // El segundo número es el porcentaje
                    percentageFound = true
                }
            } catch (e: NumberFormatException) {
                Log.d("BluetoothConnection2", "Error al parsear número: $number")
            }
        }

        // Verifica si ambos números fueron encontrados
        if (voltage != null && percentage != null) {
            Log.d("BluetoothConnection2", "Voltaje medido: $voltage V")
            Log.d("BluetoothConnection2", "Porcentaje de carga: $percentage %")

            // Actualiza SharedPreferences
            val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            with(sharedPreferences.edit()) {
                putInt("voltage", voltage)
                putInt("chargePercentage", percentage)
                apply()
            }
        } else {
            Log.d("BluetoothConnection2", "No se pudieron parsear números del mensaje: $data")
        }
    }


    private fun readDataFromDevice(bluetoothSocket: BluetoothSocket?) {
        bluetoothSocket?.let { socket ->
            try {
                // InputStream para leer los datos del dispositivo Bluetooth
                val inputStream = socket.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))

                // Bucle para leer los datos del dispositivo continuamente
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    // Llama a la función parseBluetoothData para procesar los datos recibidos
                    parseBluetoothData(line!!)
                }

            } catch (e: IOException) {
                Log.d("BluetoothConnection2", "Error al leer datos desde el dispositivo: ${e.message}")
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth habilitado", Toast.LENGTH_SHORT).show()
                startDiscovery()
            } else {
                Toast.makeText(this, "Bluetooth no habilitado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMainPanel(isConnected: Boolean) {
        val intent = Intent(this, MainPanel::class.java)
        intent.putExtra("EXTRA_BT_CONNECTED", isConnected)

        // Obtén el contexto de las animaciones de la transición
        val options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)

        startActivity(intent, options.toBundle())
        finish()
    }



    companion object {
        private const val REQUEST_ENABLE_BT = 1
    }
}