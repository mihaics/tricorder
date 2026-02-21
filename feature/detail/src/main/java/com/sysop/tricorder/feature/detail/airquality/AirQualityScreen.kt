package com.sysop.tricorder.feature.detail.airquality

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sysop.tricorder.core.ui.theme.AirQualityTeal
import com.sysop.tricorder.core.ui.theme.DarkSurface
import com.sysop.tricorder.core.ui.theme.DarkSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirQualityScreen(
    onBack: () -> Unit = {},
    viewModel: AirQualityViewModel = hiltViewModel(),
) {
    val aqi by viewModel.aqi.collectAsState()
    val pm25 by viewModel.pm25.collectAsState()
    val pm10 by viewModel.pm10.collectAsState()
    val o3 by viewModel.o3.collectAsState()
    val no2 by viewModel.no2.collectAsState()
    val so2 by viewModel.so2.collectAsState()
    val co by viewModel.co.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Air Quality", color = AirQualityTeal) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            if (aqi == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AirQualityTeal)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Fetching air quality data...", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            } else {
                // AQI hero card
                val aqiValue = aqi ?: 0.0
                val aqiColor = aqiColor(aqiValue)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "Air Quality Index",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                        Text(
                            "%.0f".format(aqiValue),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = aqiColor,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            aqiLevel(aqiValue),
                            style = MaterialTheme.typography.titleSmall,
                            color = aqiColor,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // AQI bar
                        LinearProgressIndicator(
                            progress = { (aqiValue / 300f).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = aqiColor,
                            trackColor = DarkSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Pollutants",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f),
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Pollutant cards
                val pollutants = listOf(
                    Triple("PM2.5", pm25, "\u00B5g/m\u00B3"),
                    Triple("PM10", pm10, "\u00B5g/m\u00B3"),
                    Triple("O\u2083 (Ozone)", o3, "\u00B5g/m\u00B3"),
                    Triple("NO\u2082", no2, "\u00B5g/m\u00B3"),
                    Triple("SO\u2082", so2, "\u00B5g/m\u00B3"),
                    Triple("CO", co, "\u00B5g/m\u00B3"),
                )

                pollutants.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        row.forEach { (label, value, unit) ->
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.7f),
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        value?.let { "%.1f".format(it) } ?: "--",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = AirQualityTeal,
                                    )
                                    Text(
                                        unit,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.4f),
                                    )
                                }
                            }
                        }
                        // Pad if odd number in last row
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

private fun aqiLevel(aqi: Double): String = when {
    aqi <= 50 -> "Good"
    aqi <= 100 -> "Moderate"
    aqi <= 150 -> "Unhealthy for Sensitive Groups"
    aqi <= 200 -> "Unhealthy"
    aqi <= 300 -> "Very Unhealthy"
    else -> "Hazardous"
}

private fun aqiColor(aqi: Double): Color = when {
    aqi <= 50 -> Color(0xFF4CAF50)
    aqi <= 100 -> Color(0xFFFFEB3B)
    aqi <= 150 -> Color(0xFFFF9800)
    aqi <= 200 -> Color(0xFFF44336)
    aqi <= 300 -> Color(0xFF9C27B0)
    else -> Color(0xFF800000)
}
