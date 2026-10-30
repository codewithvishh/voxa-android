package com.voxa.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.voxa.music.data.model.HomeFeedResponse
import com.voxa.music.data.model.Song
import com.voxa.music.data.remote.ApiClient
import com.voxa.music.data.repository.MusicRepository
import com.voxa.music.playback.PlaybackManager
import com.voxa.music.playback.PlaybackState
import com.voxa.music.ui.components.TrackRow
import com.voxa.music.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    playbackManager: PlaybackManager,
    playbackState: PlaybackState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { MusicRepository(ApiClient.getService(context)) }
    val scope = rememberCoroutineScope()

    var feedResponse by remember { mutableStateOf<HomeFeedResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun loadFeed() {
        isLoading = true
        errorMessage = null
        scope.launch {
            val result = repository.getHomeFeed()
            result.onSuccess {
                feedResponse = it
                isLoading = false
            }.onFailure {
                errorMessage = it.message ?: "Failed to connect to server"
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadFeed()
    }

    Scaffold(
        containerColor = VoxaBackground,
        modifier = modifier
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VoxaPrimary)
            }
        } else if (errorMessage != null && feedResponse == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Unable to connect to server", color = VoxaTextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = errorMessage ?: "", color = VoxaTextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { loadFeed() },
                        colors = ButtonDefaults.buttonColors(containerColor = VoxaPrimary)
                    ) {
                        Text("Retry", color = VoxaTextPrimary)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Top Brand Bar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VOXA",
                            color = VoxaPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STREAM",
                            color = VoxaTextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Trending Section (Horizontal Scroll)
                val trending = feedResponse?.trending ?: emptyList()
                if (trending.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Trending Now", subtitle = "Top streams across all genres")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(trending) { song ->
                                TrendingCard(
                                    song = song,
                                    isPlaying = playbackState.currentSong?.id == song.id,
                                    onClick = { playbackManager.playSong(song, trending) }
                                )
                            }
                        }
                    }
                }

                // Era Mixes / Stations
                val eraMixes = feedResponse?.eraMixes ?: emptyList()
                eraMixes.forEach { era ->
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        SectionHeader(title = era.eraName, subtitle = era.description ?: "Dynamic era recommendations")
                    }
                    items(era.songs) { song ->
                        TrackRow(
                            song = song,
                            isPlayingThis = playbackState.currentSong?.id == song.id,
                            onSongClick = { playbackManager.playSong(song, era.songs) },
                            onLikeClick = { scope.launch { repository.toggleLike(song.id) } }
                        )
                    }
                }

                // New Releases List
                val newReleases = feedResponse?.newReleases ?: emptyList()
                if (newReleases.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        SectionHeader(title = "New Releases", subtitle = "Fresh lossless tracks added today")
                    }
                    items(newReleases) { song ->
                        TrackRow(
                            song = song,
                            isPlayingThis = playbackState.currentSong?.id == song.id,
                            onSongClick = { playbackManager.playSong(song, newReleases) },
                            onLikeClick = { scope.launch { repository.toggleLike(song.id) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(text = title, color = VoxaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = subtitle, color = VoxaTextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun TrendingCard(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = song.coverUrl ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=400",
            contentDescription = song.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(VoxaSurface)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = song.title,
            color = if (isPlaying) VoxaPrimary else VoxaTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = song.artist ?: "Voxa Artist",
            color = VoxaTextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
