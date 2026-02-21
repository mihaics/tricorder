package com.sysop.tricorder.sensor.tides

import com.sysop.tricorder.core.model.*
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
) : SensorProvider {

    override val id = "tides"
    override val name = "Tides & Water"
    override val category = SensorCategory.TIDES

    override fun availability() = SensorAvailability.AVAILABLE

    override fun readings(): Flow<SensorReading> = flow {
        while (true) {
            try {
                val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                val response = api.getTidePredictions(
                    station = "9414290", // San Francisco default — will be dynamic
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
            } catch (_: Exception) {}
            delay(3_600_000) // 1 hour
        }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.MARKERS)
}
