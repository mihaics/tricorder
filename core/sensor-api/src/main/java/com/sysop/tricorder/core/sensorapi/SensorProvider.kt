package com.sysop.tricorder.core.sensorapi

import com.sysop.tricorder.core.model.*
import kotlinx.coroutines.flow.Flow

interface SensorProvider {
    val id: String
    val name: String
    val category: SensorCategory
    fun availability(): SensorAvailability
    fun readings(): Flow<SensorReading>
    fun mapOverlay(): MapOverlayConfig?
}
