package com.sysop.tricorder.sensor.tides.api

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface NoaaTidesApi {
    @GET("api/prod/datagetter")
    suspend fun getTidePredictions(
        @Query("station") station: String,
        @Query("product") product: String = "predictions",
        @Query("datum") datum: String = "MLLW",
        @Query("time_zone") timeZone: String = "lst_ldt",
        @Query("units") units: String = "metric",
        @Query("interval") interval: String = "hilo",
        @Query("format") format: String = "json",
        @Query("begin_date") beginDate: String,
        @Query("range") range: Int = 48,
    ): NoaaTideResponse
}

@JsonClass(generateAdapter = true)
data class NoaaTideResponse(
    val predictions: List<TidePrediction>?,
)

@JsonClass(generateAdapter = true)
data class TidePrediction(
    val t: String?,
    val v: String?,
    val type: String?,
)
