package com.angeldevtech.gol.ui.screens.player

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
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

    LifecycleStartEffect(Unit) {
        viewModel.onLoad()
        onStopOrDispose { viewModel.pausePlayer() }
    }

    val overlayButtonFocusRequester = remember { FocusRequester() }

    val successState = uiState as? PlayerUIState.Success
    val shouldTriggerInitialOverlayFocus = successState != null &&
            successState.isOverlayVisible

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key.isDpadMovementKey()) {
                    viewModel.showOverlayTemporarily()
                    return@onKeyEvent false
                }
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key.isDpadCenterKey()) {
                    viewModel.showOverlayTemporarily()
                    return@onKeyEvent true
                }
                false
            }
    ) {
        when (val state = uiState) {
            is PlayerUIState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(100.dp),
                    color = Color.Gray,
                    trackColor = Color.White,
                    strokeWidth = 10.dp
                )
            }
            is PlayerUIState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onBack() }
                        ) {
                            Text(text = "Volver a la página de inicio")
                        }
                        Button(
                            onClick = { viewModel.onLoad() }
                        ) {
                            Text(text = "Reintentar")
                        }
                    }
                }
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
                    )
                }
            }
        }
    }
}