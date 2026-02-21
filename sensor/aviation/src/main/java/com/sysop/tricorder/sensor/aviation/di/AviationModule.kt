package com.sysop.tricorder.sensor.aviation.di

import com.squareup.moshi.Moshi
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.aviation.AviationProvider
import com.sysop.tricorder.sensor.aviation.api.OpenSkyApi
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
abstract class AviationModule {
    @Binds @IntoSet
    abstract fun bindAviationProvider(impl: AviationProvider): SensorProvider

    companion object {
        @Provides @Singleton
        fun provideOpenSkyApi(client: OkHttpClient, moshi: Moshi): OpenSkyApi =
            Retrofit.Builder()
                .baseUrl("https://opensky-network.org/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OpenSkyApi::class.java)
    }
}
