package com.angeldevtech.gol.ui.screens.home

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.angeldevtech.gol.R
import com.angeldevtech.gol.domain.models.ScheduleCategories
import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.ui.screens.home.component.tv.CategoryList
import com.angeldevtech.gol.ui.screens.home.component.tv.EmptyList
import com.angeldevtech.gol.ui.screens.home.component.tv.ErrorHomeScreen
import com.angeldevtech.gol.ui.screens.home.component.tv.HeaderHomeScreen
import com.angeldevtech.gol.ui.screens.home.component.tv.LoadingHomeScreen
import com.angeldevtech.gol.utils.PeriodicTimeUpdateWhileResumed

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onItemSelected: (ScheduleItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    val initialListFocusRequester = remember { FocusRequester() }
    val errorButtonFocusRequester = remember { FocusRequester() }
    val headerRefreshButtonFocusRequester = remember { FocusRequester() }

    LifecycleStartEffect(Unit) {
        viewModel.onRefresh()
        onStopOrDispose {  }
    }

    PeriodicTimeUpdateWhileResumed(viewModel)

    var lastFocusedState by rememberSaveable { mutableStateOf<String?>(null) }

    val focusEffectKey = remember(uiState) {
        when (val state = uiState) {
            is HomeUIState.Success -> "Success:${state.categories.isNotEmpty()}"
            is HomeUIState.Error -> "Error"
            is HomeUIState.Loading -> "Loading"
        }
    }

    LaunchedEffect(focusEffectKey) {
        if (lastFocusedState != focusEffectKey) {
            when (val state = uiState) {
                is HomeUIState.Success -> {
                    if (state.categories.isNotEmpty()) {
                        initialListFocusRequester.requestFocus()
                    } else {
                        headerRefreshButtonFocusRequester.requestFocus()
                    }
                    lastFocusedState = focusEffectKey
                }
                is HomeUIState.Error -> {
                    errorButtonFocusRequester.requestFocus()
                    lastFocusedState = focusEffectKey
                }
                is HomeUIState.Loading -> {
                    focusManager.clearFocus()
                }
            }
        }
    }

    val liveUpcomingTitle = stringResource(R.string.category_live_upcoming)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val scope = this

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .focusRestorer(),
            contentPadding = PaddingValues(vertical = 24.dp),
        ){
            item(contentType = "HomeHeader") {
                HeaderHomeScreen(
                    uiState = uiState,
                    refresh = { viewModel.onRefresh(true) },
                    refreshButtonFocusRequester = headerRefreshButtonFocusRequester
                )
            }

            when (val state = uiState){
                is HomeUIState.Loading -> item(contentType = "LoadingContent") {
                    LoadingHomeScreen(
                        modifier = Modifier
                            .height(scope.maxHeight - 76.dp)
                            .fillMaxWidth()
                    )
                }
                is HomeUIState.Success -> {
                    if (state.categories.isNotEmpty()){
                        val initialFocusKey = state.currentOrUpcomingEvents.firstOrNull()?.id ?: state.categories.firstOrNull()?.items?.firstOrNull()?.id

                        val initialFocusCategoryName = if (state.currentOrUpcomingEvents.isNotEmpty()) {
                            liveUpcomingTitle
                        } else {
                            state.categories.firstOrNull { it.items.isNotEmpty() }?.name
                        }

                        item(key = liveUpcomingTitle, contentType = "CurrentOrUpcomingEvents") {
                            CategoryList(
                                ScheduleCategories(name = liveUpcomingTitle, items = state.currentOrUpcomingEvents),
                                viewModel,
                                onItemSelected = onItemSelected,
                                initialFocusRequester = if (liveUpcomingTitle == initialFocusCategoryName) initialListFocusRequester else null,
                                initialFocusKey = if (liveUpcomingTitle == initialFocusCategoryName) initialFocusKey else null
                            )
                        }
                        items(
                            state.categories,
                            key = { it.name },
                            contentType = { "CategoryList" }
                        ) { category ->
                            CategoryList(
                                category,
                                viewModel,
                                onItemSelected = onItemSelected,
                                initialFocusRequester = if (category.name == initialFocusCategoryName) initialListFocusRequester else null,
                                initialFocusKey = if (category.name == initialFocusCategoryName) initialFocusKey else null
                            )
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
                    ErrorHomeScreen(
                        message = state.message,
                        onRetry = { viewModel.onRefresh() },
                        retryButtonFocusRequester = errorButtonFocusRequester,
                        modifier = Modifier
                            .height(scope.maxHeight - 100.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}