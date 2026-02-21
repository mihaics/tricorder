package com.sysop.tricorder.feature.detail.gnss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SatelliteInfo(
    val constellation: Int,
    val svid: Int,
    val elevation: Float,
    val azimuth: Float,
    val cn0: Float,
    val usedInFix: Boolean,
)

@HiltViewModel
class GnssSkyPlotViewModel @Inject constructor(
    sensorRegistry: SensorRegistry,
) : ViewModel() {

    private val _satellites = MutableStateFlow(listOf<SatelliteInfo>())
    val satellites: StateFlow<List<SatelliteInfo>> = _satellites.asStateFlow()

    private val _fixInfo = MutableStateFlow(mapOf<String, Double>())
    val fixInfo: StateFlow<Map<String, Double>> = _fixInfo.asStateFlow()

    init {
        val provider = sensorRegistry.getProvider("location")
        if (provider != null) {
            viewModelScope.launch {
                provider.readings().collect { reading ->
                    if (reading.providerId == "gnss-satellites") {
                        val satData = reading.labels["satellites"] ?: return@collect
                        val parsed = satData.split(";").mapNotNull { entry ->
                            val parts = entry.split(",")
                            if (parts.size >= 6) {
                                SatelliteInfo(
                                    constellation = parts[0].toIntOrNull() ?: 0,
                                    svid = parts[1].toIntOrNull() ?: 0,
                                    elevation = parts[2].toFloatOrNull() ?: 0f,
                                    azimuth = parts[3].toFloatOrNull() ?: 0f,
                                    cn0 = parts[4].toFloatOrNull() ?: 0f,
                                    usedInFix = parts[5].toBooleanStrictOrNull() ?: false,
                                )
                            } else null
                        }
                        _satellites.value = parsed
                        _fixInfo.value = mapOf(
                            "satellite_count" to reading.values["satellite_count"]!!,
                            "satellites_used" to reading.values["satellites_used"]!!,
                        )
                    }
                }
            }
        }
    }
}
