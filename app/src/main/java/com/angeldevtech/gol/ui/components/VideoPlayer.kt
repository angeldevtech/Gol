package com.angeldevtech.gol.ui.components

import android.util.Log
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
import androidx.media3.ui.PlayerView
import com.angeldevtech.gol.ui.screens.player.PlayerUIState

@Composable
fun VideoPlayer(
    state: PlayerUIState.Success,
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
        }
    }

    DisposableEffect(Unit) {
        Log.d("PLAYER", "VideoPlayer: ${state.isPlaying}")
        if (window != null) {
            if (state.isPlaying) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                Log.d("PLAYER", "Clearing FLAG_KEEP_SCREEN_ON")
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        onDispose {
            if (window != null) {
                Log.d("PLAYER", "Clearing FLAG_KEEP_SCREEN_ON dispose")
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    DisposableEffect(state.player) {
        playerView.player = state.player

        onDispose {
            playerView.player = null
        }
    }

    AndroidView({ playerView }, modifier = Modifier.fillMaxSize())
}