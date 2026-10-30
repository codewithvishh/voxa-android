package com.voxa.music.data.repository

import com.voxa.music.data.model.HomeFeedResponse
import com.voxa.music.data.model.Playlist
import com.voxa.music.data.model.Song
import com.voxa.music.data.remote.VoxaApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepository(private val apiService: VoxaApiService) {

    suspend fun getHomeFeed(): Result<HomeFeedResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getHomeFeed()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load feed: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchSongs(query: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchSongs(query)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to search: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSuggestions(query: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSuggestions(query)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get suggestions: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordPlay(songId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.recordPlay(songId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Audit error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLike(songId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.toggleLike(songId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Toggle like error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserPlaylists(): Result<List<Playlist>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getUserPlaylists()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch playlists: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
