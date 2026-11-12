package com.example.pulse.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: TrackStatsEntity)

    @Query("SELECT * FROM track_stats ORDER BY playCount DESC LIMIT :limit")
    fun getTopTracks(limit: Int): Flow<List<TrackStatsEntity>>

    @Query("SELECT * FROM track_stats WHERE trackId = :id")
    suspend fun getStatsById(id: Long): TrackStatsEntity?
}