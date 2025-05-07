package com.angeldevtech.gol.ui.screens.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.angeldevtech.gol.ui.components.PlayerControlsOverlay
import com.angeldevtech.gol.ui.components.VideoPlayer
import kotlinx.coroutines.delay

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onBack: () -> Boolean
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showControls by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val activity = context.findActivity()
    val window = activity?.window

    val playPauseButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(showControls) {
        val currentState = uiState
        if (showControls && currentState is PlayerUIState.Success) {
            delay(100)
            try { playPauseButtonFocusRequester.requestFocus() } catch (_: Exception) { /* Ignore focus request errors */ }
            if (currentState.isPlaying){
                delay(5000)
                showControls = false
            }
        }
    }

    fun showAndResetTimer() {
        showControls = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key.nativeKeyCode in listOf(
                        KeyEvent.KEYCODE_DPAD_UP,
                        KeyEvent.KEYCODE_DPAD_DOWN,
                        KeyEvent.KEYCODE_DPAD_LEFT,
                        KeyEvent.KEYCODE_DPAD_RIGHT,
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_NUMPAD_ENTER
                    )
                ) {
                    showAndResetTimer()
                    return@onKeyEvent false
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
                    Button(
                        onClick = { onBack() }
                    ) {
                        Text(text = "OK")
                    }
                }
            }
            is PlayerUIState.Success -> {
                // Pass player state down
                VideoPlayer(state = state, window = window)

                // --- Controls Overlay Layer ---
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    PlayerControlsOverlay(
                        state = state,
                        viewModel = viewModel,
                        playPauseFocusRequester = playPauseButtonFocusRequester,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}