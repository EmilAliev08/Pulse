package com.example.pulse.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    // Все избранные треки
    @Query("SELECT * FROM favorite_tracks")
    fun getAllFavorites(): Flow<List<FavoriteTrackEntity>>

    // Добавить в избранное
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(track: FavoriteTrackEntity): Long

    // Убрать из избранного
    @Query("DELETE FROM favorite_tracks WHERE id = :trackId")
    suspend fun remove(trackId: Long): Int

    // В избранном ли трек
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tracks WHERE id = :trackId)")
    suspend fun isFavorite(trackId: Long): Boolean

    // Поиск всех треков конкретного исполнителя в избранном
    @Query("SELECT * FROM favorite_tracks WHERE artist = :artistName")
    fun getTracksByArtist(artistName: String): Flow<List<FavoriteTrackEntity>>

    // Получение случайных треков для дополнения волны (если у автора мало треков)
    @Query("SELECT * FROM favorite_tracks ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomTracks(limit: Int): List<FavoriteTrackEntity>
}