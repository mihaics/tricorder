package com.sysop.tricorder.sensor.seismic.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface UsgsApi {
    @GET("fdsnws/event/1/query")
    suspend fun getEarthquakes(
        @Query("format") format: String = "geojson",
        @Query("starttime") startTime: String,
        @Query("minmagnitude") minMagnitude: Double = 2.5,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("maxradiuskm") maxRadiusKm: Int = 500,
    ): UsgsResponse
}

@JsonClass(generateAdapter = true)
data class UsgsResponse(
    val features: List<UsgsFeature>?,
)

@JsonClass(generateAdapter = true)
data class UsgsFeature(
    val properties: UsgsProperties?,
    val geometry: UsgsGeometry?,
)

@JsonClass(generateAdapter = true)
data class UsgsProperties(
    val mag: Double?,
    val place: String?,
    val time: Long?,
    @Json(name = "detail") val detailUrl: String?,
)

@JsonClass(generateAdapter = true)
data class UsgsGeometry(
    val coordinates: List<Double>?,
)
