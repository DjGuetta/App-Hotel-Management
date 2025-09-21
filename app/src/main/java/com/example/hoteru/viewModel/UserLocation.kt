package com.example.hoteru.viewModel

import android.Manifest
import android.app.Application
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



class UserLocation(application: Application) : AndroidViewModel(application) {
    private val fusedLocation = LocationServices.getFusedLocationProviderClient(application) // is the API from Google Play Services to get the device’s location.
//    fusedLocationClient is Google’s Fused Location Provider → a smart API that combines GPS, Wi-Fi, and cell towers to get your location.
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun updateLocation(){
        try {
            fusedLocation.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        _userLocation.value = LatLng(it.latitude, it.longitude)
                    }
                }
        } catch (e: SecurityException){
            println("it didn't find the user location $e")
        }

    }

    fun calculateDistanceMeters(start: LatLng, end: LatLng): Float{
        val distance = FloatArray(1)
        Location.distanceBetween(
            start.latitude, start.longitude,
            end.latitude, end.longitude,
            distance
        )
        return distance[0]

    }
}