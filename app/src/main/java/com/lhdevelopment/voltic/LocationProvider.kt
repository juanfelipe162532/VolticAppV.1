package com.lhdevelopment.voltic

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

object LocationProvider {
    private var currentLocation: Location? = null
    private lateinit var locationManager: LocationManager
    private var locationListener: LocationListener? = null

    @SuppressLint("MissingPermission")
    fun init(context: Context) {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                currentLocation = location
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        // Request location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10f, locationListener!!)
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10f, locationListener!!)
    }

    fun getCurrentLocation(): Location? {
        return currentLocation
    }
}

