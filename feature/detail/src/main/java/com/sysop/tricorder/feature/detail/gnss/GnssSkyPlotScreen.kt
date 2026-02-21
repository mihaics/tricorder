package com.sysop.tricorder.feature.detail.gnss

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sysop.tricorder.core.ui.theme.LocationBlue

@Composable
fun GnssSkyPlotScreen(
    onBack: () -> Unit = {},
    viewModel: GnssSkyPlotViewModel = hiltViewModel(),
) {
    val satellites by viewModel.satellites.collectAsState()
    val fixInfo by viewModel.fixInfo.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("GNSS Sky Plot", style = MaterialTheme.typography.headlineMedium, color = LocationBlue)

        Spacer(modifier = Modifier.height(16.dp))

        SkyPlotCanvas(satellites = satellites)

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LegendDot(Color(0xFF2196F3), "GPS")
            LegendDot(Color(0xFFF44336), "GLONASS")
            LegendDot(Color(0xFFFF9800), "Galileo")
            LegendDot(Color(0xFF4CAF50), "BeiDou")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fix info
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Visible", style = MaterialTheme.typography.bodyMedium)
                Text("${fixInfo["satellite_count"]?.toInt() ?: 0}", style = MaterialTheme.typography.headlineMedium, color = LocationBlue)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Used in Fix", style = MaterialTheme.typography.bodyMedium)
                Text("${fixInfo["satellites_used"]?.toInt() ?: 0}", style = MaterialTheme.typography.headlineMedium, color = LocationBlue)
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = color)
        }
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
