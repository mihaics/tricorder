package com.sysop.tricorder.sensor.location

import android.annotation.SuppressLint
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.SensorProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant
import javax.inject.Inject

class LocationProvider @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient?,
    private val locationManager: LocationManager?,
) : SensorProvider {

    override val id = "location"
    override val name = "Location & GNSS"
    override val category = SensorCategory.LOCATION

    override fun availability(): SensorAvailability {
        return if (fusedLocationClient != null || locationManager != null) {
            SensorAvailability.REQUIRES_PERMISSION
        } else {
            SensorAvailability.UNAVAILABLE
        }
    }

    @SuppressLint("MissingPermission")
    override fun readings(): Flow<SensorReading> = callbackFlow {
        val client = fusedLocationClient ?: run { close(); return@callbackFlow }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(500L)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                trySend(SensorReading(
                    providerId = id,
                    category = category,
                    timestamp = Instant.now(),
                    values = buildMap {
                        put("latitude", location.latitude)
                        put("longitude", location.longitude)
                        put("altitude", location.altitude)
                        put("speed", location.speed.toDouble())
                        put("bearing", location.bearing.toDouble())
                        put("accuracy", location.accuracy.toDouble())
                    },
                    latitude = location.latitude,
                    longitude = location.longitude,
                ))
            }
        }

        try {
            client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (_: SecurityException) {
            close()
            return@callbackFlow
        }

        // Also register for GNSS satellite status
        val gnssCallback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                val values = mutableMapOf<String, Double>()
                values["satellite_count"] = status.satelliteCount.toDouble()
                var usedCount = 0
                for (i in 0 until status.satelliteCount) {
                    if (status.usedInFix(i)) usedCount++
                }
                values["satellites_used"] = usedCount.toDouble()

                val labels = mutableMapOf<String, String>()
                // Encode satellite details as JSON-like string for detail view
                val satellites = buildList {
                    for (i in 0 until status.satelliteCount) {
                        add("${status.getConstellationType(i)},${status.getSvid(i)},${status.getElevationDegrees(i)},${status.getAzimuthDegrees(i)},${status.getCn0DbHz(i)},${status.usedInFix(i)}")
                    }
                }
                labels["satellites"] = satellites.joinToString(";")

                trySend(SensorReading(
                    providerId = "gnss-satellites",
                    category = category,
                    timestamp = Instant.now(),
                    values = values,
                    labels = labels,
                ))
            }
        }

        try {
            locationManager?.registerGnssStatusCallback(gnssCallback, android.os.Handler(Looper.getMainLooper()))
        } catch (_: SecurityException) {
            // GNSS status not available without permission, continue with location only
        }

        awaitClose {
            client.removeLocationUpdates(locationCallback)
            try {
                locationManager?.unregisterGnssStatusCallback(gnssCallback)
            } catch (_: SecurityException) {
                // Already unregistered or never registered
            }
        }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.MARKERS)
}
