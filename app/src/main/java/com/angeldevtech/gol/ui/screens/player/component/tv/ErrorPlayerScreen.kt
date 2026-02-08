package com.angeldevtech.gol.ui.screens.player.component.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.angeldevtech.gol.R
import com.angeldevtech.gol.ui.screens.player.PlayerUIState
import kotlinx.coroutines.delay

@Composable
fun ErrorPlayerScreen(
    state: PlayerUIState.Error,
    retry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var secondsRemaining by remember { mutableIntStateOf(5) }

    if (state.returnHomeScreen) {
        LaunchedEffect(Unit) {
            repeat(5) {
                delay(1000)
                secondsRemaining--
            }
            onBack()
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = state.message,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (state.returnHomeScreen) {
            Text(
                text = stringResource(R.string.redirecting_home, secondsRemaining),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack
            ) {
                Text(text = stringResource(R.string.go_home))
            }
            if (!state.returnHomeScreen) {
                Button(
                    onClick = retry
                ) {
                    Text(text = stringResource(R.string.retry))
                }
            }
        }
    }
}