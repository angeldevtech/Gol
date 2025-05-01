package com.angeldevtech.gol.ui.screens.player

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import kotlinx.coroutines.delay

val DpadCenterKeys = listOf(KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER)

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showControls by remember { mutableStateOf(true) }
    val context = LocalContext.current

    DisposableEffect(Unit) { // Run once when PlayerScreen enters composition
        val activity = context.findActivity()
        val window = activity?.window

        if (window != null) {
            Log.d("PlayerScreen", "Adding FLAG_KEEP_SCREEN_ON")
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            Log.w("PlayerScreen", "Could not get window to set FLAG_KEEP_SCREEN_ON")
        }

        onDispose { // Run when PlayerScreen leaves composition
            if (window != null) {
                Log.d("PlayerScreen", "Clearing FLAG_KEEP_SCREEN_ON")
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000)
            showControls = false
        }
    }

    fun showAndResetTimer() {
        showControls = true
    }

    val playPauseButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(showControls) {
        if (showControls && uiState is PlayerUIState.Success) {
            delay(100) // Small delay to ensure button is composed
            try { playPauseButtonFocusRequester.requestFocus() } catch (e: Exception) { /* Ignore focus request errors */ }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Basic background
            .focusable() // Make the whole screen area focusable to catch key events
            .onKeyEvent { keyEvent ->
                // Show controls on any D-PAD movement
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key.nativeKeyCode in listOf(
                        KeyEvent.KEYCODE_DPAD_UP,
                        KeyEvent.KEYCODE_DPAD_DOWN,
                        KeyEvent.KEYCODE_DPAD_LEFT,
                        KeyEvent.KEYCODE_DPAD_RIGHT
                    )
                ) {
                    showAndResetTimer()
                    // Return false to allow focus navigation to proceed
                    return@onKeyEvent false
                }

                // Handle D-PAD Center specifically
                if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key.nativeKeyCode in DpadCenterKeys) {
                    val currentState = uiState // Capture current state
                    if (currentState is PlayerUIState.Success) {
                        viewModel.togglePlayPause()
                        showAndResetTimer()
                        return@onKeyEvent true // Consume the event
                    }
                }
                false // Don't consume other keys
            }
    ) {
        // --- Video Player Layer ---
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
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            is PlayerUIState.Success -> {
                // Pass player state down
                VideoPlayer(player = state.player) // Assumes VideoPlayer is defined elsewhere

                // --- Controls Overlay Layer ---
                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    PlayerControlsOverlay(
                        state = state, // Pass the Success state
                        viewModel = viewModel,
                        playPauseFocusRequester = playPauseButtonFocusRequester,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // --- Playback Error Overlay ---
                AnimatedVisibility(
                    visible = state.playbackError != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)) // Darker overlay for error
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Playback Error:\n${state.playbackError}",
                            color = MaterialTheme.colorScheme.error, // Use error color
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium // Make it prominent
                        )
                        // Optional: Add a retry button here?
                        Button(onClick = { viewModel.attemptPlayerRecovery() }) { Text("Retry") }
                    }
                }

                // --- Source Switching Loading ---
                // Show a simple scrim and indicator when switching sources
                if(state.isLoadingNewSource) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // --- Source Switch Error Message (Snackbar/Toast Recommended) ---
                val sourceSwitchError = state.sourceSwitchError
                LaunchedEffect(sourceSwitchError) {
                    if (sourceSwitchError != null) {
                        Log.e("PlayerScreen", "Source Switch Error: $sourceSwitchError")
                        // TODO: Show a Snackbar or Toast here for sourceSwitchError
                        // Example: snackbarHostState.showSnackbar("Failed to switch source: $sourceSwitchError")
                        delay(4000)
                        // Consider clearing the error in ViewModel after shown?
                    }
                }
            }
        }
    }
}

// --- Player Controls Overlay Composable ---
@Composable
fun PlayerControlsOverlay(
    state: PlayerUIState.Success,
    viewModel: PlayerViewModel,
    playPauseFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val player = state.player
    val isPlaying by remember(player) { derivedStateOf { player.isPlaying} }
    val isLive by remember(player) { derivedStateOf { player.isPlaying && player.currentLiveOffset <= 30000 } }

    Log.d("VIDEO", "IS LIVE: $isLive")
    Log.d("VIDEO", "currentLiveOffset: ${player.currentLiveOffset}")

    Box(modifier = modifier.background(Color.Black.copy(alpha = 0.4f))) {

        // Top: Item Name
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.9f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 48.dp, vertical = 24.dp)
        ) {
            Text(
                text = state.scheduleItem.name,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = state.scheduleItem.category,
                color = Color.White
            )
        }

        // Center: Play/Pause Button
        LargePlayPauseButton(
            isPlaying = isPlaying,
            onClick = { viewModel.togglePlayPause() },
            modifier = Modifier
                .align(Alignment.Center)
                .focusRequester(playPauseFocusRequester) // Apply focus requester
        )

        // Bottom Controls Row
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Left: Live Button
            LiveButton(
                isEnabled = isLive, // Enable only if seek to live is possible
                onClick = { viewModel.seekToLive() }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Fuente:",
                    color = Color.White,
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    itemsIndexed(state.scheduleItem.embeds) { index, embed ->
                        val isSelected = index == state.selectedEmbedIndex

                        Button(
                            onClick = {viewModel.changeEmbedSource(index)},
                            colors = ButtonDefaults.colors(
                                containerColor = if(isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                contentColor = if(isSelected) MaterialTheme.colorScheme.surface else Color.White,
                            ),
                            border = ButtonDefaults.border(
                                border = Border(
                                    border = BorderStroke(1.dp, Color.Gray)
                                )
                            )
                        ){
                            Text(
                                text = embed.name,
                                color = if (isSelected) Color.Black else Color.Gray,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LargePlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier // Keep accepting the modifier (for focusRequester, alignment)
) {
    // Use IconButton for better blending - it's typically transparent background
    IconButton(
        onClick = onClick,
        modifier = modifier.size(120.dp), // Keep the large touch target size
        // Optionally customize focused appearance if default isn't enough
         colors = IconButtonDefaults.colors(
             containerColor = Color.Transparent,
             contentColor = Color.White,
             focusedContainerColor = Color.Black.copy(alpha = 0.7f)
         )
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.White,
            modifier = Modifier.size(100.dp)
        )
    }
}

@Composable
fun LiveButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.colors(
            containerColor = Color.Black.copy(alpha = 0.9f), // No background needed usually
            focusedContainerColor = MaterialTheme.colorScheme.onSurface,
            focusedContentColor = MaterialTheme.colorScheme.surface,
        ),
        border = ButtonDefaults.border(
            border = Border.None,
            focusedBorder = Border.None,
            disabledBorder = Border(
                border = BorderStroke(1.dp, Color.Gray)
            )
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Simple red dot indicator when live and enabled
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (isEnabled) Color.Red else Color.Gray, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "EN VIVO",
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// Remember to have this Composable defined or import it
@Composable
fun VideoPlayer(player: Player, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val playerView = remember {
        PlayerView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            useController = false
        }
    }

    DisposableEffect(player) {
        playerView.player = player
        onDispose {
            playerView.player = null
        }
    }

    AndroidView({ playerView }, modifier = modifier.fillMaxSize())
}