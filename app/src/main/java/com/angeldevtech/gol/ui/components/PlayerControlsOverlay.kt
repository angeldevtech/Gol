package com.angeldevtech.gol.ui.components

import android.util.Log
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    playPauseFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val player = state.player
    val isLive by remember(player) { derivedStateOf { player.isPlaying && player.currentLiveOffset <= 30000 } }

    Log.d("PLAYER", "IS LIVE: $isLive")

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

        if(state.isLoadingNewSource) {
            Box(modifier = Modifier
                .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
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
                        modifier = Modifier.focusRequester(playPauseFocusRequester)
                    ) {
                        Text(text = "Reintentar")
                    }
                }
            } else {
                LargePlayPauseButton(
                    isPlaying = state.isPlaying,
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .focusRequester(playPauseFocusRequester) // Apply focus requester
                )
            }
        }

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