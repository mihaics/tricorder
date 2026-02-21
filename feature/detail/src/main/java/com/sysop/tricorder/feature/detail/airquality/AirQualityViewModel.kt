package com.sysop.tricorder.feature.detail.airquality

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AirQualityViewModel @Inject constructor(
    sensorRegistry: SensorRegistry,
) : ViewModel() {

    private val _aqi = MutableStateFlow<Double?>(null)
    val aqi: StateFlow<Double?> = _aqi.asStateFlow()

    private val _pm25 = MutableStateFlow<Double?>(null)
    val pm25: StateFlow<Double?> = _pm25.asStateFlow()

    private val _pm10 = MutableStateFlow<Double?>(null)
    val pm10: StateFlow<Double?> = _pm10.asStateFlow()

    private val _o3 = MutableStateFlow<Double?>(null)
    val o3: StateFlow<Double?> = _o3.asStateFlow()

    private val _no2 = MutableStateFlow<Double?>(null)
    val no2: StateFlow<Double?> = _no2.asStateFlow()

    private val _so2 = MutableStateFlow<Double?>(null)
    val so2: StateFlow<Double?> = _so2.asStateFlow()

    private val _co = MutableStateFlow<Double?>(null)
    val co: StateFlow<Double?> = _co.asStateFlow()

    init {
        val provider = sensorRegistry.getProvider("air-quality")
        if (provider != null) {
            viewModelScope.launch {
                provider.readings().collect { reading ->
                    _aqi.value = reading.values["aqi"]
                    _pm25.value = reading.values["pm25"]
                    _pm10.value = reading.values["pm10"]
                    _o3.value = reading.values["o3"]
                    _no2.value = reading.values["no2"]
                    _so2.value = reading.values["so2"]
                    _co.value = reading.values["co"]
                }
            }
        }
    }
}
