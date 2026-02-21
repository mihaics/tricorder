package com.sysop.tricorder.sensor.space.api

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface N2yoApi {
    @GET("rest/v1/satellite/above/{lat}/{lon}/0/70/0/")
    suspend fun getSatellitesAbove(
        @Path("lat") latitude: Double,
        @Path("lon") longitude: Double,
        @Query("apiKey") apiKey: String,
    ): N2yoResponse
}

@JsonClass(generateAdapter = true)
data class N2yoResponse(
    val info: N2yoInfo?,
    val above: List<N2yoSatellite>?,
)

@JsonClass(generateAdapter = true)
data class N2yoInfo(
    val satcount: Int?,
)

@JsonClass(generateAdapter = true)
data class N2yoSatellite(
    val satid: Int?,
    val satname: String?,
    val satlat: Double?,
    val satlng: Double?,
    val satalt: Double?,
)
