package com.angeldevtech.gol.ui.screens.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.common.Player
import androidx.media3.datasource.HttpDataSource
import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.domain.usecases.ExtractM3U8UrlUseCase
import com.angeldevtech.gol.domain.usecases.GetScheduleItemByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getScheduleItemById: GetScheduleItemByIdUseCase,
    private val extractM3u8UrlUseCase: ExtractM3U8UrlUseCase,
    private val application: Application
) : ViewModel(), Player.Listener {
    private val _uiState = MutableStateFlow<PlayerUIState>(PlayerUIState.Loading)
    val uiState: StateFlow<PlayerUIState> = _uiState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var fetchJob: Job? = null

    private val itemId: Int = checkNotNull(savedStateHandle["scheduleItemId"]) {
        "¡Ups! Falta el código del evento"
    }

    private var currentScheduleItem: ScheduleItem? = null
    private var currentSelectedIndex: Int = 0

    private var isListenerAttached = false

    init {
        loadContentForEmbedIndex(0)
    }

    private fun loadContentForEmbedIndex(embedIndex: Int, isSwitchingSource: Boolean = false) {
        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            _uiState.update {
                if (it is PlayerUIState.Success && isSwitchingSource) {
                    it.copy(isLoadingNewSource = true, sourceSwitchError = null) // Show loading for switch
                } else {
                    PlayerUIState.Loading // Initial loading state
                }
            }

            try {
                // Fetch schedule item only if not already loaded or switching source
                val scheduleItem = currentScheduleItem ?: getScheduleItemById(itemId)?.also {
                    currentScheduleItem = it // Cache it
                } ?: throw ItemNotFoundException(itemId)


                if (embedIndex < 0 || embedIndex >= scheduleItem.embeds.size) {
                    throw InvalidEmbedIndexException(embedIndex, scheduleItem.id)
                }
                currentSelectedIndex = embedIndex // Update selected index

                val embed = scheduleItem.embeds[embedIndex]
                val embedUrl = embed.url
                if (embedUrl.isBlank()) {
                    throw NoEmbedUrlException(itemId, embedIndex)
                }

                // Extract the M3U8 URL
                extractM3u8UrlUseCase(embedUrl)
                    .onSuccess { m3u8Url ->
                        initializePlayer(m3u8Url, scheduleItem, embedIndex) // Pass index
                        // Update state to Success, ensuring player is not null
                        exoPlayer?.let { player ->
                            _uiState.update {
                                PlayerUIState.Success(
                                    scheduleItem = scheduleItem,
                                    player = player,
                                    selectedEmbedIndex = embedIndex,
                                    isLoadingNewSource = false, // Finished loading source
                                    sourceSwitchError = null
                                )
                            }
                        } ?: throw PlayerInitializationException("Player became null after initialization")


                    }
                    .onFailure { extractionError ->
                        // Log extractionError
                        handleLoadingError(
                            "Could not load video stream: ${extractionError.localizedMessage}",
                            isSwitchingSource
                        )
                    }

            } catch (e: Exception) {
                // Log e
                handleLoadingError("Error loading content: ${e.localizedMessage}", isSwitchingSource)
            }
        }
    }

    private fun handleLoadingError(errorMessage: String, isSwitchingSource: Boolean) {
        _uiState.update { currentState ->
            if (currentState is PlayerUIState.Success && isSwitchingSource) {
                // If switching failed, revert to previous Success state but show error
                currentState.copy(
                    isLoadingNewSource = false,
                    sourceSwitchError = errorMessage // Show source-specific error
                )
            } else {
                // If initial load failed, show general error state
                PlayerUIState.Error(errorMessage)
            }
        }
    }

    private fun initializePlayer(m3u8Url: String, scheduleItem: ScheduleItem, embedIndex: Int) {
        // Release previous instances *before* creating new ones
        releasePlayer()

        try {
            exoPlayer = ExoPlayer.Builder(application).build().apply {
                val mediaItem = MediaItem.Builder()
                    .setUri(m3u8Url)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(scheduleItem.name)
                            .setSubtitle(
                                scheduleItem.embeds.getOrNull(embedIndex)?.name
                                    ?: scheduleItem.leagueName
                            )
                            .setArtist(scheduleItem.leagueName)
                            .build()
                    )
                    .build()

                setMediaItem(mediaItem)
                playWhenReady = true

                addListener(this@PlayerViewModel)
                isListenerAttached = true

                prepare()
            }

            mediaSession = MediaSession.Builder(application, exoPlayer!!)
                .setId("PlayerMediaSession_${itemId}_${embedIndex}") // Include index in ID
                .build()

        } catch (e: Exception) {
            releasePlayer() // Clean up if failed
            throw PlayerInitializationException("Failed to initialize ExoPlayer/MediaSession", e)
        }

        exoPlayer?.let { player ->
            _uiState.update {
                // Make sure playbackError is null when emitting a fresh Success state
                PlayerUIState.Success(
                    scheduleItem = scheduleItem,
                    player = player,
                    selectedEmbedIndex = embedIndex,
                    isLoadingNewSource = false,
                    sourceSwitchError = null,
                    playbackError = null // Explicitly clear playback error here
                )
            }
        } ?: throw PlayerInitializationException("Player became null after initialization")
    }

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun seekToLive(){
        exoPlayer?.let {
            it.seekToDefaultPosition()
            if (!it.isPlaying) {
                it.play()
            }
        }
    }

    fun changeEmbedSource(newEmbedIndex: Int) {
        if (newEmbedIndex != currentSelectedIndex) {
            // Store the current item before starting load, in case loadContent needs it
            val item = (uiState.value as? PlayerUIState.Success)?.scheduleItem ?: currentScheduleItem
            if(item != null) {
                currentScheduleItem = item // Ensure it's cached
                _uiState.update { if (it is PlayerUIState.Success) it.copy(playbackError = null) else it }
                loadContentForEmbedIndex(newEmbedIndex, isSwitchingSource = true)
            } else {
                // Handle case where item is somehow null - perhaps show error
                _uiState.update { PlayerUIState.Error("¡Ups! No se puede cambiar de fuente, faltan los datos del evento") }
            }
        }
    }

    private fun releasePlayer() {
        try {
            mediaSession?.release()
            mediaSession = null
            exoPlayer?.let { player ->
                // *** Remove the listener ***
                if (isListenerAttached) {
                    player.removeListener(this)
                    isListenerAttached = false
                }
                player.stop()
                player.clearMediaItems()
                player.release()
            }
            exoPlayer = null
        } catch (e: Exception) {
            // Log.e("PlayerViewModel", "Error releasing player resources", e)
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        // This is where we catch the errors like the 404!
        Log.e("PlayerViewModel", "Player Error Occurred", error)

        val errorMessage = when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
            PlaybackException.ERROR_CODE_IO_UNSPECIFIED -> "Network error. Please check connection."
            PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "Stream segment not found (404). Stream might be unstable." // Specific 404
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
                // Check for specific HTTP errors if possible
                val cause = error.cause
                if (cause is HttpDataSource.InvalidResponseCodeException) {
                    "HTTP error ${cause.responseCode}. Stream might be unavailable."
                } else {
                    "Network error fetching stream data."
                }
            }
            PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
            PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED -> "Error parsing stream data."
            PlaybackException.ERROR_CODE_DRM_UNSPECIFIED, // Handle DRM errors if applicable
            PlaybackException.ERROR_CODE_DRM_SCHEME_UNSUPPORTED,
            PlaybackException.ERROR_CODE_DRM_PROVISIONING_FAILED,
            PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED -> "DRM protected content error."
            else -> "An unexpected playback error occurred (${error.errorCodeName})."
        }

        // Update the UI State to show the error
        _uiState.update { currentState ->
            // Keep schedule item data if we were in success state, but show error overlay
            if (currentState is PlayerUIState.Success) {
                // Option 1: Modify Success state to include a transient player error
                // currentState.copy(playerError = errorMessage) // Need to add 'playerError:String?' to Success state

                // Option 2: Transition to a dedicated Error state (might be cleaner)
                currentState.copy(playbackError = errorMessage) // Set the playbackError field
            } else {
                // If error happened during initial load etc.
                PlayerUIState.Error(errorMessage)
            }
        }

        // Maybe attempt recovery? (Use with caution)
        attemptPlayerRecovery()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        // This might be useful for updating UI elements reactively elsewhere
        Log.d("PlayerViewModel", "IsPlaying changed: $isPlaying")
    }

    fun attemptPlayerRecovery() {
        viewModelScope.launch {
            Log.w("PlayerViewModel", "Attempting player recovery after error...")
            // Basic recovery: Re-initialize with the current source index
            val currentItem = currentScheduleItem
            val currentIndex = currentSelectedIndex
            if (currentItem != null) {
                // Small delay before retrying
                Log.d("PlayerViewModel", "Re-calling loadContentForEmbedIndex for recovery.")
                // Reload the same source again. This might trigger loading UI state.
                loadContentForEmbedIndex(currentIndex, isSwitchingSource = false)
            } else {
                Log.e("PlayerViewModel", "Cannot recover, currentScheduleItem is null.")
                _uiState.update { PlayerUIState.Error("Cannot recover player: data missing.")}
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        fetchJob?.cancel()
        releasePlayer()
        currentScheduleItem = null
    }

    class ItemNotFoundException(itemId: Int) : RuntimeException("¡Ups! El ID del evento $itemId no fue encontrado.")
    class InvalidEmbedIndexException(index: Int, itemId: Int) : RuntimeException("¡Ups! Fuente $index invalida del evento $itemId.")
    class NoEmbedUrlException(itemId: Int, index: Int) : RuntimeException("¡Ups! El evento $itemId no tiene una url válida para la fuente $index.")
    class PlayerInitializationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
}