package com.sysop.tricorder.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.model.SensorCategory
import com.sysop.tricorder.core.model.SensorReading
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
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

    private val latestReadings = ConcurrentHashMap<String, SensorReading>()

    fun toggleCategory(category: SensorCategory) {
        _activeCategories.update { current ->
            if (category in current) {
                // Remove readings for deactivated category
                val providersToRemove = sensorRegistry.getProvidersByCategory(category)
                    .map { it.id }.toSet()
                providersToRemove.forEach { latestReadings.remove(it) }
                current - category
            } else {
                current + category
            }
        }
        collectReadings()
    }

    private var collectionJob: Job? = null

    @OptIn(FlowPreview::class)
    private fun collectReadings() {
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {
            val activeProviders = _activeCategories.value.flatMap { category ->
                sensorRegistry.getProvidersByCategory(category)
            }
            if (activeProviders.isEmpty()) {
                latestReadings.clear()
                _readings.value = emptyMap()
                return@launch
            }

            // Buffer all incoming readings into a ConcurrentHashMap,
            // then emit a snapshot to the UI at most every 250ms
            merge(*activeProviders.map { it.readings() }.toTypedArray())
                .flowOn(Dispatchers.Default)
                .onEach { reading -> latestReadings[reading.providerId] = reading }
                .sample(250)
                .collect {
                    _readings.value = latestReadings.toMap()
                }
        }
    }

    fun toggleRecording() {
        _isRecording.update { !it }
    }
}
