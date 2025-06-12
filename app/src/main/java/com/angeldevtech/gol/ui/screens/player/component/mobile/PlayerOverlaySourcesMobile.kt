package com.angeldevtech.gol.ui.screens.player.component.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.angeldevtech.gol.ui.screens.player.PlayerUIState
import com.angeldevtech.gol.ui.screens.player.PlayerViewModel

@Composable
fun PlayerOverlaySourcesMobile(
    state: PlayerUIState.Success,
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LiveButtonMobile(
            isEnabled = !state.isLoadingNewSource && state.error == null,
            isLive = state.isLive,
            onClick = { viewModel.seekToLive() }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fuente(s):",
                style = MaterialTheme.typography.bodyMedium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                itemsIndexed(state.scheduleItem.embeds) { index, embed ->
                    val isSelected = index == state.selectedEmbedIndex

                    OutlinedButton(
                        onClick = { viewModel.selectEmbedIndex(index) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White
                        )
                    ) {
                        Text(text = embed.name)
                    }
                }
            }
        }
    }
}