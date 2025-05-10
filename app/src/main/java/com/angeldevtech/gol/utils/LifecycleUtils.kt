package com.angeldevtech.gol.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.angeldevtech.gol.ui.screens.home.HomeUIState
import com.angeldevtech.gol.ui.screens.home.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun PeriodicTimeUpdateWhileResumed(viewModel: HomeViewModel) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateAsState()

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.State.RESUMED) {
            while (isActive && lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                if (viewModel.uiState.value is HomeUIState.Success) {
                    viewModel.triggerTimeBasedUpdate()
                }
                delay(5 * 60 * 1000)
            }
        }
    }
}