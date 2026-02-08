package com.angeldevtech.gol.ui.screens.home.component.tv

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Text
import com.angeldevtech.gol.R

@Composable
fun EmptyList(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.empty_list_message),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}