package com.voxa.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voxa.music.data.model.Song
import com.voxa.music.data.remote.ApiClient
import com.voxa.music.data.repository.MusicRepository
import com.voxa.music.playback.PlaybackManager
import com.voxa.music.playback.PlaybackState
import com.voxa.music.ui.components.TrackRow
import com.voxa.music.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    playbackManager: PlaybackManager,
    playbackState: PlaybackState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { MusicRepository(ApiClient.getService(context)) }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Song>>(emptyList()) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    fun executeSearch(term: String) {
        if (term.isBlank()) {
            searchResults = emptyList()
            return
        }
        isSearching = true
        scope.launch {
            val result = repository.searchSongs(term)
            result.onSuccess {
                searchResults = it
                isSearching = false
            }.onFailure {
                isSearching = false
            }
        }
    }

    // Debounced autocomplete suggestions
    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length >= 2) {
            delay(300)
            val result = repository.getSuggestions(searchQuery.trim())
            result.onSuccess { suggestions = it }
        } else {
            suggestions = emptyList()
        }
    }

    Scaffold(
        containerColor = VoxaBackground,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.isBlank()) searchResults = emptyList()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("Search songs, artists, eras...", color = VoxaTextSecondary) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = VoxaPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            searchResults = emptyList()
                            suggestions = emptyList()
                        }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = VoxaTextSecondary)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VoxaPrimary,
                    unfocusedBorderColor = VoxaDivider,
                    focusedContainerColor = VoxaSurface,
                    unfocusedContainerColor = VoxaSurface,
                    focusedTextColor = VoxaTextPrimary,
                    unfocusedTextColor = VoxaTextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Suggestion Chips
            if (suggestions.isNotEmpty() && searchResults.isEmpty()) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestions) { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = VoxaSurfaceVariant,
                            modifier = Modifier.clickable {
                                searchQuery = suggestion
                                executeSearch(suggestion)
                            }
                        ) {
                            Text(
                                text = suggestion,
                                color = VoxaTextPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Results List
            if (isSearching) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = VoxaPrimary)
                }
            } else if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No songs found for \"$searchQuery\"",
                        color = VoxaTextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp, top = 8.dp)
                ) {
                    items(searchResults) { song ->
                        TrackRow(
                            song = song,
                            isPlayingThis = playbackState.currentSong?.id == song.id,
                            onSongClick = { playbackManager.playSong(song, searchResults) },
                            onLikeClick = { scope.launch { repository.toggleLike(song.id) } }
                        )
                    }
                }
            }
        }
    }
}
