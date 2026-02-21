package com.sysop.tricorder.feature.detail.aviation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.model.SensorReading
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AircraftTrackerViewModel @Inject constructor(
    sensorRegistry: SensorRegistry,
) : ViewModel() {

    private val _aircraft = MutableStateFlow(listOf<SensorReading>())
    val aircraft: StateFlow<List<SensorReading>> = _aircraft.asStateFlow()

    private val _isTableMode = MutableStateFlow(true)
    val isTableMode: StateFlow<Boolean> = _isTableMode.asStateFlow()

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
                            lat * lat + lon * lon // approximate distance sort
                        }.take(100)
                    }
                }
            }
        }
    }
}
