package com.sysop.tricorder.di

import android.content.Context
import androidx.room.Room
import com.sysop.tricorder.core.database.TricorderDatabase
import com.sysop.tricorder.core.database.dao.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TricorderDatabase =
        Room.databaseBuilder(context, TricorderDatabase::class.java, "tricorder.db").build()

    @Provides
    fun provideSessionDao(db: TricorderDatabase): SessionDao = db.sessionDao()
}
