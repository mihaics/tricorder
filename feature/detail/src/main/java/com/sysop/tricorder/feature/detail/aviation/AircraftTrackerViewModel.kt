package com.sysop.tricorder.feature.detail.aviation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.model.SensorReading
import com.sysop.tricorder.core.sensorapi.DeviceLocation
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.*

data class AircraftArInfo(
    val callsign: String,
    val country: String,
    val altitudeM: Double,
    val velocityMs: Double,
    val headingDeg: Double,
    val distanceKm: Double,
    val bearingDeg: Double,
    val elevationDeg: Double,
    val latitude: Double,
    val longitude: Double,
)

@HiltViewModel
class AircraftTrackerViewModel @Inject constructor(
    sensorRegistry: SensorRegistry,
    private val deviceLocation: DeviceLocation,
) : ViewModel() {

    private val _aircraft = MutableStateFlow(listOf<SensorReading>())
    val aircraft: StateFlow<List<SensorReading>> = _aircraft.asStateFlow()

    private val _isTableMode = MutableStateFlow(true)
    val isTableMode: StateFlow<Boolean> = _isTableMode.asStateFlow()

    val arAircraft: StateFlow<List<AircraftArInfo>> = combine(_aircraft, flow { emit(Unit) }) { aircraft, _ ->
        val userLat = deviceLocation.lat
        val userLon = deviceLocation.lon
        aircraft.mapNotNull { reading ->
            val acLat = reading.values["latitude"] ?: return@mapNotNull null
            val acLon = reading.values["longitude"] ?: return@mapNotNull null
            val acAlt = reading.values["altitude_m"] ?: 0.0
            val distKm = haversineKm(userLat, userLon, acLat, acLon)
            val bearing = bearingDeg(userLat, userLon, acLat, acLon)
            val elevation = atan2(acAlt, distKm * 1000.0).let { Math.toDegrees(it) }

            AircraftArInfo(
                callsign = reading.labels["callsign"] ?: "",
                country = reading.labels["origin_country"] ?: "",
                altitudeM = acAlt,
                velocityMs = reading.values["velocity_ms"] ?: 0.0,
                headingDeg = reading.values["heading_deg"] ?: 0.0,
                distanceKm = distKm,
                bearingDeg = bearing,
                elevationDeg = elevation,
                latitude = acLat,
                longitude = acLon,
            )
        }.sortedBy { it.distanceKm }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleMode() { _isTableMode.update { !it } }

    init {
        val provider = sensorRegistry.getProvider("aviation")
        if (provider != null) {
            viewModelScope.launch {
                provider.readings().collect { reading ->
                    _aircraft.update { current ->
                        val callsign = reading.labels["callsign"] ?: ""
                        val updated = current.toMutableList()
                        val existingIndex = updated.indexOfFirst { it.labels["callsign"] == callsign }
                        if (existingIndex >= 0) {
                            updated[existingIndex] = reading
                        } else {
                            updated.add(reading)
                        }
                        updated.sortedBy {
                            val lat = it.values["latitude"] ?: 0.0
                            val lon = it.values["longitude"] ?: 0.0
                            lat * lat + lon * lon
                        }.take(100)
                    }
                }
            }
        }
    }

    companion object {
        fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371.0
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
            return r * 2 * atan2(sqrt(a), sqrt(1 - a))
        }

        fun bearingDeg(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val dLon = Math.toRadians(lon2 - lon1)
            val y = sin(dLon) * cos(Math.toRadians(lat2))
            val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
                    sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
            var bearing = Math.toDegrees(atan2(y, x))
            if (bearing < 0) bearing += 360.0
            return bearing
        }
    }
}
