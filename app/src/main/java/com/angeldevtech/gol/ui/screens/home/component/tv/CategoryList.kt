package com.angeldevtech.gol.ui.screens.home.component.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
    BoxWithConstraints {
        val maxWidth = this.maxWidth - 96.dp

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(horizontal = 48.dp)
        ) {
            if (category.items.isEmpty()) {
                item(contentType = "EmptyCategoryList") {
                    Column(
                        modifier = Modifier
                            .width(maxWidth)
                            .height(212.dp)
                            .background(Color(37, 35, 40), RoundedCornerShape(8.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "¡Ups! No hay eventos por el momento",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No hay ningún evento en la última o próxima media hora",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                        )
                    }
                }
            } else {
                items(
                    category.items,
                    contentType = { "CategoryListItem" }
                ) { item ->
                    ItemCard(
                        item = item,
                        viewModel = viewModel,
                        onClick = { onItemSelected(item) }
                    )
                }
            }
        }
    }
}
