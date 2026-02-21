package com.sysop.tricorder

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sysop.tricorder.core.sensorapi.DeviceLocation
import dagger.hilt.android.HiltAndroidApp
import org.maplibre.android.MapLibre
import javax.inject.Inject

@HiltAndroidApp
class TricorderApp : Application() {

    @Inject lateinit var deviceLocation: DeviceLocation

    private var fusedLocationClient: FusedLocationProviderClient? = null

    override fun onCreate() {
        super.onCreate()
        MapLibre.getInstance(this)
        startPassiveLocation()
    }

    @SuppressLint("MissingPermission")
    private fun startPassiveLocation() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 30_000L)
            .setMinUpdateIntervalMillis(10_000L)
            .build()

        fusedLocationClient?.requestLocationUpdates(
            request,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation ?: return
                    deviceLocation.update(loc.latitude, loc.longitude)
                }
            },
            Looper.getMainLooper(),
        )

        // Also get last known location immediately
        fusedLocationClient?.lastLocation?.addOnSuccessListener { loc ->
            if (loc != null) {
                deviceLocation.update(loc.latitude, loc.longitude)
            }
        }
    }
}
