package com.sysop.tricorder.sensor.motion

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.SensorProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant
import javax.inject.Inject

class MotionSensorProvider @Inject constructor(
    private val sensorManager: SensorManager?,
) : SensorProvider {

    override val id = "motion"
    override val name = "Motion & Orientation"
    override val category = SensorCategory.MOTION

    override fun availability(): SensorAvailability {
        if (sensorManager == null) return SensorAvailability.UNAVAILABLE
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        return if (accel != null) SensorAvailability.AVAILABLE else SensorAvailability.UNAVAILABLE
    }

    override fun readings(): Flow<SensorReading> = callbackFlow {
        val sm = sensorManager ?: run { close(); return@callbackFlow }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val values = mutableMapOf<String, Double>()
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        values["accel_x"] = event.values[0].toDouble()
                        values["accel_y"] = event.values[1].toDouble()
                        values["accel_z"] = event.values[2].toDouble()
                    }
                    Sensor.TYPE_GYROSCOPE -> {
                        values["gyro_x"] = event.values[0].toDouble()
                        values["gyro_y"] = event.values[1].toDouble()
                        values["gyro_z"] = event.values[2].toDouble()
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        values["mag_x"] = event.values[0].toDouble()
                        values["mag_y"] = event.values[1].toDouble()
                        values["mag_z"] = event.values[2].toDouble()
                    }
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        values["rot_x"] = event.values[0].toDouble()
                        values["rot_y"] = event.values[1].toDouble()
                        values["rot_z"] = event.values[2].toDouble()
                    }
                    Sensor.TYPE_STEP_COUNTER -> {
                        values["steps"] = event.values[0].toDouble()
                    }
                }
                trySend(SensorReading(
                    providerId = id,
                    category = category,
                    timestamp = Instant.now(),
                    values = values,
                ))
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        val sensors = listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_STEP_COUNTER,
        )
        sensors.forEach { type ->
            sm.getDefaultSensor(type)?.let { sensor ->
                sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            }
        }

        awaitClose {
            sm.unregisterListener(listener)
        }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.VECTOR_FIELD)
}
