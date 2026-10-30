package com.voxa.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.voxa.music.data.local.TokenManager
import com.voxa.music.data.model.Playlist
import com.voxa.music.data.remote.ApiClient
import com.voxa.music.data.repository.MusicRepository
import com.voxa.music.playback.PlaybackManager
import com.voxa.music.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LibraryScreen(
    playbackManager: PlaybackManager,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager.getInstance(context) }
    val isLoggedIn by tokenManager.isLoggedIn.collectAsState()
    val userEmail = remember { tokenManager.getUserEmail() }

    val repository = remember { MusicRepository(ApiClient.getService(context)) }
    val scope = rememberCoroutineScope()

    var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            isLoading = true
            scope.launch {
                val res = repository.getUserPlaylists()
                res.onSuccess { playlists = it }
                isLoading = false
            }
        }
    }

    Scaffold(
        containerColor = VoxaBackground,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            item {
                Text(
                    text = "Your Library",
                    color = VoxaTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // User Profile Card / Login Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = VoxaSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = VoxaPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = VoxaPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isLoggedIn) (userEmail ?: "Voxa Member") else "Guest Mode",
                                color = VoxaTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isLoggedIn) "Cloud Synced • High Fidelity" else "Sign in to save playlists and likes",
                                color = VoxaTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        if (!isLoggedIn) {
                            Button(
                                onClick = onNavigateToAuth,
                                colors = ButtonDefaults.buttonColors(containerColor = VoxaPrimary),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("Sign In", fontSize = 12.sp, color = VoxaTextPrimary)
                            }
                        } else {
                            TextButton(onClick = { tokenManager.clear() }) {
                                Text("Logout", color = VoxaPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Liked Songs Quick Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = VoxaSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VoxaPrimary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Liked Songs",
                                    tint = VoxaTextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Liked Songs", color = VoxaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Auto-generated playlist", color = VoxaTextSecondary, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Custom Playlists Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Playlists", color = VoxaTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { /* Create playlist dialog */ }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Playlist", tint = VoxaPrimary)
                    }
                }
            }

            if (playlists.isEmpty()) {
                item {
                    Text(
                        text = if (isLoggedIn) "No custom playlists created yet." else "Sign in to create personal playlists.",
                        color = VoxaTextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(playlists) { playlist ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = VoxaSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = playlist.coverUrl ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=300",
                                contentDescription = playlist.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(playlist.name, color = VoxaTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("${playlist.songCount} songs", color = VoxaTextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
