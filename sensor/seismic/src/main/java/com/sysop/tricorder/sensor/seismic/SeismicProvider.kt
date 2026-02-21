package com.sysop.tricorder.sensor.seismic

import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.seismic.api.UsgsApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class SeismicProvider @Inject constructor(
    private val api: UsgsApi,
) : SensorProvider {

    override val id = "seismic"
    override val name = "Earthquakes"
    override val category = SensorCategory.SEISMIC

    override fun availability() = SensorAvailability.AVAILABLE

    override fun readings(): Flow<SensorReading> = flow {
        while (true) {
            try {
                val sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS).toString().take(10)
                val response = api.getEarthquakes(
                    startTime = sevenDaysAgo,
                    latitude = 0.0,
                    longitude = 0.0,
                )
                response.features?.forEach { feature ->
                    val props = feature.properties ?: return@forEach
                    val coords = feature.geometry?.coordinates ?: return@forEach
                    if (coords.size >= 3) {
                        emit(SensorReading(
                            providerId = id,
                            category = category,
                            timestamp = Instant.now(),
                            values = buildMap {
                                props.mag?.let { put("magnitude", it) }
                                put("depth_km", coords[2])
                                put("longitude", coords[0])
                                put("latitude", coords[1])
                                props.time?.let { put("time_epoch", it.toDouble()) }
                            },
                            labels = mapOf(
                                "place" to (props.place ?: "Unknown"),
                            ),
                            latitude = coords[1],
                            longitude = coords[0],
                        ))
                    }
                }
            } catch (_: Exception) {}
            delay(300_000) // 5 minutes
        }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.CIRCLES)
}
