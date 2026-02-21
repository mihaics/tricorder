package com.sysop.tricorder.feature.detail.seismic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sysop.tricorder.core.ui.theme.DarkSurface
import com.sysop.tricorder.core.ui.theme.SeismicRed
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeismicScreen(
    onBack: () -> Unit = {},
    viewModel: SeismicViewModel = hiltViewModel(),
) {
    val earthquakes by viewModel.earthquakes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Earthquakes (7 days)", color = SeismicRed) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (earthquakes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = SeismicRed)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Searching for earthquakes within 500 km...",
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(
                        "${earthquakes.size} earthquakes (M2.5+)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.7f),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(earthquakes) { quake ->
                    EarthquakeCard(quake)
                }
            }
        }
    }
}

@Composable
private fun EarthquakeCard(reading: com.sysop.tricorder.core.model.SensorReading) {
    val magnitude = reading.values["magnitude"] ?: 0.0
    val depth = reading.values["depth_km"] ?: 0.0
    val place = reading.labels["place"] ?: "Unknown"
    val timeEpoch = reading.values["time_epoch"]?.toLong()

    val magColor = when {
        magnitude >= 6.0 -> Color(0xFFD50000)
        magnitude >= 5.0 -> Color(0xFFF44336)
        magnitude >= 4.0 -> Color(0xFFFF9800)
        magnitude >= 3.0 -> Color(0xFFFFEB3B)
        else -> Color(0xFF4CAF50)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Magnitude badge
            Card(
                colors = CardDefaults.cardColors(containerColor = magColor.copy(alpha = 0.2f)),
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "%.1f".format(magnitude),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = magColor,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    place,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Depth: %.1f km".format(depth),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                )
                if (timeEpoch != null) {
                    val time = Instant.ofEpochMilli(timeEpoch)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("MMM d, HH:mm"))
                    Text(
                        time,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}
