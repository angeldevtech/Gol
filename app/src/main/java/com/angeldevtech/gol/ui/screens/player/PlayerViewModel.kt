package com.angeldevtech.gol.ui.screens.player

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player
import com.angeldevtech.gol.R
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

    private val m3u8Cache = mutableMapOf<String, String>()

    private var seekToLiveTriggered: Boolean = false
    private var player: ExoPlayer? = null
    private var overlayAutoHideJob: Job? = null
    private var pauseTimerJob: Job? = null
    private val pauseThreshold = 2_000L
    private var pauseStartTime: Long = 0L
    private var accumulatedPauseDuration: Long = 0L
    private var lastLoadTime = 0L
    private val loadIntervalMs = 3 * 60 * 60 * 1000L

    private val _shouldEnterPipMode = MutableStateFlow(false)
    val shouldEnterPipMode: StateFlow<Boolean> = _shouldEnterPipMode.asStateFlow()
    private val _isInPipMode = MutableStateFlow(false)
    val isInPipMode = _isInPipMode.asStateFlow()

    private fun getString(resId: Int, vararg formatArgs: Any): String {
        return application.getString(resId, *formatArgs)
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val currentState = _uiState.value
            if (currentState is PlayerUIState.Success) {
                _shouldEnterPipMode.value = isPlaying
                if (isPlaying) {
                    if (pauseTimerJob?.isCompleted == true) {
                        _uiState.value = currentState.copy(
                            isPlaying = true,
                            isLoadingNewSource = false,
                        )
                    } else {
                        accumulatePauseDuration()
                        cancelPauseTimer()
                        _uiState.value = currentState.copy(
                            isPlaying = true,
                            isLoadingNewSource = false,
                            isLive = true
                        )
                        seekToLiveTriggered = false
                    }
                    showOverlayTemporarily()
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
            resetPauseTime()
            seekToLiveTriggered = false

            val errorMessage = when (error.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
                PlaybackException.ERROR_CODE_IO_UNSPECIFIED -> getString(R.string.error_network)

                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> getString(R.string.error_stream_not_found)
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> getString(R.string.error_network_http)

                PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
                PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED -> getString(R.string.error_parsing)

                PlaybackException.ERROR_CODE_DRM_UNSPECIFIED,
                PlaybackException.ERROR_CODE_DRM_SCHEME_UNSUPPORTED,
                PlaybackException.ERROR_CODE_DRM_PROVISIONING_FAILED,
                PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED -> getString(R.string.error_drm)

                else -> getString(R.string.error_unexpected_playback, error.errorCodeName)
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
        val currentTime = System.currentTimeMillis()
        val shouldLoadByTime = (currentTime - lastLoadTime) > loadIntervalMs

        if (shouldLoadByTime) m3u8Cache.clear()

        if (shouldLoadByTime || _uiState.value !is PlayerUIState.Success){
            lastLoadTime = currentTime
            loadItemContent()
        } else {
            attemptPlayerRecovery()
        }
    }

    private fun loadItemContent(){
        viewModelScope.launch {
            _uiState.value = PlayerUIState.Loading

            if (itemId == null) {
                _uiState.value = PlayerUIState.Error(getString(R.string.error_event_id_missing), true)
                return@launch
            }

            val scheduleItem = getScheduleItemById(itemId)

            if (scheduleItem == null) {
                _uiState.value = PlayerUIState.Error(getString(R.string.error_event_not_found), true)
                return@launch
            }

            initializePlayer()

            if (player == null){
                if (_uiState.value !is PlayerUIState.Error) {
                    _uiState.value = PlayerUIState.Error(getString(R.string.error_player_init))
                }
                return@launch
            }
            cancelOverlayAutoHide()
            cancelPauseTimer()
            resetPauseTime()

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
            _uiState.value = PlayerUIState.Error(getString(R.string.error_player_init_with_msg, e.localizedMessage ?: ""))
        }
    }

    fun getPlayer(): ExoPlayer {
        if (player == null) {
            initializePlayer()
        }
        return player!!
    }

    fun selectEmbedIndex(embedIndex: Int) {
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success) {
            if (
                currentState.selectedEmbedIndex != embedIndex ||
                currentState.error != null ||
                !currentState.isPlaying
            ) {
                cancelOverlayAutoHide()
                cancelPauseTimer()

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
                try {
                    val embedUrl = currentState.scheduleItem.embeds[embedIndex].url

                    val cachedM3u8Url = m3u8Cache[embedUrl]
                    if (cachedM3u8Url != null) {
                        val mediaItem = MediaItem.Builder()
                            .setUri(cachedM3u8Url)
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
                        return@launch
                    }

                    extractM3u8UrlUseCase(embedUrl)
                        .onSuccess { m3u8Url ->
                            m3u8Cache[embedUrl] = m3u8Url

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
                        error = e.localizedMessage ?: getString(R.string.error_unexpected_loading)
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
            resetPauseTime()
            cancelPauseTimer()
            seekToLiveTriggered = true
            player?.let {
                it.seekToDefaultPosition()
                if (!it.isPlaying) {
                    it.play()
                }
            }
        }
    }

    fun showOverlayTemporarily() {
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success) {
            if (!currentState.isOverlayVisible) {
                _uiState.value = currentState.copy(isOverlayVisible = true)
            }
            overlayAutoHideJob?.cancel()
            overlayAutoHideJob = hideOverlay()
        }
    }

    private fun startPauseTimer() {
        if (pauseTimerJob?.isCompleted == true) {
            return
        }
        pauseStartTime = System.currentTimeMillis()
        pauseTimerJob?.cancel()
        pauseTimerJob = viewModelScope.launch {
            delay((pauseThreshold - accumulatedPauseDuration).coerceAtLeast(0))
            val currentState = _uiState.value
            if (currentState is PlayerUIState.Success && !currentState.isPlaying) {
                _uiState.value = currentState.copy(isLive = false)
            }
        }
    }

    private fun accumulatePauseDuration() {
        if (pauseStartTime != 0L && !seekToLiveTriggered){
            val currentTime = System.currentTimeMillis()
            accumulatedPauseDuration += (currentTime - pauseStartTime)
        }
    }

    fun hideOverlay(time: Long = 5000): Job {
        return viewModelScope.launch {
            delay(time)
            val currentState = _uiState.value
            if (currentState is PlayerUIState.Success) {
                if (currentState.error == null && !currentState.isLoadingNewSource){
                    _uiState.value = currentState.copy(
                        isOverlayVisible = false
                    )
                }
            }
        }
    }

    private fun cancelOverlayAutoHide() {
        overlayAutoHideJob?.cancel()
        overlayAutoHideJob = null
    }

    private fun cancelPauseTimer() {
        pauseTimerJob?.cancel()
        pauseTimerJob = null
    }

    private fun resetPauseTime() {
        pauseStartTime = 0L
        accumulatedPauseDuration = 0L
    }

    fun onEnterPipMode() {
        _isInPipMode.value = true
    }

    fun onExitPipMode() {
        _isInPipMode.value = false
    }

    fun stopPlayer() {
        val currentState = _uiState.value
        if (currentState is PlayerUIState.Success) {
            resetPauseTime()
            cancelPauseTimer()
            player?.apply {
                stop()
            }
        }
    }

    fun releasePlayer() {
        _shouldEnterPipMode.value = false
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