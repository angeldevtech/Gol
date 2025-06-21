package com.angeldevtech.gol.ui.screens.player.component.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.angeldevtech.gol.ui.screens.player.PlayerUIState
import com.angeldevtech.gol.ui.screens.player.PlayerViewModel

@Composable
fun PlayerControlsOverlayMobile(
    state: PlayerUIState.Success,
    viewModel: PlayerViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current

    val topPadding = max(insets.calculateTopPadding(), 8.dp)
    val bottomPadding = max(insets.calculateBottomPadding(), 8.dp)
    val leftPadding = insets.calculateLeftPadding(layoutDirection) + 16.dp
    val rightPadding = insets.calculateRightPadding(layoutDirection) + 16.dp

    Box(modifier = modifier.background(Color.Black.copy(alpha = 0.4f))) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { viewModel.hideOverlay(0) }
                )
        )

        PlayerOverlayHeaderMobile(
            name = state.scheduleItem.name,
            category = state.scheduleItem.category,
            onBackClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = leftPadding,
                    top = topPadding,
                    end = rightPadding,
                    bottom = bottomPadding
                )
        )

        if (state.isLoadingNewSource) {
            LoadingIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
            )
        } else if (state.error != null) {
            PlayerOverlayErrorMobile(
                error = state.error,
                attemptRecovery = { viewModel.attemptPlayerRecovery() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            PlayerOverlayPlayPauseButtonMobile(
                isPlaying = state.isPlaying,
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier.align(Alignment.Center)
            )
        }

        PlayerOverlaySourcesMobile(
            state = state,
            viewModel = viewModel,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = leftPadding,
                    top = topPadding,
                    end = rightPadding,
                    bottom = bottomPadding
                )
        )
    }
}