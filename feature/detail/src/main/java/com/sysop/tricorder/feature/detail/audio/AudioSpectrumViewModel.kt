package com.sysop.tricorder.feature.detail.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioSpectrumViewModel @Inject constructor(
    sensorRegistry: SensorRegistry,
) : ViewModel() {

    private val _dbSpl = MutableStateFlow(-96.0)
    val dbSpl: StateFlow<Double> = _dbSpl.asStateFlow()

    private val _peakFrequency = MutableStateFlow(0.0)
    val peakFrequency: StateFlow<Double> = _peakFrequency.asStateFlow()

    private val _spectrum = MutableStateFlow(floatArrayOf())
    val spectrum: StateFlow<FloatArray> = _spectrum.asStateFlow()

    // Rolling spectrogram data (last 100 frames)
    private val _spectrogramData = MutableStateFlow(listOf<FloatArray>())
    val spectrogramData: StateFlow<List<FloatArray>> = _spectrogramData.asStateFlow()

    init {
        val audioProvider = sensorRegistry.getProvider("audio")
        if (audioProvider != null) {
            viewModelScope.launch {
                audioProvider.readings().collect { reading ->
                    _dbSpl.value = reading.values["db_spl"] ?: -96.0
                    _peakFrequency.value = reading.values["peak_frequency_hz"] ?: 0.0

                    val spectrumStr = reading.labels["spectrum"]
                    if (spectrumStr != null) {
                        val magnitudes = spectrumStr.split(",").mapNotNull { it.toFloatOrNull() }.toFloatArray()
                        _spectrum.value = magnitudes
                        _spectrogramData.update { current ->
                            (current + listOf(magnitudes)).takeLast(100)
                        }
                    }
                }
            }
        }
    }
}
