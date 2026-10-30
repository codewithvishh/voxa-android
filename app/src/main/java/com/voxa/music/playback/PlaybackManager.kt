package com.voxa.music.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.voxa.music.data.model.Song
import com.voxa.music.data.remote.ApiClient
import com.voxa.music.data.repository.MusicRepository
import com.voxa.music.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlaybackManager private constructor(private val context: Context) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val repository = MusicRepository(ApiClient.getService(context))

    private val _state = MutableStateFlow(PlaybackState())
    val state: StateFlow<PlaybackState> = _state.asStateFlow()

    // 30-Second Play Count Audit tracking
    private var listeningAuditJob: Job? = null
    private var auditedSongId: Long? = null

    init {
        initMediaController()
        startPositionPolling()
    }

    private fun initMediaController() {
        val sessionToken = SessionToken(context, ComponentName(context, VoxaMediaService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                setupPlayerListener()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Playback init failed: ${e.message}") }
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
                handleAuditTimer(isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val isLoading = playbackState == Player.STATE_BUFFERING
                _state.update {
                    it.copy(
                        isLoading = isLoading,
                        durationMs = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                    )
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val currentSongId = mediaItem?.mediaId?.toLongOrNull()
                val activeSong = _state.value.queue.find { it.id == currentSongId }
                
                auditedSongId = null
                _state.update {
                    it.copy(
                        currentSong = activeSong,
                        durationMs = mediaController?.duration?.coerceAtLeast(0L) ?: 0L,
                        currentPositionMs = 0L
                    )
                }
                handleAuditTimer(_state.value.isPlaying)
            }
        })
    }

    fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        val controller = mediaController ?: return
        val mediaItems = queue.map { it.toMediaItem() }
        val targetIndex = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        _state.update {
            it.copy(
                currentSong = song,
                queue = queue,
                isLoading = true
            )
        }

        controller.setMediaItems(mediaItems, targetIndex, 0L)
        controller.prepare()
        controller.play()
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _state.update { it.copy(currentPositionMs = positionMs) }
    }

    fun skipNext() {
        mediaController?.let {
            if (it.hasNextMediaItem()) {
                it.seekToNextMediaItem()
            }
        }
    }

    fun skipPrevious() {
        mediaController?.let {
            if (it.hasPreviousMediaItem()) {
                it.seekToPreviousMediaItem()
            }
        }
    }

    fun toggleShuffle() {
        val currentShuffle = _state.value.isShuffleEnabled
        mediaController?.shuffleModeEnabled = !currentShuffle
        _state.update { it.copy(isShuffleEnabled = !currentShuffle) }
    }

    fun toggleRepeat() {
        val controller = mediaController ?: return
        val nextMode = if (controller.repeatMode == Player.REPEAT_MODE_OFF) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        controller.repeatMode = nextMode
        _state.update { it.copy(isRepeatEnabled = nextMode == Player.REPEAT_MODE_ONE) }
    }

    private fun handleAuditTimer(isPlaying: Boolean) {
        listeningAuditJob?.cancel()
        val currentSong = _state.value.currentSong ?: return

        if (isPlaying && auditedSongId != currentSong.id) {
            listeningAuditJob = coroutineScope.launch {
                delay(Constants.PLAY_COUNT_AUDIT_SECONDS * 1000L)
                auditedSongId = currentSong.id
                repository.recordPlay(currentSong.id)
            }
        }
    }

    private fun startPositionPolling() {
        coroutineScope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _state.update {
                            it.copy(
                                currentPositionMs = controller.currentPosition.coerceAtLeast(0L),
                                durationMs = controller.duration.coerceAtLeast(0L)
                            )
                        }
                    }
                }
                delay(300)
            }
        }
    }

    private fun Song.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist ?: "Voxa Artist")
            .setAlbumTitle(album ?: "Voxa Stream")
            .setArtworkUri(coverUrl?.let { Uri.parse(it) })
            .build()

        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(audioUrl)
            .setMediaMetadata(metadata)
            .build()
    }

    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        coroutineScope.cancel()
    }

    companion object {
        @Volatile
        private var instance: PlaybackManager? = null

        fun getInstance(context: Context): PlaybackManager {
            return instance ?: synchronized(this) {
                instance ?: PlaybackManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
