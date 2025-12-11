package com.example.weatherproject.util

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class LocationProvider(private val application: Application) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)
    private val geocoder = Geocoder(application, Locale.KOREAN)

    private var locationCallback: LocationCallback? = null

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getCurrentLocationOnce() {
        if (!hasLocationPermission()) {
            Log.w("LocationProvider", "Location permission not granted.")
            return
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    Log.d("LocationProvider", "Got last known location: $location")
                    _currentLocation.value = location
                    getAddressFromLocation(location)
                }
            }
        } catch (e: SecurityException) {
            Log.e("LocationProvider", "Failed to get location.", e)
        }
    }

    fun startLocationTracking() {
        if (!hasLocationPermission()) {
            Log.w("LocationProvider", "Location permission not granted.")
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            60000L
        ).apply {
            setMinUpdateIntervalMillis(30000L)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                if (lastLocation != null) {
                    Log.d("LocationProvider", "Location updated: $lastLocation")
                    _currentLocation.value = lastLocation
                    getAddressFromLocation(lastLocation)
                }
            }
        }

        try {
            val callback = locationCallback
            if (callback != null) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    callback,
                    Looper.getMainLooper()
                )
            }
        } catch (e: SecurityException) {
            Log.e("LocationProvider", "Failed to start location tracking.", e)
        }
    }

    fun stopLocationTracking() {
        val callback = locationCallback
        if (callback != null) {
            fusedLocationClient.removeLocationUpdates(callback)
            locationCallback = null
            Log.d("LocationProvider", "Location tracking stopped.")
        }
    }

    private fun getAddressFromLocation(location: Location) {
        CoroutineScope(Dispatchers.Main).launch {
            var fetchedAddress = "주소 정보 없음"
            withContext(Dispatchers.IO) {
                try {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val addr = addresses[0]
                        val addressBuilder = StringBuilder()

                        val adminArea = addr.adminArea
                        if (adminArea != null) {
                            addressBuilder.append(adminArea.replace("특별시", "시").replace("광역시", "시"))
                        }

                        val locality = addr.subLocality ?: addr.locality
                        if (locality != null) {
                            if (addressBuilder.isNotEmpty()) addressBuilder.append(" ")
                            addressBuilder.append(locality)
                        }

                        val thoroughfare = addr.thoroughfare
                        if (thoroughfare != null) {
                            if (addressBuilder.isNotEmpty()) addressBuilder.append(" ")
                            addressBuilder.append(thoroughfare)
                        }
                        
                        var finalAddress = addressBuilder.toString()
                        if (finalAddress.isBlank()) {
                            val fullAddress = addr.getAddressLine(0)
                            if (fullAddress != null) {
                                finalAddress = fullAddress.split(" ").take(3).joinToString(" ")
                            }
                        }

                        if (finalAddress.isNotBlank()) {
                            fetchedAddress = finalAddress
                        }
                    }
                } catch (e: IOException) {
                    Log.e("Geocoder", "Error fetching address", e)
                    fetchedAddress = "주소 확인 실패"
                }
            }
            _address.value = fetchedAddress
        }
    }
}
