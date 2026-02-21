package com.sysop.tricorder.sensor.space.di

import com.squareup.moshi.Moshi
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.space.SpaceProvider
import com.sysop.tricorder.sensor.space.api.N2yoApi
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
abstract class SpaceModule {
    @Binds @IntoSet
    abstract fun bindSpaceProvider(impl: SpaceProvider): SensorProvider

    companion object {
        @Provides @Singleton
        fun provideN2yoApi(client: OkHttpClient, moshi: Moshi): N2yoApi =
            Retrofit.Builder()
                .baseUrl("https://api.n2yo.com/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(N2yoApi::class.java)
    }
}
