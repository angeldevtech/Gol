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
import androidx.media3.common.Player
import androidx.media3.datasource.HttpDataSource
import com.angeldevtech.gol.domain.usecases.ExtractM3U8UrlUseCase
import com.angeldevtech.gol.domain.usecases.GetScheduleItemByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getScheduleItemById: GetScheduleItemByIdUseCase,
    private val extractM3u8UrlUseCase: ExtractM3U8UrlUseCase,
    private val application: Application
) : ViewModel() {
    private val itemId: Int? = savedStateHandle["scheduleItemId"]

    private val _uiState = MutableStateFlow<PlayerUIState>(PlayerUIState.Loading)
    val uiState: StateFlow<PlayerUIState> = _uiState.asStateFlow()

    private var player: ExoPlayer? = null
    private var overlayAutoHideJob: Job? = null
    private var pauseTimerJob: Job? = null
    private val pauseThreshold = 2_000L

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val currentState = _uiState.value
            if (currentState is PlayerUIState.Success) {
                if (isPlaying) {
                    if (pauseTimerJob?.isCompleted == true) {
                        _uiState.value = currentState.copy(
                            isPlaying = true,
                            isLoadingNewSource = false,
                        )
                    } else {
                        pauseTimerJob?.cancel()
                        _uiState.value = currentState.copy(
                            isPlaying = true,
                            isLoadingNewSource = false,
                            isLive = true
                        )
                    }
                    overlayAutoHideJob = hideOverlay()
                } else {
                    _uiState.value = currentState.copy(
                        isPlaying = false,
                    )
                    startPauseTimer()
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            cancelOverlayAutoHide()
            cancelPauseTimer()

            val errorMessage = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
                PlaybackException.ERROR_CODE_IO_UNSPECIFIED -> "¡Ups! Hay un error de internet, por favor revisa tu conexión."

                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "¡Ups! El segmento de la transmisión no fue encontrado (404) o está inestable."
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
                    val cause = error.cause
                    if (cause is HttpDataSource.InvalidResponseCodeException) {
                        "¡Ups! Error HTTP ${cause.responseCode}. La transmisión podría no estar disponible."
                    } else {
                        "¡Ups! Error de red al obtener datos de la transmisión."
                    }
                }

                PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED -> "¡Ups! Hubo un error al analizar los datos de la transmisión."

                PlaybackException.ERROR_CODE_DRM_UNSPECIFIED,
                PlaybackException.ERROR_CODE_DRM_SCHEME_UNSUPPORTED,
                PlaybackException.ERROR_CODE_DRM_PROVISIONING_FAILED,
                PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED -> "¡Ups! Hubo un error de contenido protegido DRM."

                else -> "¡Ups! Se produjo un error de reproducción inesperado (${error.errorCodeName})."
            }

            val currentState = _uiState.value
            if (currentState is PlayerUIState.Success) {
                _uiState.value = currentState.copy(
                    isOverlayVisible = true,
                    isLoadingNewSource = false,
                    error = errorMessage
                )
            }
        }
    }

    fun onLoad(){
        Log.d("PLAYER", "onLoad: START")
        if (_uiState.value !is PlayerUIState.Success){
            Log.d("PLAYER", "onLoad: NO SUCCESS")
            loadItemContent()
        } else {
            Log.d("PLAYER", "onLoad: SUCCESS")
            attemptPlayerRecovery()
        }
    }

    private fun loadItemContent(){
        viewModelScope.launch {
            _uiState.value = PlayerUIState.Loading

            if (itemId == null) {
                _uiState.value = PlayerUIState.Error("¡Ups! Hubo un error al pasar el id del evento")
                return@launch
            }

            val scheduleItem = getScheduleItemById(itemId)

            if (scheduleItem == null) {
                _uiState.value = PlayerUIState.Error("¡Ups! No se pudo encontrar datos del evento")
                return@launch
            }

            initializePlayer()

            if (player == null){
                _uiState.value = PlayerUIState.Error("¡Ups! No se pudo iniciar el reproductor de video")
                return@launch
            }

            _uiState.value = PlayerUIState.Success(scheduleItem, 0)
            loadContentForIndex(0)
        }
    }

    private fun initializePlayer() {
        try {
            if (player == null){
                player = ExoPlayer.Builder(application).build().apply {
                    addListener(playerListener)
                }
            }
        } catch (e: Exception) {
            _uiState.value = PlayerUIState.Error("¡Ups! No se pudo iniciar el reproductor de video: ${e.localizedMessage}")
        }
    }

    fun getPlayer(): ExoPlayer {
        initializePlayer()
        return player!!
    }

    fun selectEmbedIndex(embedIndex: Int) {
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success) {
            Log.d("PLAYER", "selectEmbedIndex: START")

            if (
                currentState.selectedEmbedIndex != embedIndex ||
                currentState.error != null ||
                !currentState.isPlaying
            ) {
                cancelOverlayAutoHide()
                cancelPauseTimer()

                Log.d("PLAYER", "selectEmbedIndex: PROCESS")
                player?.apply {
                    stop()
                    clearMediaItems()
                }
                _uiState.value = currentState.copy(
                    selectedEmbedIndex = embedIndex,
                    isOverlayVisible = true,
                    isLoadingNewSource = true,
                    error = null
                )
                loadContentForIndex(embedIndex)
            }
        }
    }

    private fun loadContentForIndex(embedIndex: Int) {
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success) {
            viewModelScope.launch {
                Log.d("PLAYER", "loadContentForIndex: START")
                try {
                    val embedUrl = currentState.scheduleItem.embeds[embedIndex].url

                    extractM3u8UrlUseCase(embedUrl)
                        .onSuccess { m3u8Url ->
                            val mediaItem = MediaItem.Builder()
                                .setUri(m3u8Url)
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(currentState.scheduleItem.name)
                                        .setSubtitle(
                                            currentState.scheduleItem.embeds.getOrNull(embedIndex)?.name
                                                ?: currentState.scheduleItem.leagueName
                                        )
                                        .setArtist(currentState.scheduleItem.leagueName)
                                        .build()
                                )
                                .build()

                            player?.apply {
                                setMediaItem(mediaItem)
                                prepare()
                                playWhenReady = true
                            }
                            Log.d("PLAYER", "loadContentForIndex: SUCCESS?")
                        }
                        .onFailure { extractionError ->
                            _uiState.value = currentState.copy(
                                isLoadingNewSource = false,
                                error = extractionError.localizedMessage
                            )
                        }
                } catch (e: Exception) {
                    _uiState.value = currentState.copy(
                        isLoadingNewSource = false,
                        error = e.localizedMessage
                    )
                }
            }
        }
    }

    fun togglePlayPause() {
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success) {
            player?.let {
                if (it.isPlaying) {
                    it.pause()
                } else {
                    it.play()
                }
            }
        }
    }

    fun seekToLive() {
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success) {
            player?.let {
                it.seekToDefaultPosition()
                if (!it.isPlaying) {
                    it.play()
                }
            }
            cancelPauseTimer()
        }
    }

    fun showOverlayTemporarily() {
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success) {
            if (overlayAutoHideJob != null){
                _uiState.value = currentState.copy(
                    isOverlayVisible = true
                )
                overlayAutoHideJob?.cancel()
                overlayAutoHideJob = hideOverlay()
            }
        }
    }

    private fun startPauseTimer() {
        if (pauseTimerJob?.isCompleted == true){
            Log.d("PLAYER", "startPauseTimer: COMPLETED")
            return
        }
        pauseTimerJob?.cancel()
        pauseTimerJob = viewModelScope.launch {
            delay(pauseThreshold)
            val currentState = _uiState.value
            if (currentState is PlayerUIState.Success && !currentState.isPlaying) {
                Log.d("PLAYER", "startPauseTimer: TO FALSE")
                _uiState.value = currentState.copy(isLive = false)
            }
        }
    }

    private fun hideOverlay(): Job {
        return viewModelScope.launch {
            delay(5000)
            val currentState = _uiState.value
            if (currentState is PlayerUIState.Success) {
                _uiState.value = currentState.copy(
                    isOverlayVisible = false
                )
            }
        }
    }

    private fun cancelOverlayAutoHide() {
        overlayAutoHideJob?.cancel()
        overlayAutoHideJob = null
    }

    private fun cancelPauseTimer() {
        Log.d("PLAYER", "cancelPauseTimer")
        pauseTimerJob?.cancel()
        pauseTimerJob = null
    }

    fun pausePlayer() {
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success) {
            player?.apply {
                stop()
            }
        }
    }

    private fun releasePlayer() {
        player?.let { player ->
            player.removeListener(playerListener)
            player.stop()
            player.clearMediaItems()
            player.release()
        }
        player = null
    }

    fun attemptPlayerRecovery() {
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success) {
            selectEmbedIndex(currentState.selectedEmbedIndex)
        }
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}