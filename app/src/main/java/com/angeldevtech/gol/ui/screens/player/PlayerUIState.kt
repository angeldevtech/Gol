package com.angeldevtech.gol.ui.screens.player

import com.angeldevtech.gol.domain.models.ScheduleItem

sealed interface PlayerUIState {
    data object Loading : PlayerUIState
    data class Success(
        val scheduleItem: ScheduleItem,
        val selectedEmbedIndex: Int,
        val isOverlayVisible: Boolean = true,
        val isLoadingNewSource: Boolean = true,
        val isLive: Boolean = false,
        val error: String? = null,
        val isPlaying: Boolean = false,
    ) : PlayerUIState
    data class Error(val message: String) : PlayerUIState
}