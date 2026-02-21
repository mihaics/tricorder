package com.sysop.tricorder.sensor.tides.di

import com.squareup.moshi.Moshi
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.tides.TidesProvider
import com.sysop.tricorder.sensor.tides.api.NoaaTidesApi
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
abstract class TidesModule {
    @Binds @IntoSet
    abstract fun bindTidesProvider(impl: TidesProvider): SensorProvider

    companion object {
        @Provides @Singleton
        fun provideNoaaTidesApi(client: OkHttpClient, moshi: Moshi): NoaaTidesApi =
            Retrofit.Builder()
                .baseUrl("https://api.tidesandcurrents.noaa.gov/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(NoaaTidesApi::class.java)
    }
}
