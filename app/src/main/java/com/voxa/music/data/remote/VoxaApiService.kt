package com.voxa.music.data.remote

import com.voxa.music.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface VoxaApiService {

    // Auth
    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<Map<String, Any>>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // Feed & Catalog
    @GET("api/home")
    suspend fun getHomeFeed(): Response<HomeFeedResponse>

    @GET("api/songs/{id}")
    suspend fun getSongDetails(@Path("id") id: Long): Response<Song>

    // Playback Audit & Interactions
    @POST("api/songs/{id}/play")
    suspend fun recordPlay(@Path("id") id: Long): Response<Map<String, Any>>

    @POST("api/songs/{id}/like")
    suspend fun toggleLike(@Path("id") id: Long): Response<Map<String, Any>>

    // Search
    @GET("api/search")
    suspend fun searchSongs(@Query("query") query: String): Response<List<Song>>

    @GET("api/search/suggest")
    suspend fun getSuggestions(@Query("query") query: String): Response<List<String>>

    // Playlists
    @GET("api/playlists")
    suspend fun getUserPlaylists(): Response<List<Playlist>>

    @POST("api/playlists")
    suspend fun createPlaylist(@Body request: CreatePlaylistRequest): Response<Playlist>
}
