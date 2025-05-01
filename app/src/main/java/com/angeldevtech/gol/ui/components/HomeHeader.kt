package com.angeldevtech.gol.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.angeldevtech.gol.R
import com.angeldevtech.gol.ui.screens.home.HomeUIState
import com.angeldevtech.gol.ui.screens.home.HomeViewModel

@Composable
fun HomeHeader(
    uiState: HomeUIState,
    viewModel: HomeViewModel,
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher),
                contentDescription = "Gol",
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Gol",
                style = MaterialTheme.typography.displayMedium
            )
        }

        if (uiState is HomeUIState.Success) {
            Button(
                onClick = { viewModel.onRefresh(true) },
            ) {
                Text(
                    text = "Actualizar"
                )
            }
        }
    }
}