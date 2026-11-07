package com.example.pulse.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Таблица "favorite_tracks" в базе данных
@Entity(tableName = "favorite_tracks")
data class FavoriteTrackEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val coverUrl: String,
    val previewUrl: String,
    val genre: String
)

// Track запись для базы
fun Track.toEntity(): FavoriteTrackEntity {
    return FavoriteTrackEntity(id, title, artist, album, coverUrl, previewUrl, genre)
}

// Запись из базы в Track
fun FavoriteTrackEntity.toTrack(): Track {
    return Track(id, title, artist, album, coverUrl, previewUrl, genre)
}