package com.sysop.tricorder.feature.detail.compass

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CompassScreen(
    onBack: () -> Unit = {},
    viewModel: CompassViewModel = hiltViewModel(),
) {
    val heading by viewModel.heading.collectAsState()
    val fieldStrength by viewModel.fieldStrength.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Compass",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        CompassRose(heading = heading)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "%.1f\u00B0".format(heading),
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Field: %.1f \u00B5T".format(fieldStrength),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
    }
}
