package com.lhdevelopment.voltic

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AutoCompleteTextView
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
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapScreen3 : FragmentActivity(), OnMapReadyCallback {

    private lateinit var placesClient: PlacesClient
    private lateinit var mMap: GoogleMap
    private lateinit var autocompleteAdapter: PlacesAutoCompleteAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mapscreen2)


        // Inicializar la API de Google Places
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyDLEaBvnGVCfUSa0dE_AoKPpjp57mWPNkg")
        }
        placesClient = Places.createClient(this)

        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.startPointSearch)
        val backButton: Button = findViewById(R.id.backButton)
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
            val intent = Intent(this, MapScreen2::class.java)
            startActivity(intent)
        }

        // Configuración del fragmento del mapa
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Verificar si hay coordenadas disponibles y centrar el mapa en ellas
        val latitude = intent.getDoubleExtra("LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("LONGITUDE", 0.0)
        if (latitude != 0.0 && longitude != 0.0) {
            val location = LatLng(latitude, longitude)
            mMap.addMarker(MarkerOptions().position(location).title("Selected Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
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

