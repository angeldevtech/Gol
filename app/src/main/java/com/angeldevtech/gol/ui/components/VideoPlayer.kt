package com.angeldevtech.gol.ui.components

import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.angeldevtech.gol.ui.screens.player.PlayerUIState

@Composable
fun VideoPlayer(
    state: PlayerUIState.Success,
    player: ExoPlayer,
    window: Window?,
) {
    val context = LocalContext.current
    val playerView = remember {
        PlayerView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            useController = false
            isFocusable = false
            isFocusableInTouchMode = false
        }
    }

    DisposableEffect(state.isPlaying) {
        if (window != null) {
            if (state.isPlaying) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        onDispose { }
    }

    DisposableEffect(player) {
        playerView.player = player

        onDispose {
            playerView.player = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    AndroidView({ playerView }, modifier = Modifier.fillMaxSize())
}