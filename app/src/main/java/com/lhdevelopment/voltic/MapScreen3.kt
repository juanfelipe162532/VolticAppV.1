package com.lhdevelopment.voltic

import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import android.os.Looper
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.content.res.Resources
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MapStyleOptions
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar

@Suppress("DEPRECATION", "ControlFlowWithEmptyBody")
class MapScreen3 : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val apiKey = "AIzaSyDLEaBvnGVCfUSa0dE_AoKPpjp57mWPNkg" // Reemplaza con tu clave de API
    private lateinit var loadingScreen: View
    private var startMarker: Marker? = null
    private var hasCenteredCamera = false
    private val handler = Handler()
    private val updateInterval: Long = 100
    private val runnable = object : Runnable {
        override fun run() {
            updateCameraPosition()
            handler.postDelayed(this, updateInterval)
        }
    }

    private lateinit var routePolyline: Polyline
    private val routePoints = mutableListOf<LatLng>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureActivityTheme()
        setContentView(R.layout.mapscreen3)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        loadingScreen = findViewById(R.id.route_creation_loading_screen)
        loadingScreen.visibility = View.VISIBLE

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        val backButton: Button = findViewById(R.id.backButton)
        val startRouteButton: Button = findViewById(R.id.startRouteButton)

        LocationProvider.init(this)

        startRouteButton.setOnClickListener {
            handler.post(runnable)
            start3DRouteView()
        }

        mapFragment.getMapAsync(this)


        backButton.setOnClickListener {
            val intent = Intent(this, MapScreen2::class.java)
            startActivity(intent)
        }
    }

    private fun configureActivityTheme() {
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
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        applyMapStyleBasedOnTheme()
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        showRoute(this)
        loadingScreen.visibility = View.GONE
        startLocationUpdates()
    }

    private fun applyMapStyleBasedOnTheme() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val styleResId = if (currentHour >= 18 || currentHour < 6) {
            R.raw.map_night_style // Modo noche
        } else {
            null // Modo día (predeterminado)
        }

        try {
            val success = if (styleResId != null) {
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, styleResId))
            } else {
                mMap.setMapStyle(null)
            }
            if (!success) {
                // Manejo de error si el estilo no se pudo aplicar
            }
        } catch (e: Resources.NotFoundException) {
            // Manejo de excepción si el archivo no se encuentra
        }
    }

    private fun showRoute(context: Context) {
        val sharedPreferences = context.getSharedPreferences("MapPreferences", Context.MODE_PRIVATE)
        val startLatitude = sharedPreferences.getString("START_LATITUDE", null)?.toDouble()
        val startLongitude = sharedPreferences.getString("START_LONGITUDE", null)?.toDouble()
        val endLatitude = sharedPreferences.getString("END_LATITUDE", null)?.toDouble()
        val endLongitude = sharedPreferences.getString("END_LONGITUDE", null)?.toDouble()

        if (startLatitude != null && startLongitude != null && endLatitude != null && endLongitude != null) {
            val startLocation = LatLng(startLatitude, startLongitude)
            val endLocation = LatLng(endLatitude, endLongitude)

            // Añade marcador de inicio y fin
            addStartMarker(startLocation)
            mMap.addMarker(MarkerOptions().position(endLocation).title("Punto de Fin"))

            // Muestra la ruta inicial
            updateRoute(startLocation)
        } else {
            Log.d("RouteDisplay", "No se encontraron coordenadas guardadas para la ruta.")
        }
    }

    // Actualización de la ruta en tiempo real, llamado desde el LocationCallback
    private fun updateRoute(currentLocation: LatLng) {
        val sharedPreferences = getSharedPreferences("MapPreferences", Context.MODE_PRIVATE)
        val endLatitude = sharedPreferences.getString("END_LATITUDE", null)?.toDouble()
        val endLongitude = sharedPreferences.getString("END_LONGITUDE", null)?.toDouble()

        if (endLatitude != null && endLongitude != null) {
            val endLocation = LatLng(endLatitude, endLongitude)

            // Llama a la API de direcciones para recalcular la ruta
            val retrofit = Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(DirectionsService::class.java)
            val call = service.getDirections(
                "${currentLocation.latitude},${currentLocation.longitude}",
                "${endLatitude},${endLongitude}",
                apiKey
            )

            call.enqueue(object : retrofit2.Callback<DirectionsResponse> {
                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: retrofit2.Response<DirectionsResponse>
                ) {
                    if (response.isSuccessful) {
                        val polyline = response.body()?.routes?.firstOrNull()?.overviewPolyline?.points
                        if (polyline != null) {
                            val decodedPath = decodePolyline(polyline)
                            addNewPoints(decodedPath)

                            if (!hasCenteredCamera) {
                                centerMapOnRoute(currentLocation, endLocation)
                            }
                        }
                    } else {
                        Log.d("RouteUpdate", "Error en la respuesta de la Directions API.")
                    }
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    Log.d("RouteUpdate", "Fallo en la solicitud de la Directions API: ${t.message}")
                }
            })
        } else {
            Log.d("RouteUpdate", "No se encontraron coordenadas de destino.")
        }
    }

    // Agregar nuevos puntos a la ruta y redibujar la polilínea
    private fun addNewPoints(newPoints: List<LatLng>) {
        // Elimina la polilínea existente si está inicializada
        if (::routePolyline.isInitialized) {
            routePolyline.remove()
        }

        // Agrega los nuevos puntos a la lista de puntos de ruta
        routePoints.clear() // Limpia la lista de puntos anteriores
        routePoints.addAll(newPoints)

        // Dibuja la nueva polilínea con los puntos actualizados
        routePolyline = mMap.addPolyline(
            PolylineOptions()
                .addAll(routePoints)
                .width(30f)
                .color(Color.BLUE)
        )
    }

    // Inicia las actualizaciones de ubicación en tiempo real
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = LocationRequest.create().apply {
            interval = 5000 // Actualiza cada 5 segundos
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    // Actualiza la ruta con la nueva ubicación
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    updateRoute(currentLatLng)
                }
            }
        }

        // Solicitar actualizaciones de ubicación
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }



    private fun addStartMarker(location: LatLng) {
        startMarker = mMap.addMarker(MarkerOptions().position(location).title("Punto de Inicio"))
    }

    private fun removeStartMarker() {
        startMarker?.remove() // Elimina solo el marcador de inicio
        startMarker = null
    }

    private fun centerMapOnRoute(startLocation: LatLng, endLocation: LatLng) {
        // Crear un LatLngBounds.Builder para incluir ambos puntos
        val builder = LatLngBounds.Builder()
        builder.include(startLocation)
        builder.include(endLocation)

        // Construir el LatLngBounds
        val bounds = builder.build()

        // Crear un ajuste de cámara para incluir los dos puntos
        val padding = 400 // Padding en píxeles
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)

        // Mover la cámara para ajustar el mapa a los dos puntos
        mMap.moveCamera(cameraUpdate)

        hasCenteredCamera = true
    }

    private fun start3DRouteView() {
        removeStartMarker()
        // Obtén la ubicación actual del LocationProvider
        val currentLocation = LocationProvider.getCurrentLocation()

        if (currentLocation != null) {
            val currentLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)

            // Configurar la cámara para centrarse en la ubicación actual
            val cameraPosition = CameraPosition.Builder()
                .target(currentLatLng) // Posiciona la cámara en la ubicación actual
                .tilt(60f) // Inclinación para una vista en 3D
                .bearing(0f) // Orientación de la cámara; puede ajustarse si es necesario
                .zoom(15f) // Nivel de zoom para acercarse más
                .build()

            // Mover la cámara para centrarse en la ubicación actual
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        } else {
            Log.d("MapScreen3", "Ubicación actual no disponible.")
        }
    }

    private fun updateCameraPosition() {
        val currentLocation = LocationProvider.getCurrentLocation()

        if (currentLocation != null) {
            val currentLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)

            // Configurar la cámara para centrarse en la ubicación actual
            val cameraPosition = CameraPosition.Builder()
                .target(currentLatLng) // Posiciona la cámara en la ubicación actual
                .tilt(80f) // Inclinación para una vista en 3D
                .bearing(0f) // Orientación de la cámara; puede ajustarse si es necesario
                .zoom(21f) // Nivel de zoom para acercarse más
                .build()

            // Mover la cámara para centrarse en la ubicación actual
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        } else {
            Log.d("MapScreen3", "Ubicación actual no disponible.")
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            poly.add(LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5)))
        }
        return poly
    }
}

