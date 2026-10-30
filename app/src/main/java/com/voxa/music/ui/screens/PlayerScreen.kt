package com.voxa.music.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.voxa.music.data.remote.ApiClient
import com.voxa.music.data.repository.MusicRepository
import com.voxa.music.playback.PlaybackManager
import com.voxa.music.playback.PlaybackState
import com.voxa.music.ui.components.LiveLyricsRibbon
import com.voxa.music.ui.theme.*
import com.voxa.music.util.LrcParser
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun PlayerScreen(
    playbackManager: PlaybackManager,
    playbackState: PlaybackState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val song = playbackState.currentSong ?: return
    val context = LocalContext.current
    val repository = remember { MusicRepository(ApiClient.getService(context)) }
    val scope = rememberCoroutineScope()

    var showLyricsModal by remember { mutableStateOf(false) }
    var userScrubbingPosition by remember { mutableStateOf<Float?>(null) }

    val currentPosition = userScrubbingPosition?.toLong() ?: playbackState.currentPositionMs
    val duration = playbackState.durationMs

    val lyricsList = remember(song.lyricsLrc) {
        LrcParser.parse(song.lyricsLrc)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF221319),
                        VoxaBackground,
                        VoxaBackground
                    )
                )
            )
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Close", tint = VoxaTextPrimary, modifier = Modifier.size(32.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLAYING FROM VOXA",
                        color = VoxaTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = song.era ?: "High-Fidelity Audio",
                        color = VoxaPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = VoxaSurfaceVariant
                ) {
                    Text(
                        text = "320 KBPS",
                        color = VoxaTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content: Artwork or Fullscreen Lyrics
            if (!showLyricsModal) {
                // High-Res Artwork
                AsyncImage(
                    model = song.coverUrl ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(VoxaSurface)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Song Title & Artist & Like Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            color = VoxaTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = song.artist ?: "Voxa Artist",
                            color = VoxaTextSecondary,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { scope.launch { repository.toggleLike(song.id) } }) {
                        Icon(
                            imageVector = if (song.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (song.isLiked) VoxaPrimary else VoxaTextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Karaoke Lyrics Ribbon
                LiveLyricsRibbon(
                    lyricsLrc = song.lyricsLrc,
                    currentPositionMs = currentPosition,
                    onClick = { showLyricsModal = true }
                )
            } else {
                // Synchronized Scrolling Lyrics View
                SynchronizedLyricsView(
                    lyrics = lyricsList,
                    currentPositionMs = currentPosition,
                    onLineClick = { timeMs -> playbackManager.seekTo(timeMs) },
                    onCloseLyrics = { showLyricsModal = false },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Seekbar Slider
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = if (duration > 0) currentPosition.toFloat() else 0f,
                    onValueChange = { userScrubbingPosition = it },
                    onValueChangeFinished = {
                        userScrubbingPosition?.let {
                            playbackManager.seekTo(it.toLong())
                            userScrubbingPosition = null
                        }
                    },
                    valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                    colors = SliderDefaults.colors(
                        thumbColor = VoxaPrimary,
                        activeTrackColor = VoxaPrimary,
                        inactiveTrackColor = VoxaSurfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatMs(currentPosition), color = VoxaTextSecondary, fontSize = 11.sp)
                    Text(formatMs(duration), color = VoxaTextSecondary, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(onClick = { playbackManager.toggleShuffle() }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackState.isShuffleEnabled) VoxaPrimary else VoxaTextSecondary
                    )
                }

                // Previous
                IconButton(onClick = { playbackManager.skipPrevious() }) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = VoxaTextPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Play / Pause Circle
                Surface(
                    shape = CircleShape,
                    color = VoxaPrimary,
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { playbackManager.togglePlayPause() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (playbackState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = VoxaTextPrimary,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play Pause",
                                tint = VoxaTextPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                // Next
                IconButton(onClick = { playbackManager.skipNext() }) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = VoxaTextPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Repeat
                IconButton(onClick = { playbackManager.toggleRepeat() }) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repeat",
                        tint = if (playbackState.isRepeatEnabled) VoxaPrimary else VoxaTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SynchronizedLyricsView(
    lyrics: List<com.voxa.music.util.LyricLine>,
    currentPositionMs: Long,
    onLineClick: (Long) -> Unit,
    onCloseLyrics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val activeIndex = remember(currentPositionMs, lyrics) {
        LrcParser.getActiveIndex(lyrics, currentPositionMs)
    }

    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && activeIndex < lyrics.size) {
            listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(VoxaSurfaceVariant.copy(alpha = 0.8f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("LIVE KARAOKE LYRICS", color = VoxaPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onCloseLyrics) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Lyrics", tint = VoxaTextSecondary)
            }
        }

        if (lyrics.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No synchronized lyrics found for this track.", color = VoxaTextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 40.dp)
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val isActive = index == activeIndex
                    Text(
                        text = line.text,
                        color = if (isActive) VoxaTextPrimary else VoxaTextSecondary.copy(alpha = 0.5f),
                        fontSize = if (isActive) 20.sp else 16.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLineClick(line.timeMs) }
                            .padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
