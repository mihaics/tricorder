package com.sysop.tricorder.core.model

import java.time.Instant
import java.util.UUID

data class Session(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val startTime: Instant,
    val endTime: Instant? = null,
    val latitude: Double,
    val longitude: Double,
    val activeProviders: List<String>,
)

data class TimestampedReading(
    val sessionId: UUID,
    val timestamp: Instant,
    val providerId: String,
    val values: Map<String, Double>,
    val labels: Map<String, String> = emptyMap(),
    val latitude: Double? = null,
    val longitude: Double? = null,
)
