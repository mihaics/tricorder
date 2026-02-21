package com.sysop.tricorder.feature.detail.rf

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sysop.tricorder.core.model.SensorReading

@Composable
fun RfScannerScreen(
    onBack: () -> Unit = {},
    viewModel: RfScannerViewModel = hiltViewModel(),
) {
    val wifiNetworks by viewModel.wifiNetworks.collectAsState()
    val bleDevices by viewModel.bleDevices.collectAsState()
    val cellInfo by viewModel.cellInfo.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "RF Scanner",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp),
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { viewModel.selectTab(0) }) {
                Text("WiFi (${wifiNetworks.size})", modifier = Modifier.padding(12.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { viewModel.selectTab(1) }) {
                Text("BLE (${bleDevices.size})", modifier = Modifier.padding(12.dp))
            }
            Tab(selected = selectedTab == 2, onClick = { viewModel.selectTab(2) }) {
                Text("Cell (${cellInfo.size})", modifier = Modifier.padding(12.dp))
            }
        }

        when (selectedTab) {
            0 -> RfList(wifiNetworks) { reading ->
                val ssid = reading.labels["ssid"] ?: "Hidden"
                val bssid = reading.labels["bssid"] ?: ""
                val rssi = reading.values["rssi"]?.toInt() ?: 0
                val freq = reading.values["frequency"]?.toInt() ?: 0
                val band = when { freq > 5900 -> "6 GHz"; freq > 4900 -> "5 GHz"; else -> "2.4 GHz" }
                Triple("$ssid ($band)", bssid, "$rssi dBm")
            }
            1 -> RfList(bleDevices) { reading ->
                val name = reading.labels["device_name"] ?: "Unknown"
                val mac = reading.labels["mac_address"] ?: ""
                val rssi = reading.values["rssi"]?.toInt() ?: 0
                Triple(name, mac, "$rssi dBm")
            }
            2 -> RfList(cellInfo) { reading ->
                val type = reading.labels["network_type"] ?: "Unknown"
                val level = reading.values["signal_level"]?.toInt() ?: 0
                val cellId = reading.values["cell_id"]?.toLong()?.toString() ?: ""
                Triple(type, "Cell: $cellId", "Level: $level/4")
            }
        }
    }
}

@Composable
private fun RfList(
    items: List<SensorReading>,
    formatter: (SensorReading) -> Triple<String, String, String>,
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        items(items) { reading ->
            val (title, subtitle, value) = formatter(reading)
            ListItem(
                headlineContent = { Text(title, style = MaterialTheme.typography.bodyLarge) },
                supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodyMedium) },
                trailingContent = { Text(value, style = MaterialTheme.typography.labelLarge) },
            )
            HorizontalDivider()
        }
    }
}
