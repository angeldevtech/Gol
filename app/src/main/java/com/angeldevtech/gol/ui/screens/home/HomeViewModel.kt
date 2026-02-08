package com.angeldevtech.gol.ui.screens.home

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.angeldevtech.gol.domain.usecases.GetScheduleCategoriesUseCase
import com.angeldevtech.gol.domain.usecases.RefreshScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.core.graphics.scale
import androidx.palette.graphics.Palette
import com.angeldevtech.gol.R
import com.angeldevtech.gol.domain.models.ScheduleCategories
import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.utils.PaletteResult
import com.angeldevtech.gol.utils.WhiteFilter
import com.angeldevtech.gol.utils.isLightColor
import android.app.Application
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getScheduleCategories: GetScheduleCategoriesUseCase,
    private val refreshSchedule: RefreshScheduleUseCase,
    private val application: Application
): ViewModel() {

    private val _uiState = MutableStateFlow<HomeUIState>(HomeUIState.Loading)
    val uiState: StateFlow<HomeUIState> = _uiState

    private val _paletteCache = mutableStateMapOf<String, PaletteResult>()
    val paletteCache: Map<String, PaletteResult> get() = _paletteCache

    private val _isPullToRefreshing = MutableStateFlow(false)
    val isPullToRefreshing: StateFlow<Boolean> = _isPullToRefreshing.asStateFlow()

    private var lastRefreshTime = 0L
    private val refreshIntervalMs = 3 * 60 * 60 * 1000L

    private fun getString(resId: Int): String {
        return application.getString(resId)
    }

    private fun observeSchedule() {
        _uiState.value = HomeUIState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = refreshSchedule()

            if (result.isFailure) {
                _isPullToRefreshing.value = false
                withContext(Dispatchers.Main) {
                    _uiState.value = HomeUIState.Error(getString(R.string.error_fetching_events))
                }
                return@launch
            }

            getScheduleCategories()
                .catch {
                    _isPullToRefreshing.value = false
                    withContext(Dispatchers.Main) {
                        _uiState.value =
                            HomeUIState.Error(getString(R.string.error_processing_categories))
                    }
                }
                .collect { scheduleResult ->
                    scheduleResult.fold(
                        onSuccess = { categories ->
                            val currentOrUpcomingEvents =
                                calculateCurrentOrUpcomingEvents(categories)
                            _isPullToRefreshing.value = false
                            withContext(Dispatchers.Main) {
                                _uiState.value =
                                    HomeUIState.Success(categories, currentOrUpcomingEvents)
                            }
                        },
                        onFailure = {
                            _isPullToRefreshing.value = false
                            withContext(Dispatchers.Main) {
                                _uiState.value =
                                    HomeUIState.Error(getString(R.string.error_fetching_events_sorted))
                            }
                        }
                    )
                }
        }
    }

    private fun calculateCurrentOrUpcomingEvents(categories: List<ScheduleCategories>): List<ScheduleItem> {
        if (categories.isEmpty()) {
            return emptyList()
        }

        val now = LocalDateTime.now()
        val defaultWindowStart = now.minusMinutes(30)
        val defaultWindowEnd = now.plusMinutes(30)
        val soccerWindowStart = now.minusMinutes(115)

        return categories
            .asSequence()
            .flatMap { it.items.asSequence() }
            .mapNotNull { item ->
                val eventDateTime = runCatching {
                    LocalDateTime.of(
                        LocalDate.parse(item.date),
                        LocalTime.parse(item.hour)
                    )
                }.getOrNull() ?: return@mapNotNull null

                val windowStart = if (item.category.equals("Futbol", ignoreCase = true)) {
                    soccerWindowStart
                } else {
                    defaultWindowStart
                }

                if (eventDateTime in windowStart..defaultWindowEnd) {
                    item to eventDateTime
                } else {
                    null
                }
            }
            .sortedWith(compareBy({ it.second }, { it.first.date }, { it.first.hour }))
            .map { it.first }
            .toList()
    }

    fun onRefresh(force: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        val shouldRefreshByTime = (currentTime - lastRefreshTime) > refreshIntervalMs

        if (force) {
            _isPullToRefreshing.value = true
        }

        if (force || shouldRefreshByTime || _uiState.value !is HomeUIState.Success) {
            lastRefreshTime = currentTime
            observeSchedule()
        }
    }

    fun triggerTimeBasedUpdate() {
        val currentState = _uiState.value
        if (currentState is HomeUIState.Success) {
            viewModelScope.launch(Dispatchers.Default) {
                val currentOrUpcomingEvents = calculateCurrentOrUpcomingEvents(currentState.categories)
                if (currentState.currentOrUpcomingEvents != currentOrUpcomingEvents) {
                    _uiState.value = currentState.copy(currentOrUpcomingEvents = currentOrUpcomingEvents)
                }
            }
        }
    }

    fun generatePalette(originalBitmap: Bitmap, key: String) {
        if (_paletteCache.containsKey(key)) {
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            val smallBitmap = originalBitmap.scale(50, 50).copy(Bitmap.Config.ARGB_8888, true)

            try {
                val palette = Palette.from(smallBitmap)
                    .maximumColorCount(4)
                    .addFilter(WhiteFilter())
                    .generate()
                palette.let {
                    val predominantColor =
                        it.getDominantColor(Color(red = 73, green = 69, blue = 79).toArgb())

                    val contrastColor = if (predominantColor.isLightColor()) {
                        Color.DarkGray.copy(alpha = 0.8f)
                    } else {
                        Color.White.copy(alpha = 0.8f)
                    }

                    _paletteCache[key] = PaletteResult(
                        cardColor = Color(predominantColor),
                        textColor = contrastColor
                    )
                }
            } finally {
                smallBitmap.recycle()
            }
        }
    }
}