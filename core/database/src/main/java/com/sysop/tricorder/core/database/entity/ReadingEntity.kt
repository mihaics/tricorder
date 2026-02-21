package com.sysop.tricorder.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "readings",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("sessionId"), Index("timestamp")],
)
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val timestamp: Long,
    val providerId: String,
    val values: String,
    val labels: String,
    val latitude: Double?,
    val longitude: Double?,
)
