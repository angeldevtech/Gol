package com.angeldevtech.gol.ui.screens.player.component.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
    overlayButtonFocusRequester: FocusRequester,
    initialFocusTrigger: Boolean,
    isButtonEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val shouldFocusSourcesInitially by remember(initialFocusTrigger, state.isLoadingNewSource) {
        derivedStateOf {
            initialFocusTrigger && state.isLoadingNewSource
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

        if(state.isLoadingNewSource) {
            LoadingIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
            )
        } else {
            if (state.error != null) {
                PlayerOverlayError(
                    error = state.error,
                    attemptRecovery = { viewModel.attemptPlayerRecovery() },
                    overlayButtonFocusRequester = overlayButtonFocusRequester,
                    modifier = Modifier.fillMaxSize()
                )
                LaunchedEffect(initialFocusTrigger) {
                    if (initialFocusTrigger) {
                        overlayButtonFocusRequester.requestFocus()
                    }
                }
            } else {
                PlayerOverlayPlayPauseButton(
                    isPlaying = state.isPlaying,
                    onClick = { viewModel.togglePlayPause() },
                    keepOverlay = { viewModel.showOverlayTemporarily() },
                    isButtonEnabled = isButtonEnabled,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .focusRequester(overlayButtonFocusRequester)
                )
                LaunchedEffect(initialFocusTrigger) {
                    if (initialFocusTrigger) {
                        overlayButtonFocusRequester.requestFocus()
                    }
                }
            }
        }

        PlayerOverlaySources(
            state = state,
            viewModel = viewModel,
            overlayButtonFocusRequester = overlayButtonFocusRequester,
            shouldFocusSourcesInitially = shouldFocusSourcesInitially,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 24.dp),
        )
        LaunchedEffect(shouldFocusSourcesInitially) {
            if (shouldFocusSourcesInitially) {
                overlayButtonFocusRequester.requestFocus()
            }
        }
    }
}