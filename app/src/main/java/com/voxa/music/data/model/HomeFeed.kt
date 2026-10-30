package com.voxa.music.data.model

import com.google.gson.annotations.SerializedName

data class HomeFeedResponse(
    @SerializedName("trending") val trending: List<Song> = emptyList(),
    @SerializedName("newReleases") val newReleases: List<Song> = emptyList(),
    @SerializedName("eraMixes") val eraMixes: List<EraSection> = emptyList()
)

data class EraSection(
    @SerializedName("eraName") val eraName: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("songs") val songs: List<Song> = emptyList()
)
