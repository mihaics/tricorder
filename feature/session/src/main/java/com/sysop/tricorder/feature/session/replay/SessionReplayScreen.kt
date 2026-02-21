package com.sysop.tricorder.feature.session.replay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sysop.tricorder.core.database.entity.ReadingEntity
import com.sysop.tricorder.core.ui.theme.AccentGreen
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionReplayScreen(
    onBack: () -> Unit = {},
    viewModel: SessionReplayViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.session?.name ?: "Session Replay",
                        fontFamily = MaterialTheme.typography.headlineMedium.fontFamily,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = AccentGreen,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            // Session info header
            state.session?.let { session ->
                SessionInfoHeader(session = session, durationMs = state.durationMs)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback controls
            PlaybackControls(
                isPlaying = state.isPlaying,
                speed = state.speed,
                progress = state.progress,
                currentTimeMs = state.currentTimeMs,
                durationMs = state.durationMs,
                onTogglePlayback = viewModel::togglePlayback,
                onSpeedChange = viewModel::setSpeed,
                onSeek = viewModel::seekTo,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Readings grouped by provider
            Text(
                text = "Readings (${state.visibleReadings.size}/${state.allReadings.size})",
                style = MaterialTheme.typography.titleMedium,
                color = AccentGreen,
            )

            Spacer(modifier = Modifier.height(8.dp))

            val grouped = state.visibleReadings
                .groupBy { it.providerId }
                .mapValues { (_, readings) -> readings.lastOrNull() }
                .filterValues { it != null }
                .mapValues { (_, reading) -> reading!! }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = grouped.entries.toList(),
                    key = { it.key },
                ) { (providerId, reading) ->
                    ReadingCard(providerId = providerId, reading = reading)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SessionInfoHeader(
    session: com.sysop.tricorder.core.database.entity.SessionEntity,
    durationMs: Long,
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())
    val duration = Duration.ofMillis(durationMs)
    val durationText = formatDuration(duration)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = session.name,
                style = MaterialTheme.typography.headlineMedium,
                color = AccentGreen,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Started: ${dateFormatter.format(Instant.ofEpochMilli(session.startTime))}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Duration: $durationText",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Location: %.4f, %.4f".format(session.latitude, session.longitude),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    isPlaying: Boolean,
    speed: Float,
    progress: Float,
    currentTimeMs: Long,
    durationMs: Long,
    onTogglePlayback: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onSeek: (Float) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Play/pause + speed controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onTogglePlayback) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = AccentGreen,
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1f, 2f, 5f).forEach { s ->
                        FilterChip(
                            selected = speed == s,
                            onClick = { onSpeedChange(s) },
                            label = { Text("${s.toInt()}x") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentGreen,
                                selectedLabelColor = MaterialTheme.colorScheme.background,
                            ),
                        )
                    }
                }
            }

            // Progress slider
            Slider(
                value = progress,
                onValueChange = onSeek,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = AccentGreen,
                    activeTrackColor = AccentGreen,
                ),
            )

            // Time labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatDuration(Duration.ofMillis(currentTimeMs)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                Text(
                    text = formatDuration(Duration.ofMillis(durationMs)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun ReadingCard(
    providerId: String,
    reading: ReadingEntity,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = providerId,
                    style = MaterialTheme.typography.titleMedium,
                    color = AccentGreen,
                )
                Text(
                    text = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.ofEpochMilli(reading.timestamp)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Display values as key-value pairs
            Text(
                text = reading.values,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 3,
            )

            if (reading.latitude != null && reading.longitude != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "%.5f, %.5f".format(reading.latitude, reading.longitude),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        }
    }
}

private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart()
    val seconds = duration.toSecondsPart()
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
