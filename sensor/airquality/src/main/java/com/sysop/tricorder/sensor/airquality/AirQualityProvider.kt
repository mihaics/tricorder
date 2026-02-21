package com.sysop.tricorder.sensor.airquality

import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.airquality.api.WaqiApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject

class AirQualityProvider @Inject constructor(
    private val api: WaqiApi,
) : SensorProvider {

    override val id = "air-quality"
    override val name = "Air Quality"
    override val category = SensorCategory.AIR_QUALITY

    override fun availability() = SensorAvailability.REQUIRES_API_KEY

    override fun readings(): Flow<SensorReading> = flow {
        while (true) {
            try {
                val response = api.getAirQuality(lat = 0.0, lon = 0.0, token = "demo")
                val data = response.data
                if (data != null) {
                    emit(SensorReading(
                        providerId = id,
                        category = category,
                        timestamp = Instant.now(),
                        values = buildMap {
                            data.aqi?.let { put("aqi", it.toDouble()) }
                            data.iaqi?.get("pm25")?.v?.let { put("pm25", it) }
                            data.iaqi?.get("pm10")?.v?.let { put("pm10", it) }
                            data.iaqi?.get("o3")?.v?.let { put("o3", it) }
                            data.iaqi?.get("no2")?.v?.let { put("no2", it) }
                            data.iaqi?.get("so2")?.v?.let { put("so2", it) }
                            data.iaqi?.get("co")?.v?.let { put("co", it) }
                        },
                    ))
                }
            } catch (_: Exception) {}
            delay(300_000)
        }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.HEATMAP)
}
