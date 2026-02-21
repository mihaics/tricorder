package com.sysop.tricorder.sensor.radiation

import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.radiation.api.SafecastApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import javax.inject.Inject

class RadiationProvider @Inject constructor(
    private val api: SafecastApi,
) : SensorProvider {

    override val id = "radiation"
    override val name = "Radiation"
    override val category = SensorCategory.RADIATION

    override fun availability() = SensorAvailability.AVAILABLE

    override fun readings(): Flow<SensorReading> = flow {
        while (true) {
            try {
                val measurements = api.getMeasurements(latitude = 0.0, longitude = 0.0)
                for (m in measurements) {
                    if (m.latitude != null && m.longitude != null && m.value != null) {
                        val isCpm = m.unit == "cpm"
                        emit(SensorReading(
                            providerId = id,
                            category = category,
                            timestamp = Instant.now(),
                            values = buildMap {
                                if (isCpm) {
                                    put("cpm", m.value)
                                    put("usv_h", m.value / 334.0) // approximate CPM to uSv/h
                                } else {
                                    put("usv_h", m.value)
                                }
                            },
                            latitude = m.latitude,
                            longitude = m.longitude,
                        ))
                    }
                }
            } catch (_: Exception) {}
            delay(600_000) // 10 minutes
        }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.HEATMAP)
}
