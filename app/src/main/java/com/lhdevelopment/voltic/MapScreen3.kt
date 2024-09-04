package com.lhdevelopment.voltic

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Suppress("DEPRECATION")
class MapScreen3 : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val apiKey = "AIzaSyDLEaBvnGVCfUSa0dE_AoKPpjp57mWPNkg" // Reemplaza con tu clave de API
    private lateinit var loadingScreen: View
    private var startMarker: Marker? = null
    private val handler = Handler()
    private val updateInterval: Long = 100
    private val runnable = object : Runnable {
        override fun run() {
            updateCameraPosition()
            handler.postDelayed(this, updateInterval)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mapscreen3)

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

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        showRoute(this)
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

            addStartMarker(startLocation)
            mMap.addMarker(MarkerOptions().position(endLocation).title("Punto de Fin"))

            val retrofit = Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(DirectionsService::class.java)
            val call = service.getDirections(
                "${startLatitude},${startLongitude}",
                "${endLatitude},${endLongitude}",
                apiKey
            )

            call.enqueue(object : retrofit2.Callback<DirectionsResponse> {
                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: retrofit2.Response<DirectionsResponse>
                ) {
                    if (response.isSuccessful) {
                        val polyline =
                            response.body()?.routes?.firstOrNull()?.overviewPolyline?.points
                        if (polyline != null) {
                            val decodedPath = decodePolyline(polyline)
                            mMap.addPolyline(
                                PolylineOptions()
                                    .addAll(decodedPath)
                                    .width(30f)
                                    .color(Color.RED)
                            )

                            // Centrar el mapa para mostrar ambos puntos
                            centerMapOnRoute(startLocation, endLocation)
                            loadingScreen.visibility = View.GONE
                        }
                    } else {
                        Log.d("RouteDisplay", "Error en la respuesta de la Directions API.")
                    }
                }

                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    Log.d(
                        "RouteDisplay",
                        "Fallo en la solicitud de la Directions API: ${t.message}"
                    )
                }
            })
        } else {
            Log.d("RouteDisplay", "No se encontraron coordenadas guardadas para la ruta.")
        }
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

