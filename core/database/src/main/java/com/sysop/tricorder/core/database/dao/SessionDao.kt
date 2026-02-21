package com.sysop.tricorder.core.database.dao

import androidx.room.*
import com.sysop.tricorder.core.database.entity.ReadingEntity
import com.sysop.tricorder.core.database.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSession(id: String): SessionEntity?

    @Insert
    suspend fun insertReading(reading: ReadingEntity)

    @Insert
    suspend fun insertReadings(readings: List<ReadingEntity>)

    @Query("SELECT * FROM readings WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getReadingsForSession(sessionId: String): Flow<List<ReadingEntity>>

    @Query("DELETE FROM sessions WHERE startTime < :before")
    suspend fun deleteSessionsBefore(before: Long)
}
