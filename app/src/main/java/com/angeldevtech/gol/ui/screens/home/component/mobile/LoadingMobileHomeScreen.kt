package com.angeldevtech.gol.ui.screens.home.component.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.angeldevtech.gol.utils.shimmerEffect

@Composable
fun LoadingMobileHomeScreen(
    modifier: Modifier = Modifier
) {
    val screenInfo = LocalWindowInfo.current.containerSize
    val screenWidth = with(LocalDensity.current) { screenInfo.width.toDp() }

    val columns = when {
        screenWidth < 600.dp -> 1
        screenWidth < 840.dp -> 2
        screenWidth < 1200.dp -> 3
        else -> 4
    }

    Column(
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(columns * 4) {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(32.dp)
                        .shimmerEffect()
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(columns * 5) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .shimmerEffect()
                    )
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(72.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(20.dp)
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.3f)
                                .height(16.dp)
                                .shimmerEffect()
                        )
                    }
                }
            }
        }
    }
}