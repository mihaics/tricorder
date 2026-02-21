package com.sysop.tricorder.sensor.tides

import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.DeviceLocation
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.tides.api.NoaaTidesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TidesProvider @Inject constructor(
    private val api: NoaaTidesApi,
    private val deviceLocation: DeviceLocation,
) : SensorProvider {

    override val id = "tides"
    override val name = "Tides & Water"
    override val category = SensorCategory.TIDES

    override fun availability() = SensorAvailability.AVAILABLE

    override fun readings(): Flow<SensorReading> = flow {
        while (true) {
            try {
                if (!deviceLocation.isAvailable) {
                    delay(5_000)
                    continue
                }
                // Find nearest NOAA station based on location
                val station = findNearestStation(deviceLocation.lat, deviceLocation.lon)
                if (station != null) {
                    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                    val response = api.getTidePredictions(
                        station = station,
                        beginDate = today,
                    )
                    response.predictions?.forEach { prediction ->
                        val waterLevel = prediction.v?.toDoubleOrNull()
                        if (waterLevel != null) {
                            emit(SensorReading(
                                providerId = id,
                                category = category,
                                timestamp = Instant.now(),
                                values = mapOf("water_level_m" to waterLevel),
                                labels = buildMap {
                                    prediction.type?.let { put("tide_type", it) }
                                    prediction.t?.let { put("prediction_time", it) }
                                },
                            ))
                        }
                    }
                }
            } catch (_: Exception) {}
            delay(3_600_000) // 1 hour
        }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.MARKERS)

    /** Find the nearest NOAA tide station by distance. Returns null if too far from any station. */
    private fun findNearestStation(lat: Double, lon: Double): String? {
        // Major NOAA stations with coordinates
        data class Station(val id: String, val lat: Double, val lon: Double)
        val stations = listOf(
            Station("9414290", 37.806, -122.465),  // San Francisco
            Station("8518750", 40.700, -74.014),    // New York (The Battery)
            Station("8723214", 25.768, -80.132),    // Miami
            Station("9410660", 33.720, -118.272),   // Los Angeles
            Station("8658120", 34.227, -77.954),    // Wilmington NC
            Station("8443970", 42.355, -71.053),    // Boston
            Station("8574680", 38.987, -76.481),    // Baltimore
            Station("9447130", 47.603, -122.339),   // Seattle
        )

        var nearest: Station? = null
        var minDist = Double.MAX_VALUE
        for (s in stations) {
            val dlat = s.lat - lat
            val dlon = s.lon - lon
            val dist = dlat * dlat + dlon * dlon
            if (dist < minDist) {
                minDist = dist
                nearest = s
            }
        }

        // Only return if within ~5 degrees (~500km)
        return if (minDist < 25.0) nearest?.id else null
    }
}
