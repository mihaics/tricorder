package com.sysop.tricorder.feature.detail.rf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sysop.tricorder.core.model.SensorReading
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RfScannerViewModel @Inject constructor(
    sensorRegistry: SensorRegistry,
) : ViewModel() {

    private val _wifiNetworks = MutableStateFlow(listOf<SensorReading>())
    val wifiNetworks: StateFlow<List<SensorReading>> = _wifiNetworks.asStateFlow()

    private val _bleDevices = MutableStateFlow(listOf<SensorReading>())
    val bleDevices: StateFlow<List<SensorReading>> = _bleDevices.asStateFlow()

    private val _cellInfo = MutableStateFlow(listOf<SensorReading>())
    val cellInfo: StateFlow<List<SensorReading>> = _cellInfo.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(index: Int) { _selectedTab.value = index }

    init {
        listOf("wifi-scan" to _wifiNetworks, "ble-scan" to _bleDevices, "cellular" to _cellInfo)
            .forEach { (providerId, state) ->
                val provider = sensorRegistry.getProvider(providerId)
                if (provider != null) {
                    viewModelScope.launch {
                        provider.readings().collect { reading ->
                            state.update { current ->
                                (current + reading)
                                    .distinctBy { it.labels["bssid"] ?: it.labels["mac_address"] ?: it.values.toString() }
                                    .sortedByDescending { it.values["rssi"] ?: it.values["signal_level"] ?: 0.0 }
                                    .take(50)
                            }
                        }
                    }
                }
            }
    }
}
