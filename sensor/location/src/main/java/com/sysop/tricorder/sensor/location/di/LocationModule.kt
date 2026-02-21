package com.sysop.tricorder.sensor.location.di

import android.content.Context
import android.location.LocationManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.location.LocationProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @IntoSet
    abstract fun bindLocationProvider(impl: LocationProvider): SensorProvider

    companion object {
        @Provides
        fun provideFusedLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient? =
            try { LocationServices.getFusedLocationProviderClient(context) } catch (_: Exception) { null }

        @Provides
        fun provideLocationManager(@ApplicationContext context: Context): LocationManager? =
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }
}
