package com.example.pulse.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_stats")
data class TrackStatsEntity(
    @PrimaryKey val trackId: Long,
    val playCount: Int = 0,
    val lastPlayedAt: Long = System.currentTimeMillis()
)