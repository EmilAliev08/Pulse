package com.example.pulse.data

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val coverUrl: String,
    val previewUrl: String,
    val genre: String = ""
)