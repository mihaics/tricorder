package com.sysop.tricorder.sensor.aviation

import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.DeviceLocation
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.aviation.api.OpenSkyApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject

class AviationProvider @Inject constructor(
    private val api: OpenSkyApi,
    private val deviceLocation: DeviceLocation,
) : SensorProvider {

    override val id = "aviation"
    override val name = "Aircraft Tracker"
    override val category = SensorCategory.AVIATION

    override fun availability() = SensorAvailability.AVAILABLE

    override fun readings(): Flow<SensorReading> = flow {
        while (true) {
            try {
                if (!deviceLocation.isAvailable) {
                    delay(5_000)
                    continue
                }
                val lat = deviceLocation.lat
                val lon = deviceLocation.lon
                val response = api.getAircraftStates(
                    latMin = lat - 1.0,
                    lonMin = lon - 1.0,
                    latMax = lat + 1.0,
                    lonMax = lon + 1.0,
                )
                response.states?.forEach { state ->
                    if (state.size >= 8) {
                        val callsign = (state[1] as? String)?.trim() ?: ""
                        val originCountry = (state[2] as? String) ?: ""
                        val longitude = (state[5] as? Double)
                        val latitude = (state[6] as? Double)
                        val altitude = (state[7] as? Double) ?: (state[13] as? Double)
                        val velocity = (state[9] as? Double)
                        val heading = (state[10] as? Double)
                        val verticalRate = (state[11] as? Double)

                        if (latitude != null && longitude != null) {
                            emit(SensorReading(
                                providerId = id,
                                category = category,
                                timestamp = Instant.now(),
                                values = buildMap {
                                    put("latitude", latitude)
                                    put("longitude", longitude)
                                    altitude?.let { put("altitude_m", it) }
                                    velocity?.let { put("velocity_ms", it) }
                                    heading?.let { put("heading_deg", it) }
                                    verticalRate?.let { put("vertical_rate", it) }
                                },
                                labels = mapOf(
                                    "callsign" to callsign,
                                    "origin_country" to originCountry,
                                ),
                                latitude = latitude,
                                longitude = longitude,
                            ))
                        }
                    }
                }
            } catch (_: Exception) {}
            delay(10_000) // 10 seconds
        }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.MARKERS)
}
