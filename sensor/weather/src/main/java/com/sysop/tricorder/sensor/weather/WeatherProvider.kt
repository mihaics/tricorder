package com.sysop.tricorder.sensor.weather

import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.weather.api.OpenMeteoApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject

class WeatherProvider @Inject constructor(
    private val api: OpenMeteoApi,
) : SensorProvider {

    override val id = "weather"
    override val name = "Weather"
    override val category = SensorCategory.WEATHER

    override fun availability() = SensorAvailability.AVAILABLE

    override fun readings(): Flow<SensorReading> = flow {
        while (true) {
            try {
                // Default location — will be updated by location provider
                val response = api.getCurrentWeather(latitude = 0.0, longitude = 0.0)
                val current = response.current
                if (current != null) {
                    emit(SensorReading(
                        providerId = id,
                        category = category,
                        timestamp = Instant.now(),
                        values = buildMap {
                            current.temperature_2m?.let { put("temperature_c", it) }
                            current.relative_humidity_2m?.let { put("humidity_pct", it) }
                            current.wind_speed_10m?.let { put("wind_speed_kmh", it) }
                            current.wind_direction_10m?.let { put("wind_direction_deg", it) }
                            current.uv_index?.let { put("uv_index", it) }
                            current.surface_pressure?.let { put("pressure_hpa", it) }
                        },
                    ))
                }
            } catch (_: Exception) {
                // Network error, retry on next cycle
            }
            delay(300_000) // 5 minutes
        }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.HEATMAP)
}
