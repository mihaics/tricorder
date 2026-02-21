package com.sysop.tricorder.sensor.environment.di

import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.environment.EnvironmentSensorProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class EnvironmentModule {
    @Binds
    @IntoSet
    abstract fun bindEnvironmentProvider(impl: EnvironmentSensorProvider): SensorProvider
}
