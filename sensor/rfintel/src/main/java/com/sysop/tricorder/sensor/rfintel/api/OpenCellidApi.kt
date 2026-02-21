package com.sysop.tricorder.sensor.rfintel.api

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenCellidApi {
    @GET("cell/get")
    suspend fun getCellTower(
        @Query("key") apiKey: String,
        @Query("mcc") mcc: Int,
        @Query("mnc") mnc: Int,
        @Query("lac") lac: Int,
        @Query("cellid") cellId: Int,
        @Query("format") format: String = "json",
    ): OpenCellidResponse
}

@JsonClass(generateAdapter = true)
data class OpenCellidResponse(
    val lat: Double?,
    val lon: Double?,
    val range: Int?,
    val samples: Int?,
    val status: String?,
)
