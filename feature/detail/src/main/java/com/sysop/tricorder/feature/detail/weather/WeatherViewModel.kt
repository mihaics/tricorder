package com.sysop.tricorder.feature.detail.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    sensorRegistry: SensorRegistry,
) : ViewModel() {

    private val _temperature = MutableStateFlow<Double?>(null)
    val temperature: StateFlow<Double?> = _temperature.asStateFlow()

    private val _humidity = MutableStateFlow<Double?>(null)
    val humidity: StateFlow<Double?> = _humidity.asStateFlow()

    private val _windSpeed = MutableStateFlow<Double?>(null)
    val windSpeed: StateFlow<Double?> = _windSpeed.asStateFlow()

    private val _windDirection = MutableStateFlow<Double?>(null)
    val windDirection: StateFlow<Double?> = _windDirection.asStateFlow()

    private val _uvIndex = MutableStateFlow<Double?>(null)
    val uvIndex: StateFlow<Double?> = _uvIndex.asStateFlow()

    private val _pressure = MutableStateFlow<Double?>(null)
    val pressure: StateFlow<Double?> = _pressure.asStateFlow()

    init {
        val weatherProvider = sensorRegistry.getProvider("weather")
        if (weatherProvider != null) {
            viewModelScope.launch {
                weatherProvider.readings().collect { reading ->
                    _temperature.value = reading.values["temperature_c"]
                    _humidity.value = reading.values["humidity_pct"]
                    _windSpeed.value = reading.values["wind_speed_kmh"]
                    _windDirection.value = reading.values["wind_direction_deg"]
                    _uvIndex.value = reading.values["uv_index"]
                    _pressure.value = reading.values["pressure_hpa"]
                }
            }
        }
    }
}
