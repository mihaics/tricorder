package com.sysop.tricorder.feature.session

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.sysop.tricorder.core.database.dao.SessionDao
import com.sysop.tricorder.core.database.entity.ReadingEntity
import com.sysop.tricorder.core.database.entity.SessionEntity
import com.sysop.tricorder.core.model.SensorReading
import com.sysop.tricorder.core.model.Session
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.util.UUID

class SessionRecorder(
    private val sensorRegistry: SensorRegistry,
    private val sessionDao: SessionDao,
    private val moshi: Moshi,
) {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _currentSession = MutableStateFlow<Session?>(null)
    val currentSession: StateFlow<Session?> = _currentSession.asStateFlow()

    private val _elapsed = MutableStateFlow(Duration.ZERO)
    val elapsed: StateFlow<Duration> = _elapsed.asStateFlow()

    private var recordingScope: CoroutineScope? = null
    private var timerJob: Job? = null

    private val readingBuffer = mutableListOf<ReadingEntity>()
    private val bufferLock = Any()

    private val stringMapAdapter = moshi.adapter<Map<String, Double>>(
        Types.newParameterizedType(Map::class.java, String::class.java, Double::class.javaObjectType),
    )
    private val labelMapAdapter = moshi.adapter<Map<String, String>>(
        Types.newParameterizedType(Map::class.java, String::class.java, String::class.java),
    )

    fun startRecording(
        name: String,
        latitude: Double,
        longitude: Double,
        activeProviderIds: List<String>,
    ) {
        if (_isRecording.value) return

        val session = Session(
            id = UUID.randomUUID(),
            name = name,
            startTime = Instant.now(),
            latitude = latitude,
            longitude = longitude,
            activeProviders = activeProviderIds,
        )

        val entity = SessionEntity(
            id = session.id.toString(),
            name = session.name,
            startTime = session.startTime.toEpochMilli(),
            endTime = null,
            latitude = session.latitude,
            longitude = session.longitude,
            activeProviders = activeProviderIds.joinToString(","),
        )

        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        recordingScope = scope

        scope.launch {
            sessionDao.insertSession(entity)
        }

        _currentSession.value = session
        _isRecording.value = true
        _elapsed.value = Duration.ZERO

        // Start elapsed timer
        timerJob = scope.launch {
            while (true) {
                delay(1_000L)
                _elapsed.update { Duration.between(session.startTime, Instant.now()) }
            }
        }

        // Collect readings from active providers
        val activeProviders = activeProviderIds.mapNotNull { sensorRegistry.getProvider(it) }
        if (activeProviders.isNotEmpty()) {
            scope.launch {
                activeProviders
                    .map { it.readings() }
                    .merge()
                    .collect { reading ->
                        bufferReading(session.id.toString(), reading)
                    }
            }
        }

        // Periodic flush of buffered readings
        scope.launch {
            while (true) {
                delay(1_000L)
                flushBuffer()
            }
        }
    }

    suspend fun stopRecording(): Session? {
        if (!_isRecording.value) return null

        val session = _currentSession.value ?: return null
        val endTime = Instant.now()

        _isRecording.value = false

        // Flush remaining readings
        flushBuffer()

        // Update session with end time
        val updatedSession = session.copy(endTime = endTime)
        val entity = SessionEntity(
            id = updatedSession.id.toString(),
            name = updatedSession.name,
            startTime = updatedSession.startTime.toEpochMilli(),
            endTime = endTime.toEpochMilli(),
            latitude = updatedSession.latitude,
            longitude = updatedSession.longitude,
            activeProviders = updatedSession.activeProviders.joinToString(","),
        )
        sessionDao.updateSession(entity)

        // Clean up
        timerJob?.cancel()
        timerJob = null
        recordingScope?.cancel()
        recordingScope = null
        _currentSession.value = null
        _elapsed.value = Duration.ZERO

        return updatedSession
    }

    private fun bufferReading(sessionId: String, reading: SensorReading) {
        val entity = ReadingEntity(
            sessionId = sessionId,
            timestamp = reading.timestamp.toEpochMilli(),
            providerId = reading.providerId,
            values = stringMapAdapter.toJson(reading.values),
            labels = labelMapAdapter.toJson(reading.labels),
            latitude = reading.latitude,
            longitude = reading.longitude,
        )
        synchronized(bufferLock) {
            readingBuffer.add(entity)
        }
    }

    private suspend fun flushBuffer() {
        val batch: List<ReadingEntity>
        synchronized(bufferLock) {
            if (readingBuffer.isEmpty()) return
            batch = readingBuffer.toList()
            readingBuffer.clear()
        }
        sessionDao.insertReadings(batch)
    }
}
