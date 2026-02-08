package com.angeldevtech.gol.ui.screens.player.component.tv

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.angeldevtech.gol.R
import com.angeldevtech.gol.ui.screens.player.PlayerUIState
import com.angeldevtech.gol.ui.screens.player.PlayerViewModel

@Composable
fun PlayerOverlaySources(
    state: PlayerUIState.Success,
    viewModel: PlayerViewModel,
    sourcesListFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
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
                text = stringResource(R.string.sources),
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
                        modifier = if (isSelected) Modifier.focusRequester(sourcesListFocusRequester) else Modifier
                    ) {
                        Text(
                            text = embed.name,
                        )
                    }
                }
            }
        }
    }
}