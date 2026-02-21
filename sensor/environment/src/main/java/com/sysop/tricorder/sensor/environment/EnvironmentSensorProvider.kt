package com.sysop.tricorder.sensor.environment

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

class EnvironmentSensorProvider @Inject constructor(
    private val sensorManager: SensorManager?,
) : SensorProvider {

    override val id = "environment"
    override val name = "Environment"
    override val category = SensorCategory.ENVIRONMENT

    override fun availability(): SensorAvailability {
        if (sensorManager == null) return SensorAvailability.UNAVAILABLE
        val pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        val light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        return if (pressure != null || light != null) SensorAvailability.AVAILABLE else SensorAvailability.UNAVAILABLE
    }

    override fun readings(): Flow<SensorReading> = callbackFlow {
        val sm = sensorManager ?: run { close(); return@callbackFlow }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val values = mutableMapOf<String, Double>()
                when (event.sensor.type) {
                    Sensor.TYPE_PRESSURE -> {
                        val pressure = event.values[0].toDouble()
                        values["pressure_hpa"] = pressure
                        values["altitude_m"] = SensorManager.getAltitude(
                            SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure.toFloat()
                        ).toDouble()
                    }
                    Sensor.TYPE_LIGHT -> {
                        values["light_lux"] = event.values[0].toDouble()
                    }
                    Sensor.TYPE_PROXIMITY -> {
                        values["proximity_cm"] = event.values[0].toDouble()
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

        val sensors = listOf(Sensor.TYPE_PRESSURE, Sensor.TYPE_LIGHT, Sensor.TYPE_PROXIMITY)
        sensors.forEach { type ->
            sm.getDefaultSensor(type)?.let { sensor ->
                sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            }
        }

        awaitClose { sm.unregisterListener(listener) }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.HEATMAP)
}
