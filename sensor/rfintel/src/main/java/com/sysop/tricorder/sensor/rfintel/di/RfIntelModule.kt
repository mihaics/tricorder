package com.sysop.tricorder.sensor.rfintel.di

import com.squareup.moshi.Moshi
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.rfintel.CellTowerDbProvider
import com.sysop.tricorder.sensor.rfintel.api.OpenCellidApi
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
abstract class RfIntelModule {
    @Binds @IntoSet
    abstract fun bindCellTowerDbProvider(impl: CellTowerDbProvider): SensorProvider

    companion object {
        @Provides @Singleton
        fun provideOpenCellidApi(client: OkHttpClient, moshi: Moshi): OpenCellidApi =
            Retrofit.Builder()
                .baseUrl("https://opencellid.org/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OpenCellidApi::class.java)
    }
}
