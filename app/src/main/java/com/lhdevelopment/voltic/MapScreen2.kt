package com.lhdevelopment.voltic

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AutoCompleteTextView
import android.content.res.Resources
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.model.Place
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.location.LocationServices
import java.util.Calendar

class MapScreen2 : FragmentActivity(), OnMapReadyCallback {

    private lateinit var placesClient: PlacesClient
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var autocompleteAdapter: PlacesAutoCompleteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureActivityTheme()
        setContentView(R.layout.mapscreen2)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicializar la API de Google Places
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyDLEaBvnGVCfUSa0dE_AoKPpjp57mWPNkg")
        }
        placesClient = Places.createClient(this)

        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.startPointSearch)
        val backButton: Button = findViewById(R.id.backButton)
        val nextButton: Button = findViewById(R.id.nextButton)
        val token = AutocompleteSessionToken.newInstance()

        autocompleteAdapter = PlacesAutoCompleteAdapter(this, placesClient)
        autoCompleteTextView.setAdapter(autocompleteAdapter)
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val placeId = autocompleteAdapter.getPlaceId(position)
            if (placeId != null) {
                fetchPlaceDetails(placeId)
            }
        }

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setSessionToken(token)
                        .setQuery(query)
                        .build()

                    placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->
                            autocompleteAdapter.updatePredictions(response.autocompletePredictions)
                        }
                        .addOnFailureListener { exception ->
                            // Manejo de errores
                        }
                }
            }
        })

        // Configuración del botón para regresar a MainPanel
        backButton.setOnClickListener {
            val intent = Intent(this, MapScreen1::class.java)
            startActivity(intent)
        }

        nextButton.setOnClickListener {
            val intent = Intent(this, MapScreen3::class.java)
            startActivity(intent)
        }


        // Configuración del fragmento del mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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

        // Habilitar controles de zoom
        mMap.uiSettings.isZoomControlsEnabled = true

        // Habilitar la ubicación actual y el botón de ubicación
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

        // Verificar si hay coordenadas disponibles desde el Intent y centrar el mapa en ellas
        val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)
        if (latitude != 0.0 && longitude != 0.0) {
            val location = LatLng(latitude, longitude)
            mMap.addMarker(MarkerOptions().position(location).title("Selected Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        } else {
            // Si no se proporcionan coordenadas, centrar el mapa en la ubicación actual
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }

        // Agregar un listener para seleccionar un punto en el mapa
        mMap.setOnMapClickListener { latLng ->
            mMap.clear() // Limpiar el mapa antes de agregar el nuevo marcador
            mMap.addMarker(MarkerOptions().position(latLng).title("Selected Point"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

            saveCoordinatesToSharedPreferences(latLng.latitude, latLng.longitude)
        }
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

    private fun saveCoordinatesToSharedPreferences(latitude: Double, longitude: Double) {
        val sharedPreferences = getSharedPreferences("MapPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("END_LATITUDE", latitude.toString())
        editor.putString("END_LONGITUDE", longitude.toString())
        editor.apply() // Asegúrate de aplicar los cambios
    }

    private fun fetchPlaceDetails(placeId: String) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val latLng = place.latLng
                if (latLng != null) {
                    updateMapWithLocation(latLng.latitude, latLng.longitude)
                }
            }
            .addOnFailureListener { exception ->
                // Manejo de errores
            }
    }

    private fun updateMapWithLocation(latitude: Double, longitude: Double) {
        val location = LatLng(latitude, longitude)
        mMap.clear() // Limpia el mapa antes de agregar el nuevo marcador
        mMap.addMarker(MarkerOptions().position(location).title("Selected Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }
}

