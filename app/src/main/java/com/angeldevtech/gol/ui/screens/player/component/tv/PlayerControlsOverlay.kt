package com.angeldevtech.gol.ui.screens.player.component.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.angeldevtech.gol.ui.screens.player.PlayerUIState
import com.angeldevtech.gol.ui.screens.player.PlayerViewModel

@Composable
fun PlayerControlsOverlay(
    state: PlayerUIState.Success,
    viewModel: PlayerViewModel,
    isButtonEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val playPauseButtonFocusRequester = remember { FocusRequester() }
    val errorButtonFocusRequester = remember { FocusRequester() }
    val sourcesListFocusRequester = remember { FocusRequester() }

    val focusEffectKey = remember(state) {
        if (state.isLoadingNewSource) {
            return@remember "Sources"
        } else if (state.error != null) {
            return@remember "Error"
        } else {
            return@remember "PausePlay"
        }
    }

    LaunchedEffect(focusEffectKey) {
        if (state.isOverlayVisible) {
            if (state.isLoadingNewSource) {
                sourcesListFocusRequester.requestFocus()
            } else if (state.error != null) {
                errorButtonFocusRequester.requestFocus()
            } else {
                playPauseButtonFocusRequester.requestFocus()
            }
        }
    }

    Box(modifier = modifier.background(Color.Black.copy(alpha = 0.4f))) {
        PlayerOverlayHeader(
            name = state.scheduleItem.name,
            category = state.scheduleItem.category,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 48.dp, vertical = 24.dp)
        )

        if (state.isLoadingNewSource) {
            LoadingIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
            )
        } else if (state.error != null) {
            PlayerOverlayError(
                error = state.error,
                attemptRecovery = { viewModel.attemptPlayerRecovery() },
                errorButtonFocusRequester = errorButtonFocusRequester,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            PlayerOverlayPlayPauseButton(
                isPlaying = state.isPlaying,
                onClick = { viewModel.togglePlayPause() },
                keepOverlay = { viewModel.showOverlayTemporarily() },
                isButtonEnabled = isButtonEnabled,
                modifier = Modifier
                    .align(Alignment.Center)
                    .focusRequester(playPauseButtonFocusRequester)
            )
        }

        PlayerOverlaySources(
            state = state,
            viewModel = viewModel,
            sourcesListFocusRequester = sourcesListFocusRequester,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 24.dp),
        )
    }
}