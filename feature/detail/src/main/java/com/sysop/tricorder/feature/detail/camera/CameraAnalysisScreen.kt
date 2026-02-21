package com.sysop.tricorder.feature.detail.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sysop.tricorder.core.ui.theme.CameraPink

@Composable
fun CameraAnalysisScreen(
    onBack: () -> Unit = {},
    viewModel: CameraAnalysisViewModel = hiltViewModel(),
) {
    val brightness by viewModel.brightness.collectAsState()
    val avgR by viewModel.avgR.collectAsState()
    val avgG by viewModel.avgG.collectAsState()
    val avgB by viewModel.avgB.collectAsState()
    val isPpgMode by viewModel.isPpgMode.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Camera Analysis", style = MaterialTheme.typography.headlineMedium, color = CameraPink)

        Spacer(modifier = Modifier.height(16.dp))

        // Camera preview placeholder (CameraX will be wired in later)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center,
        ) {
            Text("Camera Preview", color = Color.White.copy(alpha = 0.5f))
            // Crosshair
            Box(modifier = Modifier.size(2.dp, 40.dp).background(Color.White.copy(alpha = 0.5f)))
            Box(modifier = Modifier.size(40.dp, 2.dp).background(Color.White.copy(alpha = 0.5f)))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("PPG Mode", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = isPpgMode, onCheckedChange = { viewModel.togglePpgMode() })
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Color values
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Color Analysis", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ColorValue("R", avgR, Color.Red)
                    ColorValue("G", avgG, Color.Green)
                    ColorValue("B", avgB, Color.Blue)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Brightness: %.1f%%".format(brightness), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ColorValue(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = color)
        Text("%.0f".format(value), style = MaterialTheme.typography.headlineMedium)
    }
}
