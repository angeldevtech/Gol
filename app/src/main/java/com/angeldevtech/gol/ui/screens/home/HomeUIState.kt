package com.angeldevtech.gol.ui.screens.home

import com.angeldevtech.gol.domain.models.ScheduleCategories
import com.angeldevtech.gol.domain.models.ScheduleItem

sealed interface HomeUIState {
    data object Loading : HomeUIState
    data class Success(
        val categories: List<ScheduleCategories>,
        val currentOrUpcomingEvents: List<ScheduleItem> = emptyList(),
        val initialFocusCategoryName: String? = null
    ) : HomeUIState
    data class Error(val message: String) : HomeUIState
}