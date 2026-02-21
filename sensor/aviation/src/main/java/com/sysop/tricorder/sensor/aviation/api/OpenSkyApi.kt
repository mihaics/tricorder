package com.sysop.tricorder.sensor.aviation.api

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenSkyApi {
    @GET("api/states/all")
    suspend fun getAircraftStates(
        @Query("lamin") latMin: Double,
        @Query("lomin") lonMin: Double,
        @Query("lamax") latMax: Double,
        @Query("lomax") lonMax: Double,
    ): OpenSkyResponse
}

@JsonClass(generateAdapter = true)
data class OpenSkyResponse(
    val time: Long?,
    val states: List<List<Any?>>?,
)
