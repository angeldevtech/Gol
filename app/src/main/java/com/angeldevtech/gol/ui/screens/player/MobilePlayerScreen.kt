package com.angeldevtech.gol.ui.screens.player

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.angeldevtech.gol.ui.screens.player.component.mobile.ErrorMobilePlayerScreen
import com.angeldevtech.gol.ui.screens.player.component.mobile.LoadingIndicator
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.semantics.Role
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.angeldevtech.gol.ui.screens.player.component.mobile.PlayerControlsOverlayMobile
import com.angeldevtech.gol.ui.screens.player.component.mobile.VideoPlayer

@Composable
fun MobilePlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val window = (context as? Activity)?.window
    val player = remember { viewModel.getPlayer() }

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation =
            activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window?.let { w ->
            val windowInsetsController = WindowCompat.getInsetsController(w, w.decorView)
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        }

        onDispose {
            activity?.requestedOrientation = originalOrientation

            window?.let { w ->
                val windowInsetsController = WindowCompat.getInsetsController(w, w.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    BackHandler(onBack = onBack)

    LifecycleStartEffect(Unit) {
        viewModel.onLoad()
        onStopOrDispose { viewModel.stopPlayer() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is PlayerUIState.Loading -> {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is PlayerUIState.Error -> {
                ErrorMobilePlayerScreen(
                    state = state,
                    retry = { viewModel.onLoad() },
                    onBack = onBack,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is PlayerUIState.Success -> {
                VideoPlayer(
                    isPlaying = state.isPlaying,
                    window = window,
                    player = player
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = { viewModel.showOverlayTemporarily() }
                        )
                )

                AnimatedVisibility(
                    visible = state.isOverlayVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    PlayerControlsOverlayMobile(
                        state = state,
                        viewModel = viewModel,
                        onBackClick = onBack,
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                    )
                }
            }
        }
    }
}