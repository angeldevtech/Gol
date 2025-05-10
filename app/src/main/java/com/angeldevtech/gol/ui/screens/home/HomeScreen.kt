package com.angeldevtech.gol.ui.screens.home

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.angeldevtech.gol.domain.models.ScheduleCategories
import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.ui.components.tv.CategoryList
import com.angeldevtech.gol.ui.components.tv.EmptyList
import com.angeldevtech.gol.ui.components.tv.ErrorContent
import com.angeldevtech.gol.ui.components.tv.HomeHeader
import com.angeldevtech.gol.ui.components.tv.LoadingContent
import com.angeldevtech.gol.utils.PeriodicTimeUpdateWhileResumed

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onItemSelected: (ScheduleItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleStartEffect(Unit) {
        viewModel.onRefresh()
        onStopOrDispose {  }
    }

    PeriodicTimeUpdateWhileResumed(viewModel)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val scope = this

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 24.dp)
        ){
            item(contentType = "HomeHeader") {
                HomeHeader(
                    uiState = uiState,
                    viewModel = viewModel,
                )
            }

            when (val state = uiState){
                is HomeUIState.Loading -> item(contentType = "LoadingContent") {
                    LoadingContent(
                        modifier = Modifier
                            .height(scope.maxHeight - 76.dp)
                            .fillMaxWidth()
                    )
                }
                is HomeUIState.Success -> {
                    if (state.currentOrUpcomingEvents.isNotEmpty()){
                        item(contentType = "currentOrUpcomingEvents") {
                            CategoryList(
                                ScheduleCategories(name = "En juego y en breve", items = state.currentOrUpcomingEvents),
                                viewModel,
                                onItemSelected = onItemSelected
                            )
                        }
                    }
                    if (state.categories.isNotEmpty()){
                        items(
                            state.categories,
                            contentType = { "CategoryList" }
                        ) { category ->
                            CategoryList(category, viewModel, onItemSelected = onItemSelected)
                        }
                    } else {
                        item(contentType = "EmptyList") {
                            EmptyList(
                                modifier = Modifier
                                    .height(scope.maxHeight - 100.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
                is HomeUIState.Error -> item(contentType = "ErrorContent") {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.onRefresh() },
                        modifier = Modifier
                            .height(scope.maxHeight - 100.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}