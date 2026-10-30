package com.voxa.music.data.model

import com.google.gson.annotations.SerializedName

data class Song(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String? = null,
    @SerializedName("album") val album: String? = null,
    @SerializedName("duration") val duration: Long = 0, // In seconds or ms
    @SerializedName("audioUrl") val audioUrl: String,
    @SerializedName("coverUrl") val coverUrl: String? = null,
    @SerializedName("era") val era: String? = null,
    @SerializedName("playCount") val playCount: Long = 0,
    @SerializedName("liked") val isLiked: Boolean = false,
    @SerializedName("lyricsLrc") val lyricsLrc: String? = null
)
