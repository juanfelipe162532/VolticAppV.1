package com.lhdevelopment.voltic

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView

@Suppress("DEPRECATION")
class BluetoothConnection1 : ComponentActivity() {

    private lateinit var logoutDialog: RelativeLayout
    private lateinit var logoutButton: Button
    private lateinit var cancelButton: Button
    private lateinit var permissionsButton: Button
    private var isLogoutDialogVisible = false
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_PERMISSIONS = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar si es la primera vez que el usuario ingresa
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", false)

        if (!isFirstRun) {
            navigateToBluetoothConnection2()
            return
        }

        setContentView(R.layout.bluetoothconnection1)

        val lottieAnimationView: LottieAnimationView = findViewById(R.id.lottieAnimationView)
        lottieAnimationView.setAnimation(R.raw.bluetoothanim1)
        lottieAnimationView.playAnimation()

        logoutDialog = findViewById(R.id.logout_dialog)
        logoutButton = findViewById(R.id.button_logout)
        cancelButton = findViewById(R.id.button_cancel)
        permissionsButton = findViewById(R.id.permissionsButton)

        logoutButton.setOnClickListener {
            logoutUser()
        }

        cancelButton.setOnClickListener {
            hideLogoutDialog()
        }

        permissionsButton.setOnClickListener {
            requestPermissions()
        }

        logoutDialog.visibility = View.GONE

     /*   // Actualizar bandera de primera ejecución
        val editor = sharedPreferences.edit()
        editor.putBoolean("isFirstRun", false)
        editor.apply()
        */
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isLogoutDialogVisible) {
            super.onBackPressed()
        } else {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        logoutDialog.visibility = View.VISIBLE
        val animation = AnimationUtils.loadAnimation(this, R.anim.dialog_animation)
        logoutDialog.startAnimation(animation)
        isLogoutDialogVisible = true
    }

    private fun hideLogoutDialog() {
        logoutDialog.visibility = View.GONE
        isLogoutDialogVisible = false
    }

    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.remove("username")
        editor.remove("password")
        editor.apply()

        val intent = Intent(this, LoginScreen::class.java)
        startActivity(intent)
        finish()
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun requestPermissions() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (!hasPermissions(permissions.toTypedArray())) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permisos denegados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth habilitado", Toast.LENGTH_SHORT).show()
                navigateToBluetoothConnection2()
            } else {
                Toast.makeText(this, "Bluetooth no habilitado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToBluetoothConnection2() {
        val intent = Intent(this, BluetoothConnection2::class.java)
        startActivity(intent)
        finish()
    }
}









