package com.sysop.tricorder.feature.session.di

import com.squareup.moshi.Moshi
import com.sysop.tricorder.core.database.dao.SessionDao
import com.sysop.tricorder.core.sensorapi.SensorRegistry
import com.sysop.tricorder.feature.session.SessionRecorder
import com.sysop.tricorder.feature.session.export.SessionExporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides
    @Singleton
    fun provideSessionRecorder(
        sensorRegistry: SensorRegistry,
        sessionDao: SessionDao,
        moshi: Moshi,
    ): SessionRecorder = SessionRecorder(sensorRegistry, sessionDao, moshi, Dispatchers.Default)

    @Provides
    @Singleton
    fun provideSessionExporter(
        moshi: Moshi,
    ): SessionExporter = SessionExporter(moshi)
}
