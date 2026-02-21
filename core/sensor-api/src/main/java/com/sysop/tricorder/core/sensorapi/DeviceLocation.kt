package com.sysop.tricorder.core.sensorapi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class LatLon(val latitude: Double, val longitude: Double)

@Singleton
class DeviceLocation @Inject constructor() {

    private val _location = MutableStateFlow(DEFAULT_LOCATION)
    val location: StateFlow<LatLon> = _location.asStateFlow()

    fun update(latitude: Double, longitude: Double) {
        _location.value = LatLon(latitude, longitude)
    }

    val lat: Double get() = _location.value.latitude
    val lon: Double get() = _location.value.longitude
    val isAvailable: Boolean get() = true

    companion object {
        // Default: Arad, Romania
        val DEFAULT_LOCATION = LatLon(46.1866, 21.3123)
    }
}
