package com.angeldevtech.gol.ui.screens.home

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.angeldevtech.gol.R
import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.ui.screens.home.component.mobile.CategoryChips
import com.angeldevtech.gol.ui.screens.home.component.mobile.EmptyCategoryMobile
import com.angeldevtech.gol.ui.screens.home.component.mobile.EmptyListMobile
import com.angeldevtech.gol.ui.screens.home.component.mobile.ErrorMobileHomeScreen
import com.angeldevtech.gol.ui.screens.home.component.mobile.HeaderMobileHomeScreen
import com.angeldevtech.gol.ui.screens.home.component.mobile.LoadingMobileHomeScreen
import com.angeldevtech.gol.ui.screens.home.component.mobile.ItemCardMobile
import com.angeldevtech.gol.utils.PeriodicTimeUpdateWhileResumed
import com.angeldevtech.gol.utils.findActivity

private const val CATEGORY_LIVE_ID = "internal_live_upcoming"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileHomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onItemSelected: (ScheduleItem) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isPullingToRefresh by viewModel.isPullToRefreshing.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val activity = context.findActivity() as? ComponentActivity ?: return

    LifecycleStartEffect(Unit) {
        viewModel.onRefresh()
        onStopOrDispose { }
    }

    PeriodicTimeUpdateWhileResumed(viewModel)

    var lastOrientation by rememberSaveable { mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) }

    DisposableEffect(Unit) {
        activity.requestedOrientation = lastOrientation
        onDispose {
            lastOrientation = configuration.orientation
        }
    }

    var selectedCategoryId by rememberSaveable { mutableStateOf<String?>(null) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { HeaderMobileHomeScreen() },
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
                        val liveUpcomingTitle = stringResource(R.string.category_live_upcoming)
                        
                        // Map internal ID to display name for the Chips
                        val categoryDisplayNames = mapOf(CATEGORY_LIVE_ID to liveUpcomingTitle) + 
                            state.categories.associate { it.name to it.name }
                        
                        val categoryIds = listOf(CATEGORY_LIVE_ID) + state.categories.map { it.name }

                        if (selectedCategoryId == null || !categoryIds.contains(selectedCategoryId)) {
                            selectedCategoryId = CATEGORY_LIVE_ID
                        }

                        CategoryChips(
                            categories = categoryIds.map { categoryDisplayNames[it] ?: it },
                            selectedCategory = categoryDisplayNames[selectedCategoryId],
                            onCategorySelected = { displayName ->
                                selectedCategoryId = categoryDisplayNames.filterValues { it == displayName }.keys.firstOrNull()
                            },
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

                            if (selectedCategoryId == CATEGORY_LIVE_ID) {
                                itemsToDisplay.addAll(state.currentOrUpcomingEvents)
                            } else {
                                state.categories.find { it.name == selectedCategoryId }?.items?.let {
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