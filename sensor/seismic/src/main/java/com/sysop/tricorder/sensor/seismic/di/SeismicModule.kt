package com.sysop.tricorder.sensor.seismic.di

import com.squareup.moshi.Moshi
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.seismic.SeismicProvider
import com.sysop.tricorder.sensor.seismic.api.UsgsApi
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
abstract class SeismicModule {
    @Binds @IntoSet
    abstract fun bindSeismicProvider(impl: SeismicProvider): SensorProvider

    companion object {
        @Provides @Singleton
        fun provideUsgsApi(client: OkHttpClient, moshi: Moshi): UsgsApi =
            Retrofit.Builder()
                .baseUrl("https://earthquake.usgs.gov/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(UsgsApi::class.java)
    }
}
