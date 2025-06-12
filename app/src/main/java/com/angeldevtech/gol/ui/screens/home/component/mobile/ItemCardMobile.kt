package com.angeldevtech.gol.ui.screens.home.component.mobile

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.ui.screens.home.HomeViewModel
import com.angeldevtech.gol.utils.PaletteResult
import com.angeldevtech.gol.utils.darken

@Composable
fun ItemCardMobile(
    item: ScheduleItem,
    viewModel: HomeViewModel,
    onClick: () -> Unit = {}
) {

    val paletteResult = if (item.leagueImageUrl.isNotBlank()) {
        viewModel.paletteCache[item.leagueImageUrl] ?: PaletteResult(
            cardColor = Color.DarkGray,
            textColor = Color.White.copy(alpha = 0.8f)
        )
    } else {
        PaletteResult(
            cardColor = Color.DarkGray,
            textColor = Color.White.copy(alpha = 0.8f)
        )
    }

    val animatedCardColor by animateColorAsState(targetValue = paletteResult.cardColor.darken(0.6f))
    val animatedChipColor by animateColorAsState(targetValue = paletteResult.cardColor)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick() },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            colors = CardDefaults.cardColors(containerColor = animatedCardColor),
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val cardWidth = maxWidth
                val cardHeight = maxHeight
                val imageSize = cardWidth * 0.32f
                val paddingHorizontal = cardWidth * 0.08f
                val paddingVertical = cardHeight * 0.04f

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = paddingVertical, horizontal = paddingHorizontal),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (item.leagueImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = item.leagueImageUrl,
                            contentDescription = item.leagueName,
                            modifier = Modifier
                                .size(imageSize),
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
                        verticalArrangement = Arrangement.spacedBy(cardHeight * 0.04f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    animatedChipColor,
                                    RoundedCornerShape(cardWidth * 0.02f)
                                )
                                .padding(
                                    cardWidth * 0.03f,
                                    cardHeight * 0.02f
                                ),
                        ) {
                            Text(
                                text = item.relativeDay,
                                color = paletteResult.textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = (cardHeight * 0.10f).value.sp
                            )
                        }
                        Text(
                            text = item.hour.dropLast(3),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = (cardHeight * 0.20f).value.sp
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(72.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
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