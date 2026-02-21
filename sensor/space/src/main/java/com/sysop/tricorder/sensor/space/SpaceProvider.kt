package com.sysop.tricorder.sensor.space

import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.DeviceLocation
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.space.api.N2yoApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject

class SpaceProvider @Inject constructor(
    private val api: N2yoApi,
    private val deviceLocation: DeviceLocation,
) : SensorProvider {

    override val id = "space"
    override val name = "Satellites"
    override val category = SensorCategory.SPACE

    override fun availability() = SensorAvailability.REQUIRES_API_KEY

    override fun readings(): Flow<SensorReading> = flow {
        while (true) {
            try {
                if (!deviceLocation.isAvailable) {
                    delay(5_000)
                    continue
                }
                val response = api.getSatellitesAbove(
                    latitude = deviceLocation.lat,
                    longitude = deviceLocation.lon,
                    apiKey = "",
                )
                response.above?.forEach { sat ->
                    if (sat.satlat != null && sat.satlng != null) {
                        emit(SensorReading(
                            providerId = id,
                            category = category,
                            timestamp = Instant.now(),
                            values = buildMap {
                                put("latitude", sat.satlat)
                                put("longitude", sat.satlng)
                                sat.satalt?.let { put("altitude_km", it) }
                                sat.satid?.let { put("norad_id", it.toDouble()) }
                            },
                            labels = mapOf(
                                "sat_name" to (sat.satname ?: "Unknown"),
                            ),
                            latitude = sat.satlat,
                            longitude = sat.satlng,
                        ))
                    }
                }
            } catch (_: Exception) {}
            delay(60_000) // 1 minute
        }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.MARKERS)
}
