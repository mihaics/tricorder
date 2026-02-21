package com.sysop.tricorder.sensor.airquality.api

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WaqiApi {
    @GET("feed/geo:{lat};{lon}/")
    suspend fun getAirQuality(
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
        @Query("token") token: String,
    ): WaqiResponse
}

@JsonClass(generateAdapter = true)
data class WaqiResponse(
    val status: String?,
    val data: WaqiData?,
)

@JsonClass(generateAdapter = true)
data class WaqiData(
    val aqi: Int?,
    val iaqi: Map<String, WaqiValue>?,
)

@JsonClass(generateAdapter = true)
data class WaqiValue(
    val v: Double?,
)
