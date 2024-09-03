package com.lhdevelopment.voltic

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapScreen3 : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val apiKey = "AIzaSyDLEaBvnGVCfUSa0dE_AoKPpjp57mWPNkg" // Reemplaza con tu clave de API
    private lateinit var loadingScreen: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mapscreen3)

        loadingScreen = findViewById(R.id.route_creation_loading_screen)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        val backButton: Button = findViewById(R.id.backButton)
        mapFragment.getMapAsync(this)

        backButton.setOnClickListener {
            val intent = Intent(this, MapScreen2::class.java)
            startActivity(intent)
        }
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        loadingScreen.visibility = View.VISIBLE
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

            mMap.addMarker(MarkerOptions().position(startLocation).title("Punto de Inicio"))
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
                override fun onResponse(call: Call<DirectionsResponse>, response: retrofit2.Response<DirectionsResponse>) {
                    if (response.isSuccessful) {
                        val polyline = response.body()?.routes?.firstOrNull()?.overviewPolyline?.points
                        if (polyline != null) {
                            val decodedPath = decodePolyline(polyline)
                            mMap.addPolyline(
                                PolylineOptions()
                                    .addAll(decodedPath)
                                    .width(8f)
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
                    Log.d("RouteDisplay", "Fallo en la solicitud de la Directions API: ${t.message}")
                }
            })
        } else {
            Log.d("RouteDisplay", "No se encontraron coordenadas guardadas para la ruta.")
        }
    }

    private fun centerMapOnRoute(startLocation: LatLng, endLocation: LatLng) {
        // Crear un LatLngBounds.Builder para incluir ambos puntos
        val builder = LatLngBounds.Builder()
        builder.include(startLocation)
        builder.include(endLocation)

        // Construir el LatLngBounds
        val bounds = builder.build()

        // Crear un ajuste de cámara para incluir los dos puntos
        val padding = 100 // Padding en píxeles
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)

        // Mover la cámara para ajustar el mapa a los dos puntos
        mMap.moveCamera(cameraUpdate)
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
