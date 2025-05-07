package com.angeldevtech.gol.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
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