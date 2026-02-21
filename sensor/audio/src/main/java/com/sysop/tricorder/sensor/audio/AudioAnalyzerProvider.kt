package com.sysop.tricorder.sensor.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.SensorProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.time.Instant
import javax.inject.Inject

class AudioAnalyzerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : SensorProvider {

    override val id = "audio"
    override val name = "Audio Analyzer"
    override val category = SensorCategory.AUDIO

    override fun availability(): SensorAvailability {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        return if (hasPermission) SensorAvailability.AVAILABLE
        else SensorAvailability.REQUIRES_PERMISSION
    }

    override fun readings(): Flow<SensorReading> = callbackFlow {
        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT,
        ).coerceAtLeast(4096)

        val fftSize = Integer.highestOneBit(bufferSize) // Nearest power of 2

        val recorder = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                bufferSize * 4,
            )
        } catch (_: SecurityException) {
            close(); return@callbackFlow
        }

        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            close()
            return@callbackFlow
        }

        recorder.startRecording()
        val buffer = FloatArray(fftSize)

        try {
            while (isActive) {
                val read = recorder.read(buffer, 0, fftSize, AudioRecord.READ_BLOCKING)
                if (read > 0) {
                    val samples = if (read == fftSize) buffer else buffer.copyOf(Integer.highestOneBit(read))
                    val dbSpl = FftProcessor.computeDbSpl(samples)
                    val magnitudes = FftProcessor.fft(samples)
                    val peakFreq = FftProcessor.peakFrequency(magnitudes, sampleRate)

                    trySend(SensorReading(
                        providerId = id,
                        category = category,
                        timestamp = Instant.now(),
                        values = mapOf(
                            "db_spl" to dbSpl,
                            "peak_frequency_hz" to peakFreq,
                        ),
                        labels = mapOf(
                            "spectrum" to magnitudes.joinToString(",") { "%.1f".format(it) },
                        ),
                    ))
                }
            }
        } finally {
            recorder.stop()
            recorder.release()
        }

        awaitClose {}
    }.flowOn(Dispatchers.IO)

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.HEATMAP)
}
