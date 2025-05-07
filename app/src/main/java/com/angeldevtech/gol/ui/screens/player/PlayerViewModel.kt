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
) : ViewModel(), Player.Listener {

    private val itemId: Int? = savedStateHandle["scheduleItemId"]

    private val _uiState = MutableStateFlow<PlayerUIState>(PlayerUIState.Loading)
    val uiState: StateFlow<PlayerUIState> = _uiState.asStateFlow()

    init {
        loadItemContent()
    }

    private fun loadItemContent(){
        viewModelScope.launch {
            _uiState.value = PlayerUIState.Loading
            if (itemId == null) {
                _uiState.value = PlayerUIState.Error("¡Ups! Hubo un error al pasar el id del evento")
            } else {
                val scheduleItem = getScheduleItemById(itemId)
                if (scheduleItem == null){
                    _uiState.value = PlayerUIState.Error("¡Ups! No se pudo encontrar datos del evento")
                } else {
                    val player = ExoPlayer.Builder(application).build().apply {
                        playWhenReady = true
                        addListener(this@PlayerViewModel)
                    }

                    _uiState.value = PlayerUIState.Success(scheduleItem, 0, player)
                    playContentForEmbedIndex(0)
                }
            }
        }
    }

    private fun playContentForEmbedIndex(embedIndex: Int){
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success){
            viewModelScope.launch {
                _uiState.value = currentState.copy(
                    player = currentState.player.apply { stop() },
                    isLoadingNewSource = true,
                    error = null
                )
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

                            _uiState.value = currentState.copy(
                                player = currentState.player.apply {
                                    clearMediaItems()
                                    setMediaItem(mediaItem)
                                    prepare()
                                    playWhenReady = true
                                },
                                isLoadingNewSource = false
                            )
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

    fun changeEmbedSource(newEmbedIndex: Int) {
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success && newEmbedIndex != currentState.selectedEmbedIndex) {
            playContentForEmbedIndex(newEmbedIndex)
        }
    }

    fun togglePlayPause() {
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success) {
            currentState.player.let {
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
            currentState.player.let {
                it.seekToDefaultPosition()
                if (!it.isPlaying) {
                    it.play()
                }
            }
        }
    }

    private fun releasePlayer() {
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success) {
            currentState.player.let { player ->
                player.removeListener(this)
                player.stop()
                player.clearMediaItems()
                player.release()
            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
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
                error = errorMessage
            )
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Log.d("PlayerViewModel", "IsPlaying changed: $isPlaying")
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success) {
            _uiState.value = currentState.copy(isPlaying = isPlaying)
        }
    }

    fun attemptPlayerRecovery() {
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success) {
            playContentForEmbedIndex(currentState.selectedEmbedIndex)
        }
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}