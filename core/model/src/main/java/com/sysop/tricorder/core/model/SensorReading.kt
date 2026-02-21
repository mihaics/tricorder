package com.sysop.tricorder.core.model

import java.time.Instant

data class SensorReading(
    val providerId: String,
    val category: SensorCategory,
    val timestamp: Instant,
    val values: Map<String, Double>,
    val labels: Map<String, String> = emptyMap(),
    val latitude: Double? = null,
    val longitude: Double? = null,
)
