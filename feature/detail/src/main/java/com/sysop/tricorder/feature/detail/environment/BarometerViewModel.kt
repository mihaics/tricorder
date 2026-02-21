package com.sysop.tricorder.feature.detail.environment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BarometerViewModel @Inject constructor(
    sensorRegistry: SensorRegistry,
) : ViewModel() {

    private val _pressure = MutableStateFlow(1013.25)
    val pressure: StateFlow<Double> = _pressure.asStateFlow()

    private val _altitude = MutableStateFlow(0.0)
    val altitude: StateFlow<Double> = _altitude.asStateFlow()

    private val _lightLux = MutableStateFlow(0.0)
    val lightLux: StateFlow<Double> = _lightLux.asStateFlow()

    private val _pressureHistory = MutableStateFlow(listOf<Double>())
    val pressureHistory: StateFlow<List<Double>> = _pressureHistory.asStateFlow()

    init {
        val provider = sensorRegistry.getProvider("environment")
        if (provider != null) {
            viewModelScope.launch {
                provider.readings().collect { reading ->
                    reading.values["pressure_hpa"]?.let { p ->
                        _pressure.value = p
                        _pressureHistory.update { (it + p).takeLast(180) } // ~3 min at 1/sec
                    }
                    reading.values["altitude_m"]?.let { _altitude.value = it }
                    reading.values["light_lux"]?.let { _lightLux.value = it }
                }
            }
        }
    }
}
