package com.sysop.tricorder.feature.detail.aviation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sysop.tricorder.core.ui.theme.AviationSkyBlue

@Composable
fun AircraftTrackerScreen(
    onBack: () -> Unit = {},
    viewModel: AircraftTrackerViewModel = hiltViewModel(),
) {
    val aircraft by viewModel.aircraft.collectAsState()
    val isTableMode by viewModel.isTableMode.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Aircraft Tracker", style = MaterialTheme.typography.headlineMedium, color = AviationSkyBlue)
            TextButton(onClick = { viewModel.toggleMode() }) {
                Text(if (isTableMode) "AR View" else "Table View")
            }
        }

        Text(
            "${aircraft.size} aircraft detected",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )

        if (isTableMode) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(aircraft) { reading ->
                    val callsign = reading.labels["callsign"] ?: "N/A"
                    val country = reading.labels["origin_country"] ?: ""
                    val altitude = reading.values["altitude_m"]?.let { "%.0f m".format(it) } ?: "N/A"
                    val speed = reading.values["velocity_ms"]?.let { "%.0f m/s".format(it) } ?: ""
                    val heading = reading.values["heading_deg"]?.let { "%.0f\u00B0".format(it) } ?: ""

                    ListItem(
                        headlineContent = { Text(callsign, style = MaterialTheme.typography.labelLarge) },
                        supportingContent = { Text("$country  $heading  $speed") },
                        trailingContent = { Text(altitude, style = MaterialTheme.typography.bodyLarge, color = AviationSkyBlue) },
                    )
                    HorizontalDivider()
                }
            }
        } else {
            // AR mode placeholder
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("AR Mode\n(Camera + Aircraft Overlay)", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
