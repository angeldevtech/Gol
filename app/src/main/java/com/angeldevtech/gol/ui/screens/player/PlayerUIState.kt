package com.angeldevtech.gol.ui.screens.player

import androidx.media3.common.Player
import com.angeldevtech.gol.domain.models.ScheduleItem

sealed interface PlayerUIState {
    data object Loading : PlayerUIState
    data class Success(
        val scheduleItem: ScheduleItem,
        val selectedEmbedIndex: Int,
        val player: Player,
        val isLoadingNewSource: Boolean = false,
        val isPlaying: Boolean = false,
        val error: String? = null
    ) : PlayerUIState
    data class Error(val message: String) : PlayerUIState
}