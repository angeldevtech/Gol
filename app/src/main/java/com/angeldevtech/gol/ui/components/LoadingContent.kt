package com.angeldevtech.gol.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.angeldevtech.gol.utils.shimmerEffect

@Composable
fun LoadingContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(3) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .padding(start = 48.dp)
                        .height(30.dp)
                        .width(120.dp)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(horizontal = 48.dp)
                ) {
                    items(4) {
                        Column(
                            modifier = Modifier.height(212.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 268.dp, height = 144.dp)
                                    .shimmerEffect()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .height(22.dp)
                                    .width(220.dp)
                                    .shimmerEffect()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .height(18.dp)
                                    .width(80.dp)
                                    .shimmerEffect()
                            )
                        }
                    }
                }
            }
        }
    }
}