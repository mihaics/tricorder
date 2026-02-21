package com.sysop.tricorder.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.model.SensorCategory
import com.sysop.tricorder.core.model.SensorReading
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val sensorRegistry: SensorRegistry,
) : ViewModel() {

    private val _activeCategories = MutableStateFlow(setOf<SensorCategory>())
    val activeCategories: StateFlow<Set<SensorCategory>> = _activeCategories.asStateFlow()

    private val _readings = MutableStateFlow(mapOf<String, SensorReading>())
    val readings: StateFlow<Map<String, SensorReading>> = _readings.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    fun toggleCategory(category: SensorCategory) {
        _activeCategories.update { current ->
            if (category in current) current - category
            else current + category
        }
        collectReadings()
    }

    private var collectionJob: kotlinx.coroutines.Job? = null

    private fun collectReadings() {
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {
            val activeProviders = _activeCategories.value.flatMap { category ->
                sensorRegistry.getProvidersByCategory(category)
            }
            if (activeProviders.isEmpty()) {
                _readings.value = emptyMap()
                return@launch
            }

            merge(*activeProviders.map { it.readings() }.toTypedArray())
                .collect { reading ->
                    _readings.update { current ->
                        current + (reading.providerId to reading)
                    }
                }
        }
    }

    fun toggleRecording() {
        _isRecording.update { !it }
    }
}
