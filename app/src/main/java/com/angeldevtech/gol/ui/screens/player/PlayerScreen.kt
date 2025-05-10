package com.angeldevtech.gol.ui.screens.player

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.angeldevtech.gol.ui.components.tv.ErrorPlayer
import com.angeldevtech.gol.ui.components.tv.LoadingIndicator
import com.angeldevtech.gol.ui.components.tv.PlayerControlsOverlay
import com.angeldevtech.gol.ui.components.tv.VideoPlayer
import com.angeldevtech.gol.utils.isDpadCenterKey
import com.angeldevtech.gol.utils.isDpadMovementKey

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val window = (context as? Activity)?.window
    val player = remember { viewModel.getPlayer() }

    BackHandler(onBack = onBack)

    val overlayButtonFocusRequester = remember { FocusRequester() }
    val isButtonEnabled = remember { mutableStateOf(true) }

    val successState = uiState as? PlayerUIState.Success
    val shouldTriggerInitialOverlayFocus = successState != null &&
            successState.isOverlayVisible

    LifecycleStartEffect(Unit) {
        viewModel.onLoad()
        isButtonEnabled.value = true
        onStopOrDispose { viewModel.pausePlayer() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key.isDpadMovementKey()) {
                    isButtonEnabled.value = true
                    viewModel.showOverlayTemporarily()
                    return@onKeyEvent false
                }
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key.isDpadCenterKey()) {
                    val currentSuccessState = uiState as? PlayerUIState.Success
                    if (currentSuccessState != null && !currentSuccessState.isOverlayVisible) {
                        isButtonEnabled.value = false
                    }
                    viewModel.showOverlayTemporarily()
                    return@onKeyEvent false
                }
                false
            }
    ) {
        when (val state = uiState) {
            is PlayerUIState.Loading -> {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is PlayerUIState.Error -> {
                ErrorPlayer(
                    state = state,
                    viewModel = viewModel,
                    onBack = onBack,
                    modifier = Modifier.fillMaxSize()
                )
            }
            is PlayerUIState.Success -> {
                VideoPlayer(state = state, window = window, player = player)

                AnimatedVisibility(
                    visible = state.isOverlayVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    PlayerControlsOverlay(
                        state = state,
                        viewModel = viewModel,
                        overlayButtonFocusRequester = overlayButtonFocusRequester,
                        initialFocusTrigger = shouldTriggerInitialOverlayFocus,
                        modifier = Modifier.fillMaxSize(),
                        isButtonEnabled = isButtonEnabled.value
                    )
                }
            }
        }
    }
}