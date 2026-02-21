package com.sysop.tricorder.feature.detail.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AudioSpectrumScreen(
    onBack: () -> Unit = {},
    viewModel: AudioSpectrumViewModel = hiltViewModel(),
) {
    val dbSpl by viewModel.dbSpl.collectAsState()
    val peakFrequency by viewModel.peakFrequency.collectAsState()
    val spectrogramData by viewModel.spectrogramData.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Audio Spectrum",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // dB meter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("%.1f dB".format(dbSpl), style = MaterialTheme.typography.headlineLarge)
            Text("Peak: %.0f Hz".format(peakFrequency), style = MaterialTheme.typography.titleMedium)
        }

        // dB bar
        val dbNormalized = ((dbSpl + 96) / 96).toFloat().coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { dbNormalized },
            modifier = Modifier.fillMaxWidth().height(8.dp).padding(vertical = 4.dp),
            color = when {
                dbNormalized > 0.8f -> Color.Red
                dbNormalized > 0.6f -> Color.Yellow
                else -> MaterialTheme.colorScheme.primary
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Spectrogram
        Text("Spectrogram", style = MaterialTheme.typography.titleMedium)
        SpectrogramCanvas(
            spectrogramData = spectrogramData,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Black),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Frequency (Hz) ->",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        )
    }
}
