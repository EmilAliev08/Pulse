package com.example.pulse.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class DeezerResponse(val data: List<DeezerTrack>)

data class DeezerTrack(
    val id: Long,
    val title: String?,
    val artist: DeezerArtist?,
    val album: DeezerAlbum?,
    val preview: String?,
    val genre: DeezerGenre?
)

data class DeezerArtist(val name: String?)
data class DeezerAlbum(val title: String?, val cover_medium: String?)

data class DeezerGenre(val name: String?)

interface DeezerService {
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 100
    ): DeezerResponse
}

object MusicApi {
    private val service: DeezerService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeezerService::class.java)
    }

    suspend fun search(query: String): Result<List<Track>> {
        return try {
            val response = service.search(query)

            val tracks = response.data
                .filter { !it.preview.isNullOrEmpty() && !it.title.isNullOrEmpty() }
                .map {
                    Track(
                        id = it.id,
                        title = it.title ?: "Unknown",
                        artist = it.artist?.name ?: "Unknown",
                        album = it.album?.title ?: "",
                        coverUrl = it.album?.cover_medium ?: "",
                        previewUrl = it.preview ?: "",
                        genre = it.genre?.name ?: "Неизвестный жанр"
                    )
                }

            Result.success(tracks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}