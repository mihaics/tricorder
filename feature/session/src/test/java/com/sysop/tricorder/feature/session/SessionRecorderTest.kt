package com.sysop.tricorder.feature.session

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.sysop.tricorder.core.database.dao.SessionDao
import com.sysop.tricorder.core.database.entity.ReadingEntity
import com.sysop.tricorder.core.database.entity.SessionEntity
import com.sysop.tricorder.core.model.MapOverlayConfig
import com.sysop.tricorder.core.model.SensorAvailability
import com.sysop.tricorder.core.model.SensorCategory
import com.sysop.tricorder.core.model.SensorReading
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant

class SessionRecorderTest {

    private lateinit var sessionDao: SessionDao
    private lateinit var sensorRegistry: SensorRegistry
    private lateinit var moshi: Moshi
    private lateinit var recorder: SessionRecorder

    private val insertedSessions = mutableListOf<SessionEntity>()
    private val insertedReadings = mutableListOf<ReadingEntity>()

    private val fakeProvider = object : SensorProvider {
        override val id = "test-sensor"
        override val name = "Test Sensor"
        override val category = SensorCategory.ENVIRONMENT
        override fun availability() = SensorAvailability.AVAILABLE
        override fun readings(): Flow<SensorReading> = flow {
            var count = 0
            while (true) {
                emit(
                    SensorReading(
                        providerId = "test-sensor",
                        category = SensorCategory.ENVIRONMENT,
                        timestamp = Instant.now(),
                        values = mapOf("temp" to (20.0 + count)),
                        labels = mapOf("unit" to "celsius"),
                        latitude = 47.0,
                        longitude = 8.0,
                    ),
                )
                count++
                delay(100)
            }
        }
        override fun mapOverlay(): MapOverlayConfig? = null
    }

    @Before
    fun setup() {
        sessionDao = mockk(relaxed = true)
        moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

        coEvery { sessionDao.insertSession(any()) } coAnswers {
            insertedSessions.add(firstArg())
        }
        coEvery { sessionDao.updateSession(any()) } coAnswers {
            // no-op
        }
        coEvery { sessionDao.insertReadings(any()) } coAnswers {
            insertedReadings.addAll(firstArg<List<ReadingEntity>>())
        }
        coEvery { sessionDao.insertReading(any()) } coAnswers {
            insertedReadings.add(firstArg())
        }

        sensorRegistry = SensorRegistry(setOf(fakeProvider))
        recorder = SessionRecorder(sensorRegistry, sessionDao, moshi)
    }

    @Test
    fun `startRecording sets isRecording to true`() = runTest {
        assertThat(recorder.isRecording.value).isFalse()

        recorder.startRecording(
            name = "Test Session",
            latitude = 47.3769,
            longitude = 8.5417,
            activeProviderIds = listOf("test-sensor"),
        )

        assertThat(recorder.isRecording.value).isTrue()
        assertThat(recorder.currentSession.value).isNotNull()
        assertThat(recorder.currentSession.value!!.name).isEqualTo("Test Session")

        // Clean up
        recorder.stopRecording()
    }

    @Test
    fun `startRecording creates session entity in database`() = runTest {
        recorder.startRecording(
            name = "DB Session",
            latitude = 47.3769,
            longitude = 8.5417,
            activeProviderIds = listOf("test-sensor"),
        )

        // Give the coroutine time to insert
        delay(200)

        assertThat(insertedSessions).isNotEmpty()
        val entity = insertedSessions.first()
        assertThat(entity.name).isEqualTo("DB Session")
        assertThat(entity.latitude).isEqualTo(47.3769)
        assertThat(entity.longitude).isEqualTo(8.5417)
        assertThat(entity.endTime).isNull()
        assertThat(entity.activeProviders).isEqualTo("test-sensor")

        recorder.stopRecording()
    }

    @Test
    fun `readings get buffered and batch inserted`() = runTest {
        recorder.startRecording(
            name = "Buffer Test",
            latitude = 47.0,
            longitude = 8.0,
            activeProviderIds = listOf("test-sensor"),
        )

        // Wait for readings to be collected and flushed (>1 second for buffer flush)
        delay(1_500)

        recorder.stopRecording()

        // Verify readings were batch inserted
        assertThat(insertedReadings).isNotEmpty()
        val reading = insertedReadings.first()
        assertThat(reading.providerId).isEqualTo("test-sensor")
        assertThat(reading.values).contains("temp")
    }

    @Test
    fun `stopRecording sets endTime and returns session`() = runTest {
        recorder.startRecording(
            name = "Stop Test",
            latitude = 47.0,
            longitude = 8.0,
            activeProviderIds = listOf("test-sensor"),
        )

        delay(200)

        val session = recorder.stopRecording()

        assertThat(session).isNotNull()
        assertThat(session!!.endTime).isNotNull()
        assertThat(session.name).isEqualTo("Stop Test")
        assertThat(recorder.isRecording.value).isFalse()
        assertThat(recorder.currentSession.value).isNull()

        coVerify { sessionDao.updateSession(any()) }
    }

    @Test
    fun `stopRecording when not recording returns null`() = runTest {
        val result = recorder.stopRecording()
        assertThat(result).isNull()
    }

    @Test
    fun `startRecording while already recording is ignored`() = runTest {
        recorder.startRecording(
            name = "First",
            latitude = 47.0,
            longitude = 8.0,
            activeProviderIds = listOf("test-sensor"),
        )

        recorder.startRecording(
            name = "Second",
            latitude = 48.0,
            longitude = 9.0,
            activeProviderIds = listOf("test-sensor"),
        )

        // Should still be the first session
        assertThat(recorder.currentSession.value!!.name).isEqualTo("First")

        recorder.stopRecording()
    }

    @Test
    fun `reading values are serialized to JSON`() = runTest {
        recorder.startRecording(
            name = "JSON Test",
            latitude = 47.0,
            longitude = 8.0,
            activeProviderIds = listOf("test-sensor"),
        )

        delay(1_500)
        recorder.stopRecording()

        assertThat(insertedReadings).isNotEmpty()
        val reading = insertedReadings.first()
        // Values should be valid JSON
        assertThat(reading.values).startsWith("{")
        assertThat(reading.values).contains("\"temp\"")
        // Labels should be valid JSON
        assertThat(reading.labels).startsWith("{")
        assertThat(reading.labels).contains("\"unit\"")
    }
}
