package com.sysop.tricorder.sensor.weather.di

import com.squareup.moshi.Moshi
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.weather.WeatherProvider
import com.sysop.tricorder.sensor.weather.api.OpenMeteoApi
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
abstract class WeatherModule {
    @Binds @IntoSet
    abstract fun bindWeatherProvider(impl: WeatherProvider): SensorProvider

    companion object {
        @Provides @Singleton
        fun provideOpenMeteoApi(client: OkHttpClient, moshi: Moshi): OpenMeteoApi =
            Retrofit.Builder()
                .baseUrl("https://api.open-meteo.com/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OpenMeteoApi::class.java)
    }
}
