package com.angeldevtech.gol.ui.components.tv

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.ui.screens.home.HomeViewModel
import com.angeldevtech.gol.utils.PaletteResult
import com.angeldevtech.gol.utils.darken

@Composable
fun ItemCard (
    item: ScheduleItem,
    viewModel: HomeViewModel,
    onClick: () -> Unit = {}
) {

    val paletteResult = if (item.leagueImageUrl.isNotBlank()) {
        viewModel.paletteCache[item.leagueImageUrl] ?: PaletteResult(
            cardColor = MaterialTheme.colorScheme.surfaceVariant,
            textColor = Color.White.copy(alpha = 0.8f)
        )
    } else {
        PaletteResult(
            cardColor = MaterialTheme.colorScheme.surfaceVariant,
            textColor = Color.White.copy(alpha = 0.8f)
        )
    }

    val animatedCardColor by animateColorAsState(targetValue = paletteResult.cardColor.darken())
    val animatedChipColor by animateColorAsState(targetValue = paletteResult.cardColor)

    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    Column(
        modifier = Modifier.size(268.dp, 212.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp),
            border = CardDefaults.border(focusedBorder = Border(
                border = BorderStroke(width = 3.dp, color = Color.White)
            )),
            colors = CardDefaults.colors(containerColor = animatedCardColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp, horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (item.leagueImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.leagueImageUrl,
                        contentDescription = item.leagueName,
                        modifier = Modifier
                            .size(80.dp),
                        contentScale = ContentScale.Fit,
                        onSuccess = { state ->
                        val bitmap = (state.result.drawable as? BitmapDrawable)?.bitmap
                        bitmap?.let {
                            viewModel.generatePalette(it, item.leagueImageUrl)
                        }
                    }
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .background(animatedChipColor, RoundedCornerShape(4.dp))
                            .padding(8.dp, 4.dp),
                    ) {
                        Text(
                            text = item.date,
                            color = paletteResult.textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = item.hour.dropLast(3),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }
            }
        }
        Column {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                color = if (focused) Color.White else Color.Gray,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.leagueName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                color = Color.Gray,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}