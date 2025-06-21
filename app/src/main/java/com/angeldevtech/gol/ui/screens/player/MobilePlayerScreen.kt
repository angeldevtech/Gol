package com.angeldevtech.gol.ui.screens.player

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.graphics.Rect
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.angeldevtech.gol.ui.screens.player.component.mobile.ErrorMobilePlayerScreen
import com.angeldevtech.gol.ui.screens.player.component.mobile.LoadingIndicator
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAndroidRectF
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.semantics.Role
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRect
import androidx.core.util.Consumer
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.angeldevtech.gol.ui.screens.player.component.mobile.PlayerControlsOverlayMobile
import com.angeldevtech.gol.ui.screens.player.component.mobile.VideoPlayer
import com.angeldevtech.gol.utils.ACTION_PAUSE
import com.angeldevtech.gol.utils.ACTION_PLAY
import com.angeldevtech.gol.utils.createPipActions
import com.angeldevtech.gol.utils.findActivity

@Composable
fun MobilePlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity() as? ComponentActivity ?: return
    val window = (context as? Activity)?.window
    val player = remember { viewModel.getPlayer() }

    val isInPipMode by viewModel.isInPipMode.collectAsStateWithLifecycle()
    val shouldEnterPipMode by viewModel.shouldEnterPipMode.collectAsStateWithLifecycle()
    var showPlayer by remember { mutableStateOf(true) }
    var playerViewBounds by remember { mutableStateOf(Rect()) }

    DisposableEffect(shouldEnterPipMode) {
        val onUserLeaveBehavior = Runnable {
            if (shouldEnterPipMode) {
                viewModel.hideOverlay(0)
                val successState = uiState as? PlayerUIState.Success ?: return@Runnable
                val params = PictureInPictureParams.Builder()
                    .setSourceRectHint(playerViewBounds)
                    .setAspectRatio(Rational(16, 9))
                    .setActions(createPipActions(context, successState.isPlaying))
                    .build()
                activity.enterPictureInPictureMode(params)
            }
        }

        activity.addOnUserLeaveHintListener(onUserLeaveBehavior)
        onDispose {
            activity.removeOnUserLeaveHintListener(onUserLeaveBehavior)
        }
    }

    DisposableEffect(activity) {
        val listener = Consumer<PictureInPictureModeChangedInfo> { info ->
            if (info.isInPictureInPictureMode) {
                viewModel.onEnterPipMode()
            } else {
                viewModel.onExitPipMode()
            }
        }
        activity.addOnPictureInPictureModeChangedListener(listener)

        val pipActionsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ACTION_PLAY -> {
                        viewModel.attemptPlayerRecovery()
                    }
                    ACTION_PAUSE -> {
                        player.pause()
                    }
                }
            }
        }
        val intentFilter = IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.registerReceiver(pipActionsReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                activity,
                pipActionsReceiver,
                intentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        onDispose {
            activity.removeOnPictureInPictureModeChangedListener(listener)
            activity.unregisterReceiver(pipActionsReceiver)
        }
    }

    LaunchedEffect(uiState, isInPipMode) {
        if (isInPipMode) {
            val successState = uiState as? PlayerUIState.Success ?: return@LaunchedEffect
            val updatedParams = PictureInPictureParams.Builder()
                .setActions(createPipActions(context, successState.isPlaying))
                .build()
            activity.setPictureInPictureParams(updatedParams)
        }
    }

    DisposableEffect(Unit) {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        window?.let { w ->
            val windowInsetsController = WindowCompat.getInsetsController(w, w.decorView)
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        }

        onDispose {
            window?.let { w ->
                val windowInsetsController = WindowCompat.getInsetsController(w, w.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    val proactiveOnBack = {
        showPlayer = false
        window?.let { w ->
            val windowInsetsController = WindowCompat.getInsetsController(w, w.decorView)
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
        viewModel.releasePlayer()
        onBack()
    }

    BackHandler(onBack = proactiveOnBack)

    LifecycleEventEffect(event = Lifecycle.Event.ON_START) {
        viewModel.onLoad()
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_STOP) {
        viewModel.stopPlayer()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.releasePlayer()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
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
                if (showPlayer) {
                    VideoPlayer(
                        isPlaying = state.isPlaying,
                        window = window,
                        player = player,
                        modifier = Modifier
                            .onGloballyPositioned { layoutCoordinates ->
                                playerViewBounds =
                                    layoutCoordinates.boundsInWindow().toAndroidRectF().toRect()
                            }
                            .aspectRatio(16f / 9f)
                    )
                }

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
                    visible = state.isOverlayVisible && !isInPipMode,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
                ) {
                    PlayerControlsOverlayMobile(
                        state = state,
                        viewModel = viewModel,
                        onBackClick = proactiveOnBack,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}