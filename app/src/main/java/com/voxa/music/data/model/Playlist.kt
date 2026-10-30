package com.voxa.music.data.model

import com.google.gson.annotations.SerializedName

data class Playlist(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("isPublic") val isPublic: Boolean = true,
    @SerializedName("songCount") val songCount: Int = 0,
    @SerializedName("coverUrl") val coverUrl: String? = null,
    @SerializedName("songs") val songs: List<Song> = emptyList()
)

data class CreatePlaylistRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("isPublic") val isPublic: Boolean = true
)
