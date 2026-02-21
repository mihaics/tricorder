package com.sysop.tricorder.sensor.radiation.api

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface SafecastApi {
    @GET("measurements.json")
    suspend fun getMeasurements(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("distance") distanceKm: Int = 50,
    ): List<SafecastMeasurement>
}

@JsonClass(generateAdapter = true)
data class SafecastMeasurement(
    val latitude: Double?,
    val longitude: Double?,
    val value: Double?,
    val unit: String?,
)
