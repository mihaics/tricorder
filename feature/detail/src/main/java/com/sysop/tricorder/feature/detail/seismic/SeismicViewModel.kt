package com.sysop.tricorder.feature.detail.seismic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.model.SensorReading
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeismicViewModel @Inject constructor(
    sensorRegistry: SensorRegistry,
) : ViewModel() {

    private val _earthquakes = MutableStateFlow(listOf<SensorReading>())
    val earthquakes: StateFlow<List<SensorReading>> = _earthquakes.asStateFlow()

    init {
        val provider = sensorRegistry.getProvider("seismic")
        if (provider != null) {
            viewModelScope.launch {
                provider.readings().collect { reading ->
                    _earthquakes.update { current ->
                        // Deduplicate by time_epoch, keep latest 50
                        val epoch = reading.values["time_epoch"]
                        val filtered = if (epoch != null) {
                            current.filter { it.values["time_epoch"] != epoch }
                        } else current
                        (filtered + reading)
                            .sortedByDescending { it.values["magnitude"] ?: 0.0 }
                            .take(50)
                    }
                }
            }
        }
    }
}
