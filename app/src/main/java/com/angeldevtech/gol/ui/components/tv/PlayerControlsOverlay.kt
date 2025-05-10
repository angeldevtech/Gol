package com.angeldevtech.gol.ui.components.tv

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.angeldevtech.gol.ui.screens.player.PlayerUIState
import com.angeldevtech.gol.ui.screens.player.PlayerViewModel

@Composable
fun PlayerControlsOverlay(
    state: PlayerUIState.Success,
    viewModel: PlayerViewModel,
    overlayButtonFocusRequester: FocusRequester,
    initialFocusTrigger: Boolean,
    modifier: Modifier = Modifier,
    isButtonEnabled: Boolean
) {
    val shouldFocusSourcesInitially by remember(initialFocusTrigger, state) {
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
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.error,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.attemptPlayerRecovery() },
                        modifier = Modifier.focusRequester(overlayButtonFocusRequester)
                    ) {
                        Text(text = "Reintentar")
                    }
                    LaunchedEffect(initialFocusTrigger) {
                        if (initialFocusTrigger) {
                            overlayButtonFocusRequester.requestFocus()
                        }
                    }
                }
            } else {
                LargePlayPauseButton(
                    isPlaying = state.isPlaying,
                    onClick = { viewModel.togglePlayPause() },
                    keepOverlay = { viewModel.showOverlayTemporarily() },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .focusRequester(overlayButtonFocusRequester),
                    isButtonEnabled = isButtonEnabled
                )
                LaunchedEffect(initialFocusTrigger) {
                    if (initialFocusTrigger) {
                        overlayButtonFocusRequester.requestFocus()
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LiveButton(
                isEnabled = !state.isLoadingNewSource && state.error == null,
                isLive = state.isLive,
                onClick = { viewModel.seekToLive() }
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Fuente(s):",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    itemsIndexed(state.scheduleItem.embeds) { index, embed ->
                        val isSelected = index == state.selectedEmbedIndex

                        Button(
                            onClick = { viewModel.selectEmbedIndex(index) },
                            colors = ButtonDefaults.colors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.surface else Color.White,
                            ),
                            border = ButtonDefaults.border(
                                border = Border(
                                    border = BorderStroke(1.dp, Color.Gray)
                                )
                            ),
                            modifier = if (isSelected && shouldFocusSourcesInitially) Modifier.focusRequester(overlayButtonFocusRequester) else Modifier
                        ) {
                            Text(
                                text = embed.name,
                            )
                        }
                    }
                }
            }
            LaunchedEffect(shouldFocusSourcesInitially) {
                if (shouldFocusSourcesInitially) {
                    overlayButtonFocusRequester.requestFocus()
                }
            }
        }
    }
}