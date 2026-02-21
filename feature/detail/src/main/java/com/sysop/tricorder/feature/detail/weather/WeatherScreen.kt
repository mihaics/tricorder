package com.sysop.tricorder.feature.detail.weather

import androidx.compose.foundation.layout.*
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
import com.sysop.tricorder.core.ui.theme.WeatherYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onBack: () -> Unit = {},
    viewModel: WeatherViewModel = hiltViewModel(),
) {
    val temperature by viewModel.temperature.collectAsState()
    val humidity by viewModel.humidity.collectAsState()
    val windSpeed by viewModel.windSpeed.collectAsState()
    val windDirection by viewModel.windDirection.collectAsState()
    val uvIndex by viewModel.uvIndex.collectAsState()
    val pressure by viewModel.pressure.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather", color = WeatherYellow) },
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
            if (temperature == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = WeatherYellow)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Fetching weather data...", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            } else {
                // Temperature - hero card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "Temperature",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                        Text(
                            "%.1f \u00B0C".format(temperature),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = WeatherYellow,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Humidity & Pressure row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WeatherCard(
                        label = "Humidity",
                        value = humidity?.let { "%.0f %%".format(it) } ?: "--",
                        modifier = Modifier.weight(1f),
                    )
                    WeatherCard(
                        label = "Pressure",
                        value = pressure?.let { "%.1f hPa".format(it) } ?: "--",
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Wind row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WeatherCard(
                        label = "Wind Speed",
                        value = windSpeed?.let { "%.1f km/h".format(it) } ?: "--",
                        modifier = Modifier.weight(1f),
                    )
                    WeatherCard(
                        label = "Wind Dir",
                        value = windDirection?.let { "%.0f\u00B0 %s".format(it, windDirectionLabel(it)) } ?: "--",
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // UV Index
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                "UV Index",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.7f),
                            )
                            uvIndex?.let {
                                Text(
                                    uvLevel(it),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = uvColor(it),
                                )
                            }
                        }
                        Text(
                            uvIndex?.let { "%.1f".format(it) } ?: "--",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = uvIndex?.let { uvColor(it) } ?: Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

private fun windDirectionLabel(degrees: Double): String = when {
    degrees < 22.5 || degrees >= 337.5 -> "N"
    degrees < 67.5 -> "NE"
    degrees < 112.5 -> "E"
    degrees < 157.5 -> "SE"
    degrees < 202.5 -> "S"
    degrees < 247.5 -> "SW"
    degrees < 292.5 -> "W"
    else -> "NW"
}

private fun uvLevel(uv: Double): String = when {
    uv < 3 -> "Low"
    uv < 6 -> "Moderate"
    uv < 8 -> "High"
    uv < 11 -> "Very High"
    else -> "Extreme"
}

private fun uvColor(uv: Double): Color = when {
    uv < 3 -> Color(0xFF4CAF50)
    uv < 6 -> Color(0xFFFFEB3B)
    uv < 8 -> Color(0xFFFF9800)
    uv < 11 -> Color(0xFFF44336)
    else -> Color(0xFF9C27B0)
}
