package com.sysop.tricorder.sensor.radiation.di

import com.squareup.moshi.Moshi
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.radiation.RadiationProvider
import com.sysop.tricorder.sensor.radiation.api.SafecastApi
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
abstract class RadiationModule {
    @Binds @IntoSet
    abstract fun bindRadiationProvider(impl: RadiationProvider): SensorProvider

    companion object {
        @Provides @Singleton
        fun provideSafecastApi(client: OkHttpClient, moshi: Moshi): SafecastApi =
            Retrofit.Builder()
                .baseUrl("https://api.safecast.org/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SafecastApi::class.java)
    }
}
