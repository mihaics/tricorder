package com.sysop.tricorder.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val startTime: Long,
    val endTime: Long?,
    val latitude: Double,
    val longitude: Double,
    val activeProviders: String,
)
