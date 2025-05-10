package com.angeldevtech.gol.ui.components.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.angeldevtech.gol.domain.models.ScheduleCategories
import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.ui.screens.home.HomeViewModel

@Composable
fun CategoryList(
    category: ScheduleCategories,
    viewModel: HomeViewModel,
    onItemSelected: (ScheduleItem) -> Unit,
) {
    Text(
        text = category.name,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp)
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(horizontal = 48.dp)
    ) {
        items(category.items){ item ->
            ItemCard(
                item = item,
                viewModel = viewModel,
                onClick = { onItemSelected(item) }
            )
        }
    }
}