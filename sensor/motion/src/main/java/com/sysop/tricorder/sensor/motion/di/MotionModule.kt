package com.sysop.tricorder.sensor.motion.di

import android.content.Context
import android.hardware.SensorManager
import com.sysop.tricorder.core.sensorapi.SensorProvider
import com.sysop.tricorder.sensor.motion.MotionSensorProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class MotionModule {

    @Binds
    @IntoSet
    abstract fun bindMotionProvider(impl: MotionSensorProvider): SensorProvider

    companion object {
        @Provides
        fun provideSensorManager(@ApplicationContext context: Context): SensorManager? =
            context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    }
}
