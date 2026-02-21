package com.sysop.tricorder.sensor.rfintel

import com.sysop.tricorder.core.model.*
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.rfintel.api.OpenCellidApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.time.Instant
import javax.inject.Inject

class CellTowerDbProvider @Inject constructor(
    private val api: OpenCellidApi,
) : SensorProvider {

    override val id = "cell-tower-db"
    override val name = "Cell Tower Database"
    override val category = SensorCategory.RF

    override fun availability() = SensorAvailability.REQUIRES_API_KEY

    override fun readings(): Flow<SensorReading> = emptyFlow()
    // Cell tower lookups are triggered by the CellularProvider's readings
    // This will be connected when the RF detail view cross-references

    suspend fun lookupTower(mcc: Int, mnc: Int, lac: Int, cellId: Int, apiKey: String): SensorReading? {
        return try {
            val response = api.getCellTower(apiKey, mcc, mnc, lac, cellId)
            if (response.lat != null && response.lon != null) {
                SensorReading(
                    providerId = id,
                    category = category,
                    timestamp = Instant.now(),
                    values = buildMap {
                        put("tower_lat", response.lat)
                        put("tower_lon", response.lon)
                        response.range?.let { put("range_m", it.toDouble()) }
                        response.samples?.let { put("samples", it.toDouble()) }
                    },
                    latitude = response.lat,
                    longitude = response.lon,
                )
            } else null
        } catch (_: Exception) { null }
    }

    override fun mapOverlay() = MapOverlayConfig(type = OverlayType.CIRCLES)
}
