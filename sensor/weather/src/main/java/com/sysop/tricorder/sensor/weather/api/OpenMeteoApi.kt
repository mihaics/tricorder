package com.sysop.tricorder.sensor.weather.api

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,wind_speed_10m,wind_direction_10m,uv_index,surface_pressure",
    ): OpenMeteoResponse
}

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val current: CurrentWeather?,
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    val temperature_2m: Double?,
    val relative_humidity_2m: Double?,
    val wind_speed_10m: Double?,
    val wind_direction_10m: Double?,
    val uv_index: Double?,
    val surface_pressure: Double?,
)
