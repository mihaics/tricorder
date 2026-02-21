package com.sysop.tricorder.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sysop.tricorder.core.database.converter.Converters
import com.sysop.tricorder.core.database.dao.SessionDao
import com.sysop.tricorder.core.database.entity.ReadingEntity
import com.sysop.tricorder.core.database.entity.SessionEntity

@Database(
    entities = [SessionEntity::class, ReadingEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class TricorderDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
