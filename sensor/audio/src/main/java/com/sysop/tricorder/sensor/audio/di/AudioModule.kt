package com.sysop.tricorder.sensor.audio.di

import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.audio.AudioAnalyzerProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {
    @Binds @IntoSet
    abstract fun bindAudioProvider(impl: AudioAnalyzerProvider): SensorProvider
}
