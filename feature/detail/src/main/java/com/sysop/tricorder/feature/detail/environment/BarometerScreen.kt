package com.sysop.tricorder.feature.detail.environment

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sysop.tricorder.core.ui.theme.EnvironmentGreen

@Composable
fun BarometerScreen(
    onBack: () -> Unit = {},
    viewModel: BarometerViewModel = hiltViewModel(),
) {
    val pressure by viewModel.pressure.collectAsState()
    val altitude by viewModel.altitude.collectAsState()
    val lightLux by viewModel.lightLux.collectAsState()
    val pressureHistory by viewModel.pressureHistory.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Barometer / Altimeter", style = MaterialTheme.typography.headlineMedium, color = EnvironmentGreen)

        Spacer(modifier = Modifier.height(24.dp))

        // Altitude
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Altitude", style = MaterialTheme.typography.titleMedium)
                Text("%.1f m".format(altitude), style = MaterialTheme.typography.headlineLarge, color = EnvironmentGreen)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pressure
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Pressure", style = MaterialTheme.typography.titleMedium)
                Text("%.2f hPa".format(pressure), style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(8.dp))
                // Trend indicator
                val trend = if (pressureHistory.size >= 2) {
                    val recent = pressureHistory.takeLast(10).average()
                    val older = pressureHistory.take(10).average()
                    when {
                        recent - older > 0.5 -> "Rising (Fair weather)"
                        older - recent > 0.5 -> "Falling (Storm approaching)"
                        else -> "Stable"
                    }
                } else "Collecting data..."
                Text(trend, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Light sensor
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Ambient Light", style = MaterialTheme.typography.titleMedium)
                Text("%.0f lux".format(lightLux), style = MaterialTheme.typography.headlineMedium, color = EnvironmentGreen)
            }
        }
    }
}
