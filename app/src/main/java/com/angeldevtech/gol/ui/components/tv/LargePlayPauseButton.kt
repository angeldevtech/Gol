package com.angeldevtech.gol.ui.components.tv

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults

@Composable
fun LargePlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    keepOverlay: () -> Unit,
    modifier: Modifier = Modifier,
    isButtonEnabled: Boolean
) {

    val isFirstClick = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isFirstClick.value = true
    }

    IconButton(
        onClick = {
            if (isButtonEnabled || !isFirstClick.value) {
                keepOverlay()
                onClick()
            } else {
                isFirstClick.value = false
            }
        },
        modifier = modifier
            .size(120.dp),
        colors = IconButtonDefaults.colors(
            containerColor = Color.Black.copy(alpha = 0.3f),
            contentColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.7f),
        )
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            modifier = Modifier.size(100.dp)
        )
    }
}