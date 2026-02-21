package com.sysop.tricorder.feature.detail.audio

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
import com.sysop.tricorder.core.ui.theme.AudioPurple
import com.sysop.tricorder.core.ui.theme.DarkSurface
import com.sysop.tricorder.core.ui.theme.DarkSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSpectrumScreen(
    onBack: () -> Unit = {},
    viewModel: AudioSpectrumViewModel = hiltViewModel(),
) {
    val dbSpl by viewModel.dbSpl.collectAsState()
    val peakFrequency by viewModel.peakFrequency.collectAsState()
    val spectrogramData by viewModel.spectrogramData.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Spectrum", color = AudioPurple) },
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
            // dB meter
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            "%.1f dB".format(dbSpl),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Text(
                            "Peak: %.0f Hz".format(peakFrequency),
                            style = MaterialTheme.typography.titleMedium,
                            color = AudioPurple,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val dbNormalized = ((dbSpl + 96) / 96).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { dbNormalized },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            dbNormalized > 0.8f -> Color.Red
                            dbNormalized > 0.6f -> Color.Yellow
                            else -> AudioPurple
                        },
                        trackColor = DarkSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spectrogram
            Text(
                "Spectrogram",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            SpectrogramCanvas(
                spectrogramData = spectrogramData,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "0 Hz",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.4f),
                )
                Text(
                    "22050 Hz",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.4f),
                )
            }
        }
    }
}
