package com.angeldevtech.gol.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.ui.screens.home.component.mobile.CategoryChips
import com.angeldevtech.gol.ui.screens.home.component.mobile.EmptyCategoryMobile
import com.angeldevtech.gol.ui.screens.home.component.mobile.EmptyListMobile
import com.angeldevtech.gol.ui.screens.home.component.mobile.ErrorMobileHomeScreen
import com.angeldevtech.gol.ui.screens.home.component.mobile.HeaderMobileHomeScreen
import com.angeldevtech.gol.ui.screens.home.component.mobile.LoadingMobileHomeScreen
import com.angeldevtech.gol.ui.screens.home.component.mobile.ItemCardMobile
import com.angeldevtech.gol.utils.PeriodicTimeUpdateWhileResumed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileHomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onItemSelected: (ScheduleItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPullingToRefresh by viewModel.isPullToRefreshing.collectAsStateWithLifecycle()

    LifecycleStartEffect(Unit) {
        viewModel.onRefresh()
        onStopOrDispose { }
    }

    PeriodicTimeUpdateWhileResumed(viewModel)

    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { HeaderMobileHomeScreen() },
                // TODO make settings
//                actions = {
//                    IconButton(
//                        onClick = { },
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Settings,
//                            contentDescription = "Settings"
//                        )
//                    }
//                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
                expandedHeight = TopAppBarDefaults.TopAppBarExpandedHeight - 16.dp
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is HomeUIState.Loading -> {
                LoadingMobileHomeScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            is HomeUIState.Success -> {
                if (state.categories.isNotEmpty()){
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val categories =
                            listOf("En juego y en breve") + state.categories.map { it.name }

                        if (selectedCategory == null) {
                            selectedCategory = categories.first()
                        }

                        CategoryChips(
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it },
                            modifier = Modifier
                                .fillMaxWidth()
                        )

                        PullToRefreshBox(
                            isRefreshing = isPullingToRefresh,
                            onRefresh = { viewModel.onRefresh(true) },
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            val screenInfo = LocalWindowInfo.current.containerSize
                            val screenWidth = with(LocalDensity.current) { screenInfo.width.toDp() }

                            val columns = when {
                                screenWidth < 600.dp -> 1
                                screenWidth < 840.dp -> 2
                                screenWidth < 1200.dp -> 3
                                else -> 4
                            }

                            val itemsToDisplay = mutableListOf<ScheduleItem>()

                            if (selectedCategory == "En juego y en breve") {
                                itemsToDisplay.addAll(state.currentOrUpcomingEvents)
                            } else {
                                state.categories.find { it.name == selectedCategory }?.items?.let {
                                    itemsToDisplay.addAll(it)
                                }
                            }

                            if (itemsToDisplay.isNotEmpty()) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(columns),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(itemsToDisplay, key = { it.id }) { item ->
                                        ItemCardMobile(
                                            item = item,
                                            viewModel = viewModel,
                                            onClick = { onItemSelected(item) }
                                        )
                                    }
                                }
                            } else {
                                EmptyCategoryMobile(
                                    modifier = Modifier
                                        .fillMaxSize()
                                )
                            }
                        }
                    }
                } else {
                    EmptyListMobile(
                        onRetry = { viewModel.onRefresh(true) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }

            is HomeUIState.Error -> {
                ErrorMobileHomeScreen(
                    message = state.message,
                    onRetry = { viewModel.onRefresh(true) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}