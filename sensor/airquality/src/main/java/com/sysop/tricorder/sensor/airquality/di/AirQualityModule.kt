package com.sysop.tricorder.sensor.airquality.di

import com.squareup.moshi.Moshi
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.airquality.AirQualityProvider
import com.sysop.tricorder.sensor.airquality.api.WaqiApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AirQualityModule {
    @Binds @IntoSet
    abstract fun bindAirQualityProvider(impl: AirQualityProvider): SensorProvider

    companion object {
        @Provides @Singleton
        fun provideWaqiApi(client: OkHttpClient, moshi: Moshi): WaqiApi =
            Retrofit.Builder()
                .baseUrl("https://api.waqi.info/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(WaqiApi::class.java)
    }
}
