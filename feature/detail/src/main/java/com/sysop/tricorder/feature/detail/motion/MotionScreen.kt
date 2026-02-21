package com.sysop.tricorder.feature.detail.motion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sysop.tricorder.core.ui.theme.MotionCyan

@Composable
fun MotionScreen(
    onBack: () -> Unit = {},
    viewModel: MotionViewModel = hiltViewModel(),
) {
    val data by viewModel.motionData.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Text(
            text = "Motion & Orientation",
            style = MaterialTheme.typography.headlineMedium,
            color = MotionCyan,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step counter
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Steps", style = MaterialTheme.typography.titleMedium)
                Text("%.0f".format(data.steps), style = MaterialTheme.typography.headlineLarge, color = MotionCyan)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Accelerometer
        SensorCard("Accelerometer (m/s\u00B2)", "X" to data.accelX, "Y" to data.accelY, "Z" to data.accelZ)

        Spacer(modifier = Modifier.height(8.dp))

        // Gyroscope
        SensorCard("Gyroscope (rad/s)", "X" to data.gyroX, "Y" to data.gyroY, "Z" to data.gyroZ)

        Spacer(modifier = Modifier.height(8.dp))

        // Magnetometer
        SensorCard("Magnetometer (\u00B5T)", "X" to data.magX, "Y" to data.magY, "Z" to data.magZ)

        Spacer(modifier = Modifier.height(8.dp))

        // Rotation
        SensorCard("Rotation Vector", "X" to data.rotX, "Y" to data.rotY, "Z" to data.rotZ)
    }
}

@Composable
private fun SensorCard(title: String, vararg axes: Pair<String, Float>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                axes.forEach { (label, value) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                        Text("%.3f".format(value), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
