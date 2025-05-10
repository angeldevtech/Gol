package com.angeldevtech.gol.ui.screens.player

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MobilePlayerScreen(
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
        onStopOrDispose {  }
    }

}