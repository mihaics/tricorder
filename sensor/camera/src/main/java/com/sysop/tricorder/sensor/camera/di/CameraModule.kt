package com.sysop.tricorder.sensor.camera.di

import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.camera.CameraAnalysisProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class CameraModule {
    @Binds @IntoSet
    abstract fun bindCameraProvider(impl: CameraAnalysisProvider): SensorProvider
}
