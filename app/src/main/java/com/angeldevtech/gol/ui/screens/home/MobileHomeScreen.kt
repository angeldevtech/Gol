package com.angeldevtech.gol.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.utils.PeriodicTimeUpdateWhileResumed

@Composable
fun MobileHomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onItemSelected: (ScheduleItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleStartEffect(Unit) {
        viewModel.onRefresh()
        onStopOrDispose {  }
    }

    PeriodicTimeUpdateWhileResumed(viewModel)

}