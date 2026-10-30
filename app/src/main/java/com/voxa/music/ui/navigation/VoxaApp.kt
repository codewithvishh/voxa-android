package com.voxa.music.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.voxa.music.playback.PlaybackManager
import com.voxa.music.ui.components.MiniPlayerBar
import com.voxa.music.ui.screens.*
import com.voxa.music.ui.theme.*

@Composable
fun VoxaApp() {
    val context = LocalContext.current
    val playbackManager = remember { PlaybackManager.getInstance(context) }
    val playbackState by playbackManager.state.collectAsState()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var isPlayerExpanded by remember { mutableStateOf(false) }

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Library
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = VoxaBackground,
            bottomBar = {
                if (currentRoute != Screen.Auth.route) {
                    Column {
                        // Floating MiniPlayer docked right above bottom navigation
                        AnimatedVisibility(
                            visible = playbackState.currentSong != null && !isPlayerExpanded,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            MiniPlayerBar(
                                state = playbackState,
                                onTogglePlayPause = { playbackManager.togglePlayPause() },
                                onSkipNext = { playbackManager.skipNext() },
                                onExpand = { isPlayerExpanded = true }
                            )
                        }

                        // Bottom Navigation Bar
                        NavigationBar(
                            containerColor = VoxaSurface,
                            tonalElevation = 8.dp
                        ) {
                            bottomNavItems.forEach { screen ->
                                val selected = currentRoute == screen.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        screen.icon?.let { icon ->
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = screen.title,
                                                tint = if (selected) VoxaPrimary else VoxaTextSecondary
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = screen.title,
                                            color = if (selected) VoxaPrimary else VoxaTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = VoxaPrimary.copy(alpha = 0.15f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(playbackManager = playbackManager, playbackState = playbackState)
                }
                composable(Screen.Search.route) {
                    SearchScreen(playbackManager = playbackManager, playbackState = playbackState)
                }
                composable(Screen.Library.route) {
                    LibraryScreen(
                        playbackManager = playbackManager,
                        onNavigateToAuth = { navController.navigate(Screen.Auth.route) }
                    )
                }
                composable(Screen.Auth.route) {
                    AuthScreen(
                        onAuthSuccess = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // Fullscreen Expanded Player Sheet
        AnimatedVisibility(
            visible = isPlayerExpanded && playbackState.currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            PlayerScreen(
                playbackManager = playbackManager,
                playbackState = playbackState,
                onDismiss = { isPlayerExpanded = false }
            )
        }
    }
}
